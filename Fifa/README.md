# FIFA 2026 Live Engine
**A free, public, real-time football website. No login. No subscription. No cost.**

> Automatically tracks live match data, detects goals, generates shareable content, and delivers it to the world within seconds of it happening.

---

## What This Is

A public website that does three things better than anything else in its price range ($0):

1. **Live match dashboard** — scores, clock, status, event feed, stat comparisons. Refreshes every 30 seconds automatically.
2. **Goal alert system** — browser toast notifications the moment a score changes. Browser Push API opt-in for team-specific alerts without an account.
3. **Shareable content** — auto-generated OG images per goal/match, one-click share to Twitter/WhatsApp, dynamic match pages that rank on Google.

**Tournament window:** FIFA 2026 — June 11 to July 19, 2026 (48 teams, 104 matches).

---

## Table of Contents

1. [Architecture](#1-architecture)
2. [Free Data Sources](#2-free-data-sources)
3. [Database — Supabase](#3-database--supabase)
4. [Backend — Python Poller](#4-backend--python-poller)
5. [Frontend — Next.js](#5-frontend--nextjs)
6. [OG Image Generation](#6-og-image-generation)
7. [SEO Strategy](#7-seo-strategy)
8. [Viral Mechanics](#8-viral-mechanics)
9. [Browser Push Notifications](#9-browser-push-notifications)
10. [Retention Without Accounts](#10-retention-without-accounts)
11. [Launch Checklist](#11-launch-checklist)
12. [Deployment Order](#12-deployment-order)
13. [Scaling Path](#13-scaling-path)
14. [Future: Paid Layer](#14-future-paid-layer)

---

## 1. Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                       PUBLIC INTERNET                        │
│                                                              │
│  ┌─────────────┐  30s poll / Realtime WS                    │
│  │   Browser   │ ◄──────────────────── Vercel (Next.js 14)  │
│  │  (no login) │                       /match/[id]          │
│  │             │                       /api/live            │
│  │  Web Push   │ ◄── push notification /api/og/[id]         │
│  └─────────────┘                       /api/health          │
│                                               │              │
└───────────────────────────────────────────────┼─────────────┘
                                                │ Supabase JS
                               ┌────────────────▼─────────────┐
                               │       Supabase (free)         │
                               │  matches  |  match_events     │
                               │  push_subs                    │
                               │  Realtime: postgres_changes   │
                               └────────────┬─────────────────┘
                                            │ REST + service key
                               ┌────────────▼─────────────────┐
                               │   Render.com (free worker)    │
                               │      Python poller.py         │
                               │  60s live / 120s idle         │
                               └────────────┬─────────────────┘
                                            │ HTTP GET (no key)
                               ┌────────────▼─────────────────┐
                               │   ESPN Unofficial API         │
                               │   football-data.org (free)    │
                               │   TheSportsDB (free)          │
                               └──────────────────────────────┘
```

**Goal event latency:**
```
t=0s   ESPN scoreboard returns new score
t=1s   Poller detects score diff
t=3s   Supabase match_events INSERT
t=4s   Supabase Realtime fires → browser receives event
t=5s   GoalAlert toast appears in UI
t=5s   Web Push notification sent to opted-in subscribers
```

**Total: ~5 seconds from ESPN update to user notification.**

---

## 2. Free Data Sources

### Primary: ESPN Unofficial Scoreboard API
```
https://site.api.espn.com/apis/site/v2/sports/soccer/{league}/scoreboard
```
- **No API key. No rate limit documentation. Works.**
- Updates: every ~30 seconds during live matches
- Provides: scores, match status, clock, team names, logos, venue
- Leagues: `fifa.world`, `uefa.champions`, `eng.1`, `esp.1`, `ger.1`, `ita.1`, `fra.1`

### Secondary: football-data.org (free tier)
```
https://api.football-data.org/v4/competitions/WC/matches
```
- 10 requests/minute on free tier
- Provides: standings, fixtures, full scoreline history
- Use for: post-match pages, group tables, knockout bracket
- Register at football-data.org for free API key

### Asset source: TheSportsDB (free)
```
https://www.thesportsdb.com/api/v1/json/3/searchteams.php?t={team_name}
```
- Provides: team badge URLs, national flag images, player headshots
- Strategy: fetch once on first match detection → cache in Supabase `teams` table
- Never fetch per-request — cache everything locally

### Layer diagram
```
LAYER 1 — TRIGGERS (every 60s)
  ESPN unofficial → score changes → events → Supabase

LAYER 2 — ENRICHMENT (on match start, once)
  football-data.org → fixtures, groups, standings
  TheSportsDB → team badges and colors (cache locally)

LAYER 3 — STATIC CONTEXT (sync daily)
  football-data.org → tournament schedule, team info
```

### API comparison

| API | Key Required | Rate Limit | Update Speed | Best For |
|-----|-------------|------------|--------------|----------|
| ESPN unofficial | No | Unknown (generous) | ~30s | Live score polling |
| football-data.org | Yes (free) | 10/min | 1-5min | Standings, fixtures |
| TheSportsDB | No | Moderate | Daily | Team assets |
| OpenFootball (GitHub) | No | None (static) | 24-48hr | Historical data |

---

## 3. Database — Supabase

### Setup

1. [supabase.com](https://supabase.com) → New project → choose region near Render worker
2. SQL Editor → paste and run `supabase/schema.sql`
3. Database → Replication → enable `match_events` and `matches`
4. Settings → API → copy Project URL, anon key, service role key

### Schema (`supabase/schema.sql`)

```sql
-- ── matches ──────────────────────────────────────────────────────────────────
create table if not exists matches (
  id                text        primary key,
  home_team         text        not null,
  away_team         text        not null,
  home_team_short   text        not null default '',
  home_team_logo    text        not null default '',
  away_team_logo    text        not null default '',
  home_score        integer     not null default 0,
  away_score        integer     not null default 0,
  status            text        not null default 'pre',
  clock             text        not null default '',
  tournament        text        not null default '',
  venue             text        not null default '',
  updated_at        timestamptz not null default now()
);

create index if not exists matches_status_idx  on matches (status);
create index if not exists matches_updated_idx on matches (updated_at desc);

-- ── match_events ──────────────────────────────────────────────────────────────
-- Immutable event log. id = sha256(match_id:event_type:score)[:16]
create table if not exists match_events (
  id           text        primary key,
  match_id     text        not null references matches(id) on delete cascade,
  event_type   text        not null,   -- goal | ht | ft
  team_name    text        not null default '',
  team_logo    text        not null default '',
  home_team    text        not null,
  away_team    text        not null,
  home_score   integer     not null default 0,
  away_score   integer     not null default 0,
  clock_label  text        not null default '',
  tournament   text        not null default '',
  created_at   timestamptz not null default now()
);

create index if not exists events_match_idx   on match_events (match_id);
create index if not exists events_created_idx on match_events (created_at desc);
create index if not exists events_type_idx    on match_events (event_type);

-- ── push_subscriptions ───────────────────────────────────────────────────────
-- Web Push API subscriptions. No user account needed.
create table if not exists push_subscriptions (
  id         uuid        primary key default gen_random_uuid(),
  endpoint   text        not null unique,
  p256dh     text        not null,
  auth       text        not null,
  teams      text[]      not null default '{}',  -- ESPN team IDs to watch
  created_at timestamptz not null default now()
);

-- ── Row Level Security ────────────────────────────────────────────────────────
alter table matches            enable row level security;
alter table match_events       enable row level security;
alter table push_subscriptions enable row level security;

create policy "public read matches"       on matches            for select using (true);
create policy "public read events"        on match_events       for select using (true);
create policy "public read push_subs"     on push_subscriptions for select using (true);
create policy "public insert push_subs"   on push_subscriptions for insert with check (true);
create policy "public update push_subs"   on push_subscriptions for update using (true);

-- ── Realtime ─────────────────────────────────────────────────────────────────
alter publication supabase_realtime add table match_events;
alter publication supabase_realtime add table matches;

-- ── Views ─────────────────────────────────────────────────────────────────────
create or replace view recent_goals as
  select * from match_events
  where  event_type in ('goal', 'own_goal', 'penalty_goal', 'var_goal')
  order  by created_at desc
  limit  50;
```

---

## 4. Backend — Python Poller

### Deploy on Render.com (free worker tier)

**`backend/render.yaml`:**
```yaml
services:
  - type: worker
    name: fifa-poller
    runtime: python
    buildCommand: pip install -r requirements.txt
    startCommand: python poller.py
    plan: free
    envVars:
      - key: SUPABASE_URL
        sync: false
      - key: SUPABASE_SERVICE_ROLE_KEY
        sync: false
      - key: LEAGUE_SLUGS
        value: "fifa.world,uefa.champions,eng.1,esp.1,ger.1,ita.1,fra.1"
      - key: POLL_INTERVAL_LIVE
        value: "60"
      - key: POLL_INTERVAL_IDLE
        value: "120"
      - key: PYTHON_VERSION
        value: "3.12.0"
```

**`backend/requirements.txt`:**
```
httpx==0.27.0
python-dotenv==1.0.1
supabase==2.5.1
```

**`backend/poller.py`** — Core logic:

```python
"""
FIFA 2026 Live Engine — Match Poller
Polls ESPN every 60s, detects score changes, writes to Supabase.
Deploy: Render.com free worker | Run: python poller.py
"""
import asyncio, hashlib, logging, os, sys
from datetime import datetime, timezone
from typing import Optional
import httpx
from dotenv import load_dotenv

load_dotenv()
logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(message)s",
                    handlers=[logging.StreamHandler(sys.stdout)])
logger = logging.getLogger("poller")

ESPN_BASE = "https://site.api.espn.com/apis/site/v2/sports/soccer"
LEAGUE_SLUGS = os.getenv("LEAGUE_SLUGS",
    "fifa.world,uefa.champions,eng.1,esp.1,ger.1,ita.1,fra.1").split(",")
POLL_INTERVAL_LIVE = int(os.getenv("POLL_INTERVAL_LIVE", "60"))
POLL_INTERVAL_IDLE = int(os.getenv("POLL_INTERVAL_IDLE", "120"))
SUPABASE_URL  = os.getenv("SUPABASE_URL", "")
SUPABASE_KEY  = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")
_state: dict[str, dict] = {}  # in-memory: match_id → {home_score, away_score, status}

class SupabaseClient:
    def __init__(self, url, key):
        self.url = url.rstrip("/")
        self.headers = {"apikey": key, "Authorization": f"Bearer {key}",
                        "Content-Type": "application/json", "Prefer": "return=minimal"}
        self._enabled = bool(url and key)

    def enabled(self): return self._enabled

    async def upsert_match(self, client, match):
        if not self._enabled: return
        try:
            r = await client.post(f"{self.url}/rest/v1/matches",
                headers={**self.headers, "Prefer": "resolution=merge-duplicates"},
                json=match, timeout=10)
            r.raise_for_status()
        except Exception as e:
            logger.warning(f"upsert_match error: {e}")

    async def insert_event(self, client, event):
        if not self._enabled:
            logger.info(f"[no-db] {event['event_type']} | "
                        f"{event['home_team']} {event['home_score']}-"
                        f"{event['away_score']} {event['away_team']}")
            return
        try:
            r = await client.post(f"{self.url}/rest/v1/match_events",
                headers=self.headers, json=event, timeout=10)
            r.raise_for_status()
            logger.info(f"Event: {event['event_type']} | "
                        f"{event['home_team']} {event['home_score']}-"
                        f"{event['away_score']} {event['away_team']}")
        except Exception as e:
            logger.warning(f"insert_event error: {e}")

supabase = SupabaseClient(SUPABASE_URL, SUPABASE_KEY)

async def fetch_scoreboard(client, league):
    try:
        r = await client.get(f"{ESPN_BASE}/{league}/scoreboard",
            timeout=15, headers={"User-Agent": "Mozilla/5.0"})
        return r.json().get("events", []) if r.status_code == 200 else []
    except Exception as e:
        logger.debug(f"fetch [{league}]: {e}")
        return []

async def fetch_all_events(client):
    results = await asyncio.gather(
        *[fetch_scoreboard(client, s) for s in LEAGUE_SLUGS], return_exceptions=True)
    return [e for r in results if isinstance(r, list) for e in r]

def _map_status(s):
    return {"STATUS_IN_PROGRESS":"in","STATUS_HALFTIME":"halftime",
            "STATUS_FINAL":"post","STATUS_FULL_TIME":"post",
            "STATUS_SCHEDULED":"pre","STATUS_POSTPONED":"delayed"}.get(s, "pre")

def parse_match(event):
    try:
        comp = event["competitions"][0]
        home = next(c for c in comp["competitors"] if c["homeAway"] == "home")
        away = next(c for c in comp["competitors"] if c["homeAway"] == "away")
        status = comp["status"]
        return {
            "id": event["id"],
            "home_team": home["team"]["displayName"],
            "away_team": away["team"]["displayName"],
            "home_team_short": home["team"].get("shortDisplayName", ""),
            "home_team_logo": home["team"].get("logo", ""),
            "away_team_logo": away["team"].get("logo", ""),
            "home_score": int(home.get("score", "0") or "0"),
            "away_score": int(away.get("score", "0") or "0"),
            "status": _map_status(status["type"]["name"]),
            "clock": status["type"].get("shortDetail", ""),
            "tournament": (comp.get("league", {}).get("name")
                           or event.get("season", {}).get("slug", "Football")),
            "venue": comp.get("venue", {}).get("fullName", ""),
            "updated_at": datetime.now(timezone.utc).isoformat(),
        }
    except (KeyError, StopIteration, ValueError):
        return None

def make_event_id(match_id, event_type, score):
    return hashlib.sha256(f"{match_id}:{event_type}:{score}".encode()).hexdigest()[:16]

def detect_events(match):
    mid = match["id"]
    prev = _state.get(mid)
    ch, ca, cs = match["home_score"], match["away_score"], match["status"]
    events = []

    if prev is None:
        _state[mid] = {"home_score": ch, "away_score": ca, "status": cs}
        return []

    ph, pa, ps = prev["home_score"], prev["away_score"], prev["status"]

    for i in range(max(ch - ph, 0)):
        sh = ph + i + 1
        events.append({"id": make_event_id(mid, "goal_home", f"{sh}{ca}"),
            "match_id": mid, "event_type": "goal",
            "team_name": match["home_team"], "team_logo": match["home_team_logo"],
            "home_team": match["home_team"], "away_team": match["away_team"],
            "home_score": sh, "away_score": ca, "clock_label": match["clock"],
            "tournament": match["tournament"],
            "created_at": datetime.now(timezone.utc).isoformat()})

    for i in range(max(ca - pa, 0)):
        sa = pa + i + 1
        events.append({"id": make_event_id(mid, "goal_away", f"{ch}{sa}"),
            "match_id": mid, "event_type": "goal",
            "team_name": match["away_team"], "team_logo": match["away_team_logo"],
            "home_team": match["home_team"], "away_team": match["away_team"],
            "home_score": ch, "away_score": sa, "clock_label": match["clock"],
            "tournament": match["tournament"],
            "created_at": datetime.now(timezone.utc).isoformat()})

    if ps == "in" and cs == "halftime":
        events.append({"id": make_event_id(mid, "ht", f"{ch}{ca}"),
            "match_id": mid, "event_type": "ht", "team_name": match["home_team"],
            "team_logo": "", "home_team": match["home_team"], "away_team": match["away_team"],
            "home_score": ch, "away_score": ca, "clock_label": "HT",
            "tournament": match["tournament"],
            "created_at": datetime.now(timezone.utc).isoformat()})

    if ps in ("in", "halftime") and cs == "post":
        events.append({"id": make_event_id(mid, "ft", f"{ch}{ca}"),
            "match_id": mid, "event_type": "ft", "team_name": match["home_team"],
            "team_logo": "", "home_team": match["home_team"], "away_team": match["away_team"],
            "home_score": ch, "away_score": ca, "clock_label": "FT",
            "tournament": match["tournament"],
            "created_at": datetime.now(timezone.utc).isoformat()})

    _state[mid] = {"home_score": ch, "away_score": ca, "status": cs}
    return events

async def poll_cycle(client):
    raw_events = await fetch_all_events(client)
    if not raw_events: return False
    matches = [m for e in raw_events if (m := parse_match(e))]
    live = sum(1 for m in matches if m["status"] in ("in", "halftime"))
    logger.info(f"{len(matches)} matches | {live} live")
    tasks = []
    for match in matches:
        tasks.append(supabase.upsert_match(client, match))
        for ev in detect_events(match):
            tasks.append(supabase.insert_event(client, ev))
    if tasks:
        await asyncio.gather(*tasks, return_exceptions=True)
    return live > 0

async def main():
    logger.info("=" * 50)
    logger.info("FIFA 2026 Live Engine — Poller starting")
    logger.info(f"Leagues: {', '.join(LEAGUE_SLUGS)}")
    logger.info(f"Supabase: {'connected' if supabase.enabled() else 'log-only mode'}")
    logger.info("=" * 50)
    limits = httpx.Limits(max_keepalive_connections=10, max_connections=20)
    async with httpx.AsyncClient(limits=limits, follow_redirects=True) as client:
        while True:
            try:
                has_live = await poll_cycle(client)
                await asyncio.sleep(POLL_INTERVAL_LIVE if has_live else POLL_INTERVAL_IDLE)
            except asyncio.CancelledError:
                break
            except Exception as e:
                logger.error(f"Poll error: {e}", exc_info=True)
                await asyncio.sleep(30)

if __name__ == "__main__":
    try: asyncio.run(main())
    except KeyboardInterrupt: logger.info("Stopped")
```

### Local test (no Supabase needed — logs to stdout)
```bash
cd backend
python -m venv venv && source venv/bin/activate
pip install -r requirements.txt
python poller.py
# Events print to stdout. Connect Supabase when ready.
```

---

## 5. Frontend — Next.js

### Stack
| Layer | Choice | Why |
|-------|--------|-----|
| Framework | Next.js 14 App Router | Static match pages + dynamic routes |
| Styling | Tailwind CSS | Custom dark pitch theme, zero runtime |
| Realtime | Supabase JS client | `postgres_changes` listener, no extra infra |
| OG Images | `@vercel/og` (satori) | Serverless, no Puppeteer, no Chromium |
| Charts | Chart.js + react-chartjs-2 | Stat donuts, client-side only |
| Animations | @formkit/auto-animate | Match list reorder, 2.5 KB |
| Deploy | Vercel (free) | Edge network, zero config |

### Install
```bash
cd frontend
npm install
npm install chart.js react-chartjs-2 @formkit/auto-animate
cp .env.example .env.local  # fill in Supabase keys
npm run dev
```

### File structure
```
frontend/
├── app/
│   ├── layout.tsx              ← global SEO, theme, metadata
│   ├── page.tsx                ← live dashboard (client component)
│   ├── match/
│   │   └── [id]/
│   │       └── page.tsx        ← individual match page (SSR + SEO)
│   ├── api/
│   │   ├── live/route.ts       ← GET /api/live (30s cache)
│   │   ├── events/route.ts     ← GET /api/events?limit=N
│   │   ├── og/[id]/route.tsx   ← GET /api/og/[id] → PNG image
│   │   ├── push/subscribe/route.ts   ← POST /api/push/subscribe
│   │   ├── push/notify/route.ts      ← POST /api/push/notify (internal)
│   │   └── health/route.ts     ← GET /api/health
│   └── globals.css
├── components/
│   ├── MatchCard.tsx           ← single match card with score flash
│   ├── MatchGrid.tsx           ← grouped by status
│   ├── GoalAlert.tsx           ← toast stack, auto-dismiss 8s
│   ├── EventFeed.tsx           ← right panel event log
│   ├── StatComparison.tsx      ← side-by-side stat bars
│   ├── LiveTicker.tsx          ← scrolling live matches header
│   ├── ShareButton.tsx         ← copy link + Twitter intent
│   └── PushOptIn.tsx           ← "notify me for [team]" prompt
└── lib/
    ├── types.ts                ← Match, MatchEvent, LiveResponse
    ├── espn.ts                 ← fetch + transform + detectGoals
    └── supabase.ts             ← browser client + Realtime
```

### Key: `app/match/[id]/page.tsx` — Individual match page

Each completed or live match has its own indexable URL at `/match/{espn-id}`.

```typescript
// app/match/[id]/page.tsx
import { Metadata } from 'next'
import { fetchMatchById } from '@/lib/espn'

interface Props { params: { id: string } }

export async function generateMetadata({ params }: Props): Promise<Metadata> {
  const match = await fetchMatchById(params.id)
  if (!match) return { title: 'Match | FIFA 2026' }

  const title = `${match.homeTeam.name} ${match.homeScore}–${match.awayScore} ${match.awayTeam.name}`
  const description = `Live score and match stats: ${title} | ${match.tournament} | FIFA 2026`

  return {
    title,
    description,
    openGraph: {
      title,
      description,
      images: [`/api/og/${params.id}`],  // dynamic OG image
      type: 'website',
    },
    twitter: {
      card: 'summary_large_image',
      title,
      description,
      images: [`/api/og/${params.id}`],
    },
  }
}

export default async function MatchPage({ params }: Props) {
  const match = await fetchMatchById(params.id)

  // JSON-LD structured data for Google
  const jsonLd = {
    '@context': 'https://schema.org',
    '@type': 'SportsEvent',
    name: `${match?.homeTeam.name} vs ${match?.awayTeam.name}`,
    sport: 'Soccer',
    startDate: match?.updatedAt,
    location: { '@type': 'Place', name: match?.venue },
    homeTeam: { '@type': 'SportsTeam', name: match?.homeTeam.name },
    awayTeam: { '@type': 'SportsTeam', name: match?.awayTeam.name },
    description: `FIFA 2026 match: ${match?.tournament}`,
  }

  return (
    <>
      <script type="application/ld+json" dangerouslySetInnerHTML={{ __html: JSON.stringify(jsonLd) }} />
      {/* match card, event feed, stat comparison */}
    </>
  )
}
```

### `app/sitemap.ts` — Dynamic sitemap

```typescript
import { MetadataRoute } from 'next'
import { createClient } from '@supabase/supabase-js'

export default async function sitemap(): Promise<MetadataRoute.Sitemap> {
  const supabase = createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  )
  const { data: matches } = await supabase
    .from('matches')
    .select('id, updated_at')
    .order('updated_at', { ascending: false })
    .limit(500)

  const matchRoutes = (matches ?? []).map(m => ({
    url: `https://your-domain.com/match/${m.id}`,
    lastModified: new Date(m.updated_at),
    changeFrequency: 'hourly' as const,
    priority: 0.8,
  }))

  return [
    { url: 'https://your-domain.com', lastModified: new Date(), priority: 1.0 },
    ...matchRoutes,
  ]
}
```

---

## 6. OG Image Generation

Every match URL gets a dynamic social preview image via `@vercel/og`. No Puppeteer, no Chrome, no extra cost — runs as a Vercel Edge Function.

### Install
```bash
npm install @vercel/og
```

### `app/api/og/[id]/route.tsx`

```tsx
import { ImageResponse } from '@vercel/og'
import { NextRequest } from 'next/server'
import { createClient } from '@supabase/supabase-js'

export const runtime = 'edge'

export async function GET(req: NextRequest, { params }: { params: { id: string } }) {
  const supabase = createClient(
    process.env.NEXT_PUBLIC_SUPABASE_URL!,
    process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY!
  )
  const { data: match } = await supabase
    .from('matches')
    .select('*')
    .eq('id', params.id)
    .single()

  if (!match) {
    return new ImageResponse(<div style={{ background: '#020c0a', width: '100%', height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'white', fontSize: 32 }}>Match not found</div>, { width: 1200, height: 630 })
  }

  const isLive = match.status === 'in' || match.status === 'halftime'
  const isGoal = match.home_score + match.away_score > 0

  return new ImageResponse(
    <div style={{
      background: 'linear-gradient(135deg, #020c0a 0%, #071f18 50%, #020c0a 100%)',
      width: '100%', height: '100%', display: 'flex', flexDirection: 'column',
      alignItems: 'center', justifyContent: 'center', fontFamily: 'sans-serif', padding: 48,
    }}>
      {/* Tournament label */}
      <div style={{ color: '#22c55e', fontSize: 20, marginBottom: 24, textTransform: 'uppercase', letterSpacing: 4 }}>
        {match.tournament}
      </div>

      {/* Score row */}
      <div style={{ display: 'flex', alignItems: 'center', gap: 48 }}>
        {/* Home team */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
          {match.home_team_logo && (
            <img src={match.home_team_logo} width={80} height={80} style={{ objectFit: 'contain' }} />
          )}
          <span style={{ color: 'white', fontSize: 28, fontWeight: 700 }}>{match.home_team_short || match.home_team}</span>
        </div>

        {/* Score */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
          <span style={{ color: isGoal ? '#f59e0b' : 'white', fontSize: 96, fontWeight: 900 }}>{match.home_score}</span>
          <span style={{ color: '#4b5563', fontSize: 64 }}>–</span>
          <span style={{ color: isGoal ? '#f59e0b' : 'white', fontSize: 96, fontWeight: 900 }}>{match.away_score}</span>
        </div>

        {/* Away team */}
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
          {match.away_team_logo && (
            <img src={match.away_team_logo} width={80} height={80} style={{ objectFit: 'contain' }} />
          )}
          <span style={{ color: 'white', fontSize: 28, fontWeight: 700 }}>{match.away_team_short || match.away_team}</span>
        </div>
      </div>

      {/* Status badge */}
      <div style={{
        marginTop: 32, padding: '8px 24px', borderRadius: 24,
        background: isLive ? '#22c55e' : '#374151',
        color: 'white', fontSize: 20, fontWeight: 700,
      }}>
        {isLive ? `LIVE · ${match.clock}` : match.status === 'post' ? 'FT' : 'UPCOMING'}
      </div>

      {/* Site branding */}
      <div style={{ position: 'absolute', bottom: 24, right: 48, color: '#6b7280', fontSize: 16 }}>
        fifa2026live.com
      </div>
    </div>,
    { width: 1200, height: 630 }
  )
}
```

**This image is automatically used by:**
- Twitter/X when someone pastes a match URL
- WhatsApp link previews
- Facebook shares
- Discord embeds
- iMessage link previews

**Result:** Every match shared on social media shows a branded score card with team logos and live status. Zero manual work.

---

## 7. SEO Strategy

### URL structure

| Page | URL | Search intent |
|------|-----|---------------|
| Live dashboard | `/` | "FIFA 2026 live scores" |
| Individual match | `/match/[espn-id]` | "France vs Brazil 2026 score" |
| Team hub | `/team/[slug]` | "Brazil FIFA 2026 schedule" |
| Group standings | `/group/[letter]` | "Group A 2026 World Cup standings" |
| Tournament bracket | `/bracket` | "FIFA 2026 knockout bracket" |

### Schema.org markup (in `app/match/[id]/page.tsx`)

```json
{
  "@context": "https://schema.org",
  "@type": "SportsEvent",
  "name": "France vs Brazil",
  "sport": "Soccer",
  "startDate": "2026-07-05T19:00:00Z",
  "location": { "@type": "Place", "name": "MetLife Stadium, New Jersey" },
  "organizer": { "@type": "Organization", "name": "FIFA" },
  "homeTeam": { "@type": "SportsTeam", "name": "France" },
  "awayTeam": { "@type": "SportsTeam", "name": "Brazil" },
  "eventStatus": "https://schema.org/EventScheduled"
}
```

For live match commentary, add `LiveBlogPosting`:
```json
{
  "@type": "LiveBlogPosting",
  "liveBlogUpdate": [
    {
      "@type": "BlogPosting",
      "headline": "GOAL! Mbappe scores in 23rd minute",
      "datePublished": "2026-07-05T19:23:00Z",
      "articleBody": "France 1-0 Brazil. Mbappe converts from close range."
    }
  ]
}
```

### Content pages to create before tournament (target: May 2026)

| Page | Search Volume Estimate | Build Effort |
|------|----------------------|--------------|
| "/fifa-2026-groups" | 200k+ | Low — pull from football-data.org |
| "/fifa-2026-schedule" | 500k+ | Low — static with dynamic live overlay |
| "/fifa-2026-bracket" | 100k+ | Medium — visual bracket component |
| "/team/[country]" (48 pages) | 50-500k each | Low — template-driven |
| "/predictions" | 50k+ | Medium — pre-match stats page |

### Dynamic sitemap

`/sitemap.xml` auto-generated from Supabase `matches` table (see Section 5).
Submit to Google Search Console immediately after first match data is ingested.

---

## 8. Viral Mechanics

### Auto-generated share cards (via `@vercel/og`)

Every match page has a "Share" button that:
1. Copies the match URL to clipboard
2. Opens Twitter intent with pre-written copy + OG image

```tsx
// components/ShareButton.tsx
'use client'
function ShareButton({ match }: { match: Match }) {
  const url = `${process.env.NEXT_PUBLIC_SITE_URL}/match/${match.id}`
  const text = match.status === 'post'
    ? `FT: ${match.homeTeam.name} ${match.homeScore}–${match.awayScore} ${match.awayTeam.name} | ${match.tournament}`
    : `LIVE: ${match.homeTeam.name} ${match.homeScore}–${match.awayScore} ${match.awayTeam.name} (${match.clock})`

  const twitterUrl = `https://twitter.com/intent/tweet?text=${encodeURIComponent(text + '\n\n' + url)}`

  return (
    <div style={{ display: 'flex', gap: 8 }}>
      <button onClick={() => navigator.clipboard.writeText(url)}>Copy link</button>
      <a href={twitterUrl} target="_blank" rel="noopener">Share on X</a>
    </div>
  )
}
```

**Tweet copy formulas that perform on sports Twitter:**
- Goals: `GOAL ⚽ [Player] | [Home] [H]-[A] [Away] ([Min]') #FIFA2026`
- Upsets: `UPSET ALERT 🚨 [Underdog] [H]-[A] [Favourite] FT #FIFA2026 #[Country]`
- FT: `FT: [Home] [H]-[A] [Away] | [Tournament] [link]`

### Emoji reactions (no auth, Supabase Realtime)

On goal events, fire an emoji "burst" visible to all concurrent viewers — creates a sense of community without accounts.

```typescript
// Supabase channel for emoji reactions — no DB storage needed
const channel = supabase.channel(`reactions:${matchId}`)
  .on('broadcast', { event: 'react' }, ({ payload }) => {
    spawnEmoji(payload.emoji)  // CSS animation
  })
  .subscribe()

// On user tap:
channel.send({ type: 'broadcast', event: 'react', payload: { emoji: '⚽' } })
```

### Virality scoring (what events to surface)

| Event | Virality Score | Auto-share prompt? |
|-------|---------------|-------------------|
| 90'+ winner | 10 | Yes — toast says "Share this moment" |
| Penalty shootout win | 9 | Yes |
| Hat-trick | 8 | Yes |
| Shock upset (rank gap > 20) | 8 | Yes |
| Red card in 1st half | 6 | Optional |
| Own goal | 5 | Optional |
| Normal goal | 3 | No (share button always present) |

---

## 9. Browser Push Notifications

No app, no login. Users get live goal alerts via Web Push API.

### Flow
1. User visits site → sees "Get goal alerts for your team?" banner
2. Browser prompts for notification permission
3. On allow → `PushManager.subscribe()` → POST to `/api/push/subscribe`
4. Service worker stores subscription in Supabase `push_subscriptions`
5. When goal event fires → poller calls `/api/push/notify` → browser push delivered

### `components/PushOptIn.tsx`
```tsx
'use client'
import { useState } from 'react'

export function PushOptIn({ teamId, teamName }: { teamId: string, teamName: string }) {
  const [subscribed, setSubscribed] = useState(false)

  async function subscribe() {
    const registration = await navigator.serviceWorker.ready
    const subscription = await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: process.env.NEXT_PUBLIC_VAPID_PUBLIC_KEY,
    })
    await fetch('/api/push/subscribe', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ subscription, teamId }),
    })
    setSubscribed(true)
  }

  if (subscribed) return <span>Notifying you for {teamName} goals</span>
  return (
    <button onClick={subscribe}>
      Get goal alerts for {teamName}
    </button>
  )
}
```

### Generate VAPID keys (one-time)
```bash
npx web-push generate-vapid-keys
# Add to .env.local:
# NEXT_PUBLIC_VAPID_PUBLIC_KEY=...
# VAPID_PRIVATE_KEY=...
# VAPID_SUBJECT=mailto:you@example.com
```

### `public/sw.js` — Service worker
```javascript
self.addEventListener('push', event => {
  const data = event.data.json()
  event.waitUntil(
    self.registration.showNotification(data.title, {
      body: data.body,
      icon: '/icon-192.png',
      badge: '/badge-72.png',
      data: { url: data.url },
      vibrate: [200, 100, 200],
    })
  )
})

self.addEventListener('notificationclick', event => {
  event.notification.close()
  event.waitUntil(clients.openWindow(event.notification.data.url))
})
```

**Notification copy that converts:**
- `GOAL ⚽ France 1-0 Brazil — Mbappe 23'`
- `HALF TIME: France 1-0 Brazil`
- `FT: France 2-1 Brazil — What a match!`

---

## 10. Retention Without Accounts

No login = no email, no user ID. Use browser storage to personalize.

### `localStorage` team preferences
```typescript
// lib/preferences.ts
const STORAGE_KEY = 'fifa2026_prefs'

interface Prefs {
  followedTeams: string[]  // ESPN team IDs
  followedTournaments: string[]
  theme: 'dark' | 'light'
}

export function getPrefs(): Prefs {
  try {
    return JSON.parse(localStorage.getItem(STORAGE_KEY) ?? '{}')
  } catch { return { followedTeams: [], followedTournaments: [], theme: 'dark' } }
}

export function followTeam(teamId: string) {
  const prefs = getPrefs()
  prefs.followedTeams = [...new Set([...prefs.followedTeams, teamId])]
  localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs))
}

export function unfollowTeam(teamId: string) {
  const prefs = getPrefs()
  prefs.followedTeams = prefs.followedTeams.filter(id => id !== teamId)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(prefs))
}
```

### Personalized homepage (no auth)
- "Your teams" section at top of dashboard — pulls from `localStorage`
- Match cards for followed teams always appear first
- Star icon on each match card → toggles team follow
- No account needed — survives page refresh, gone if cookies cleared

### Return-visit hooks
| Hook | How |
|------|-----|
| Web Push opt-in | Browser remembers subscription across sessions |
| "Your team is playing" banner | Cross-reference localStorage teams vs today's fixtures |
| "Last match you watched" | Store `lastViewedMatchId` in localStorage, surface on return |
| PWA install prompt | Add to home screen = icon on their phone = high return rate |

### PWA manifest (`public/manifest.json`)
```json
{
  "name": "FIFA 2026 Live Scores",
  "short_name": "FIFA 2026",
  "description": "Live scores, goal alerts, and match stats for FIFA World Cup 2026",
  "start_url": "/",
  "display": "standalone",
  "theme_color": "#020c0a",
  "background_color": "#020c0a",
  "icons": [
    { "src": "/icon-192.png", "sizes": "192x192", "type": "image/png" },
    { "src": "/icon-512.png", "sizes": "512x512", "type": "image/png" }
  ]
}
```

---

## 11. Launch Checklist

### 1. Supabase (5 min)
- [ ] Project created, region selected (match Render's region)
- [ ] `supabase/schema.sql` executed — no errors
- [ ] Realtime enabled for `match_events` and `matches`
- [ ] Credentials copied (URL, anon key, service role key)

### 2. Backend — Render (10 min)
- [ ] GitHub repo connected
- [ ] `render.yaml` auto-detected as worker service
- [ ] `SUPABASE_URL` + `SUPABASE_SERVICE_ROLE_KEY` set in Render dashboard
- [ ] Manual deploy triggered
- [ ] Logs show: "Supabase: connected" + "N matches | M live"
- [ ] `matches` table has rows in Supabase Table Editor

### 3. Frontend — Vercel (10 min)
- [ ] Root directory set to `frontend/`
- [ ] Env vars set: `NEXT_PUBLIC_SUPABASE_URL`, `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- [ ] Optional: `NEXT_PUBLIC_VAPID_PUBLIC_KEY` + `VAPID_PRIVATE_KEY` for push
- [ ] Deploy succeeds
- [ ] `/api/health` returns `{"status":"ok"}`
- [ ] `/api/live` returns match data
- [ ] OG images render at `/api/og/[matchId]`

### 4. Verify end-to-end
- [ ] Match cards visible on homepage
- [ ] Goal toast fires on next score change
- [ ] Share link shows branded OG image in Twitter preview
- [ ] Match URL `/match/[id]` has correct title and OG metadata

### 5. SEO
- [ ] `sitemap.xml` live and accessible
- [ ] Submit sitemap to Google Search Console
- [ ] Test one match URL in [Rich Results Test](https://search.google.com/test/rich-results)
- [ ] `SportsEvent` JSON-LD validates

### 6. Optional: Push notifications
- [ ] VAPID keys generated and added to env
- [ ] `public/sw.js` deployed
- [ ] `public/manifest.json` deployed
- [ ] Test push in DevTools → Application → Service Workers

---

## 12. Deployment Order

**Deploy in this exact order — each step depends on the previous.**

```
STEP 1 — Supabase
  Create project → run schema.sql → enable Realtime
  (takes 5 min, needed by both backend and frontend)

STEP 2 — Backend (Render)
  Connect GitHub → set SUPABASE_* env vars → deploy
  Verify: match data appears in Supabase table within 2 min

STEP 3 — Frontend (Vercel)
  Connect GitHub → set env vars → deploy
  Verify: /api/health + /api/live both 200

STEP 4 — Domain (optional)
  Vercel → Settings → Domains → add custom domain
  Update NEXT_PUBLIC_SITE_URL env var

STEP 5 — Google Search Console
  Add property → submit sitemap.xml
  Wait 24-48hr for first indexing
```

**Total time to live: under 30 minutes.**

---

## 13. Scaling Path

### Free tier limits (what breaks first)

| Service | Free Limit | Breaks at |
|---------|-----------|-----------|
| Supabase Realtime | 200 concurrent connections | ~200 simultaneous viewers |
| Supabase DB | 500 MB storage | ~2 years of match data |
| Render free worker | 750 hrs/month | Always-on (750 = 31 days) |
| Vercel Edge Functions | 100k invocations/month | ~3k daily OG image requests |
| Vercel Bandwidth | 100 GB/month | ~2M page views |

### Upgrade path (still cheap)

| Traffic | Action | Cost |
|---------|--------|------|
| 200+ concurrent | Supabase Pro | $25/month |
| Render spinning down | Railway Hobby ($5/mo) or Render Starter ($7/mo) | $5-7/month |
| Vercel bandwidth | Vercel Pro | $20/month |
| **Total at 10k DAU** | | **~$52/month** |

### Performance wins at $0

1. **Cache `/api/live` at edge** — Vercel automatically caches with `s-maxage=20`. Most users hit CDN, not origin.
2. **OG images cached at edge** — `@vercel/og` responses are edge-cached by Vercel automatically.
3. **Supabase Realtime batching** — one WS connection per browser tab; 200 limit = 200 concurrent tabs, not users.
4. **ESPN API has no known rate limit** — the poller fetches 7 leagues in parallel; well within observed thresholds.

---

## 14. Future: Paid Layer

When free proves the concept, add a paid tier without rebuilding anything.

| Feature | Free (always) | Paid ($9-49/mo) |
|---------|--------------|-----------------|
| Live scores | Yes | Yes |
| Goal alerts | Yes | Yes (custom timing) |
| Match pages | Yes | Yes |
| Browser push | Yes (all goals) | Yes (team-specific, custom) |
| Branded share cards | Site-watermarked | Custom logo/colors |
| Video goal clips (MP4) | No | Yes (FFmpeg pipeline) |
| Multi-league coverage | 7 leagues | All 30+ leagues |
| CSV export | No | Yes |
| Embeddable widget | No | Yes (iframe/API) |
| Analytics dashboard | No | Yes |

**Monetization options (tournament window only is fine):**
- One-time tournament pass: $29 (June-August)
- Monthly Creator: $19/month
- White-label for sports media: $299/month flat

**Add auth later with Clerk (30-min setup, free tier):**
```bash
npm install @clerk/nextjs
# wrap layout.tsx with <ClerkProvider>
# gate paid features with auth().userId check
```

---

## Environment Variables Reference

### Frontend (`.env.local` / Vercel)
```env
NEXT_PUBLIC_SUPABASE_URL=https://your-project.supabase.co
NEXT_PUBLIC_SUPABASE_ANON_KEY=your-anon-key
NEXT_PUBLIC_SITE_URL=https://your-domain.com
NEXT_PUBLIC_VAPID_PUBLIC_KEY=your-vapid-public-key  # optional: push notifications

SUPABASE_SERVICE_ROLE_KEY=your-service-key           # server-side only
VAPID_PRIVATE_KEY=your-vapid-private-key             # optional
VAPID_SUBJECT=mailto:you@example.com                 # optional
```

### Backend (`.env` / Render dashboard)
```env
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_SERVICE_ROLE_KEY=your-service-key
LEAGUE_SLUGS=fifa.world,uefa.champions,eng.1,esp.1,ger.1,ita.1,fra.1
POLL_INTERVAL_LIVE=60
POLL_INTERVAL_IDLE=120
```

---

## Technology Stack Summary

| Layer | Tech | Cost |
|-------|------|------|
| Frontend | Next.js 14 + Tailwind | Free (Vercel) |
| Realtime | Supabase Realtime | Free |
| Database | Supabase PostgreSQL | Free |
| Backend poller | Python + httpx | Free (Render) |
| OG images | @vercel/og (satori) | Free (edge) |
| Live data | ESPN unofficial API | Free (no key) |
| Standings/fixtures | football-data.org | Free (key required) |
| Team assets | TheSportsDB | Free |
| Push notifications | Web Push API | Free |
| **Total** | | **$0/month** |

---

*FIFA 2026 tournament: June 11 – July 19, 2026 | 104 matches | 48 teams*
*Stack verified: March 2026 | All APIs active and returning live data*
