# FIFA 2026 Live Engine — Project Memory
_Updated: 2026-03-05 | Running context for Claude sessions_

---

## What This Project Is

A **free, public, no-login website** for FIFA World Cup 2026 live scores and viral content.
Not a SaaS. Not a paid product (yet). A public utility that proves content delivery works.

Tournament: **June 11 – July 19, 2026** (104 matches, 48 teams).

---

## Architecture Decisions (locked)

| Decision | Choice | Reason |
|----------|--------|--------|
| Data source | ESPN unofficial API (no key) | Free, ~30s update, no rate limit documented |
| Realtime | Supabase Realtime postgres_changes | Free, no WS server to manage |
| Poller host | Render.com free worker | 750 hrs/month = always-on |
| Frontend host | Vercel free | Edge CDN, Next.js native |
| OG images | @vercel/og (satori) | Serverless, no Chromium, edge-cached |
| Push notifications | Web Push API | No app, no login, browser-native |
| Personalization | localStorage | No auth needed |
| Auth (future only) | Clerk | 30-min setup when needed |

---

## File Structure

```
C:/Dev/Fifa/
├── README.md              ← SINGLE SOURCE OF TRUTH (merged from 3 docs)
├── .claude/
│   └── MEMORY.md          ← this file
├── frontend/              ← Next.js 14 (deploy: Vercel)
│   ├── app/
│   │   ├── page.tsx       ← live dashboard
│   │   ├── match/[id]/    ← SEO match pages
│   │   ├── api/live/      ← 30s cached ESPN proxy
│   │   ├── api/og/[id]/   ← @vercel/og dynamic images
│   │   ├── api/push/      ← Web Push subscribe + notify
│   │   └── api/health/
│   ├── components/        ← MatchCard, GoalAlert, EventFeed, etc.
│   └── lib/               ← types.ts, espn.ts, supabase.ts
├── backend/               ← Python poller (deploy: Render.com)
│   ├── poller.py          ← async ESPN poll + Supabase write
│   ├── requirements.txt   ← httpx, python-dotenv, supabase
│   └── render.yaml        ← free worker config
└── supabase/
    └── schema.sql         ← matches + match_events + push_subs + RLS
```

Old files `Fifa.md`, `BLUEPRINT.md`, `MVP_IMPLEMENTATION.md` → merged into `README.md`, can be deleted.

---

## Key Technical Facts

- **ESPN league slugs**: `fifa.world`, `uefa.champions`, `eng.1`, `esp.1`, `ger.1`, `ita.1`, `fra.1`
- **ESPN base URL**: `https://site.api.espn.com/apis/site/v2/sports/soccer`
- **Goal detection**: in-memory state diff in `poller.py` (`_state` dict: match_id → scores)
- **Event dedup**: SHA-256 of `match_id:event_type:score` → 16-char hex ID (safe to re-insert)
- **Supabase free tier limits**: 200 concurrent Realtime connections, 500 MB storage
- **Render free tier**: 750 hrs/month (sufficient for always-on worker, no cold starts for workers)
- **OG images**: edge-cached by Vercel automatically after first request per match ID

---

## What's Built vs Pending

### Built (files exist)
- [x] `backend/poller.py` — full async poller with event detection
- [x] `backend/render.yaml` + `requirements.txt` + `Dockerfile`
- [x] `supabase/schema.sql` — DDL with RLS, indexes, push_subscriptions table
- [x] `frontend/lib/types.ts` — Match, MatchEvent, LiveResponse
- [x] `frontend/lib/espn.ts` — fetchAllLiveMatches, transformEvent, detectGoals
- [x] `frontend/lib/supabase.ts` — browser client, fetchRecentEvents, subscribeToEvents
- [x] `frontend/app/api/live/route.ts`
- [x] `frontend/app/api/events/route.ts`
- [x] `frontend/app/api/health/route.ts`
- [x] `frontend/app/page.tsx` — main dashboard with polling + goal alerts
- [x] `frontend/components/MatchCard.tsx`
- [x] `frontend/components/MatchGrid.tsx`
- [x] `frontend/components/GoalAlert.tsx`
- [x] `frontend/components/EventFeed.tsx`
- [x] `frontend/components/StatComparison.tsx`
- [x] `frontend/components/LiveTicker.tsx`
- [x] `frontend/tailwind.config.ts` — custom pitch/live/goal colors + animations

### Pending (documented in README.md, not yet coded)
- [ ] `frontend/app/match/[id]/page.tsx` — SSR match pages with JSON-LD
- [ ] `frontend/app/api/og/[id]/route.tsx` — @vercel/og dynamic images
- [ ] `frontend/app/api/push/subscribe/route.ts` — Web Push subscription
- [ ] `frontend/app/api/push/notify/route.ts` — trigger push notifications
- [ ] `frontend/app/sitemap.ts` — dynamic sitemap from Supabase
- [ ] `frontend/components/ShareButton.tsx` — copy link + Twitter intent
- [ ] `frontend/components/PushOptIn.tsx` — team-specific push opt-in
- [ ] `frontend/lib/preferences.ts` — localStorage team follow
- [ ] `public/sw.js` — service worker for push
- [ ] `public/manifest.json` — PWA manifest
- [ ] Team and group pages (`/team/[slug]`, `/group/[letter]`)

---

## User Preferences (this project)

- Keep everything **free tier** — no paid APIs, no paid hosting
- **Website-only** focus — not a creator SaaS tool (that's future)
- No login, no subscription on the public site
- Use `@vercel/og` for social images (not Puppeteer/FFmpeg at this stage)
- Gemini MCP (`gemini-2.5-pro`) for research/architecture questions
- DeepSeek for algorithm-heavy coding tasks

---

## Gemini Insights (2026-03-05)

Key findings from Gemini consultation on this project:

1. **OG images**: Use `@vercel/og` (satori) — runs as Edge Function, no Chrome, cached automatically
2. **SEO**: Dynamic routes `/match/[id]` critical — current SPA structure unindexable
3. **JSON-LD**: `SportsEvent` schema on match pages → Google Rich Results (score in SERP)
4. **LiveBlogPosting**: Add to match pages to signal real-time content to Google
5. **Web Push**: Best retention tool for a no-login site; team-specific opt-in converts better than generic
6. **LocalStorage personalization**: "Follow team" pattern creates stickiness without auth
7. **Emoji reactions via Supabase Realtime broadcast**: Community feel without DB storage
8. **Supabase Realtime 200 connection limit**: 200 concurrent browser tabs (not users — tabs)
9. **Dynamic sitemap**: Must auto-generate from matches table; submit to GSC on first data

---

## Environment Variables

### Frontend (Vercel)
- `NEXT_PUBLIC_SUPABASE_URL`
- `NEXT_PUBLIC_SUPABASE_ANON_KEY`
- `NEXT_PUBLIC_SITE_URL`
- `NEXT_PUBLIC_VAPID_PUBLIC_KEY` (optional, for push)
- `SUPABASE_SERVICE_ROLE_KEY` (server-side only)
- `VAPID_PRIVATE_KEY` + `VAPID_SUBJECT` (optional)

### Backend (Render)
- `SUPABASE_URL`
- `SUPABASE_SERVICE_ROLE_KEY`
- `LEAGUE_SLUGS` (default: 7 leagues)
- `POLL_INTERVAL_LIVE` (default: 60s)
- `POLL_INTERVAL_IDLE` (default: 120s)

---

## Next Session Starting Points

If continuing this project, prioritize in order:
1. Build `app/match/[id]/page.tsx` — unlocks SEO and OG images
2. Build `app/api/og/[id]/route.tsx` — viral sharing requires this
3. Test poller locally against a live match (verify <10s latency)
4. Build `app/sitemap.ts` — submit to Google Search Console ASAP
5. Add Web Push (`sw.js` + subscribe endpoint + PushOptIn component)
