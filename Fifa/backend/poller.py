"""
FIFA 2026 Micro-Content Engine — Match Poller
============================================
Polls ESPN unofficial API every 60 seconds during live match windows.
Detects score changes and writes events to Supabase.

Deploy to: Render.com free tier (750 hrs/month)
Run:       python poller.py
"""

import asyncio
import hashlib
import json
import logging
import os
import sys
from datetime import datetime, timezone
from typing import Optional

import httpx
from dotenv import load_dotenv

load_dotenv()

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
    handlers=[logging.StreamHandler(sys.stdout)],
)
logger = logging.getLogger("poller")

# ── Config ────────────────────────────────────────────────────────────────────

ESPN_BASE = "https://site.api.espn.com/apis/site/v2/sports/soccer"

# Leagues to monitor (ordered by priority)
LEAGUE_SLUGS: list[str] = os.getenv(
    "LEAGUE_SLUGS",
    "fifa.world,uefa.champions,eng.1,esp.1,ger.1,ita.1,fra.1",
).split(",")

POLL_INTERVAL_LIVE = int(os.getenv("POLL_INTERVAL_LIVE", "60"))   # seconds when live matches
POLL_INTERVAL_IDLE = int(os.getenv("POLL_INTERVAL_IDLE", "120"))  # seconds when no live matches

SUPABASE_URL = os.getenv("SUPABASE_URL", "")
SUPABASE_KEY = os.getenv("SUPABASE_SERVICE_ROLE_KEY", "")

# In-memory state: match_id → {home_score, away_score}
_state: dict[str, dict] = {}

# ── Supabase client ────────────────────────────────────────────────────────────

class SupabaseClient:
    def __init__(self, url: str, key: str):
        self.url = url.rstrip("/")
        self.headers = {
            "apikey": key,
            "Authorization": f"Bearer {key}",
            "Content-Type": "application/json",
            "Prefer": "return=minimal",
        }
        self._enabled = bool(url and key)

    def enabled(self) -> bool:
        return self._enabled

    async def upsert_match(self, client: httpx.AsyncClient, match: dict) -> None:
        if not self._enabled:
            return
        try:
            r = await client.post(
                f"{self.url}/rest/v1/matches",
                headers={**self.headers, "Prefer": "resolution=merge-duplicates"},
                json=match,
                timeout=10,
            )
            r.raise_for_status()
        except Exception as e:
            logger.warning(f"Supabase upsert_match error: {e}")

    async def insert_event(self, client: httpx.AsyncClient, event: dict) -> None:
        if not self._enabled:
            logger.info(f"[no-db] Event: {event.get('event_type')} | {event.get('home_team')} {event.get('home_score')}-{event.get('away_score')} {event.get('away_team')}")
            return
        try:
            r = await client.post(
                f"{self.url}/rest/v1/match_events",
                headers=self.headers,
                json=event,
                timeout=10,
            )
            r.raise_for_status()
            logger.info(
                f"Inserted event: {event['event_type']} | "
                f"{event['home_team']} {event['home_score']}-"
                f"{event['away_score']} {event['away_team']}"
            )
        except Exception as e:
            logger.warning(f"Supabase insert_event error: {e}")


supabase = SupabaseClient(SUPABASE_URL, SUPABASE_KEY)

# ── ESPN fetcher ───────────────────────────────────────────────────────────────

async def fetch_scoreboard(
    client: httpx.AsyncClient,
    league: str,
) -> list[dict]:
    url = f"{ESPN_BASE}/{league}/scoreboard"
    try:
        r = await client.get(url, timeout=15, headers={"User-Agent": "Mozilla/5.0"})
        if r.status_code != 200:
            return []
        data = r.json()
        return data.get("events", [])
    except Exception as e:
        logger.debug(f"fetch_scoreboard [{league}] error: {e}")
        return []


async def fetch_all_events(client: httpx.AsyncClient) -> list[dict]:
    """Fetch events from all configured leagues in parallel."""
    results = await asyncio.gather(
        *[fetch_scoreboard(client, slug) for slug in LEAGUE_SLUGS],
        return_exceptions=True,
    )
    all_events: list[dict] = []
    for r in results:
        if isinstance(r, list):
            all_events.extend(r)
    return all_events

# ── Match parsing ──────────────────────────────────────────────────────────────

def parse_match(event: dict) -> Optional[dict]:
    """Extract flat match dict from ESPN event object."""
    try:
        comp = event["competitions"][0]
        competitors = comp["competitors"]
        home = next(c for c in competitors if c["homeAway"] == "home")
        away = next(c for c in competitors if c["homeAway"] == "away")
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
            "tournament": (
                comp.get("league", {}).get("name")
                or event.get("season", {}).get("slug", "Football")
            ),
            "venue": comp.get("venue", {}).get("fullName", ""),
            "updated_at": datetime.now(timezone.utc).isoformat(),
        }
    except (KeyError, StopIteration, ValueError) as e:
        logger.debug(f"parse_match error for event {event.get('id')}: {e}")
        return None


def _map_status(espn_status: str) -> str:
    return {
        "STATUS_IN_PROGRESS": "in",
        "STATUS_HALFTIME":    "halftime",
        "STATUS_FINAL":       "post",
        "STATUS_FULL_TIME":   "post",
        "STATUS_SCHEDULED":   "pre",
        "STATUS_POSTPONED":   "delayed",
    }.get(espn_status, "pre")

# ── Event detection ────────────────────────────────────────────────────────────

def make_event_id(match_id: str, event_type: str, score: str) -> str:
    raw = f"{match_id}:{event_type}:{score}"
    return hashlib.sha256(raw.encode()).hexdigest()[:16]


def detect_and_build_events(match: dict) -> list[dict]:
    """
    Compare current match state to stored state.
    Returns list of new events (goals, HT, FT).
    """
    mid = match["id"]
    prev = _state.get(mid)
    curr_home = match["home_score"]
    curr_away = match["away_score"]
    curr_status = match["status"]

    new_events: list[dict] = []

    if prev is None:
        # First time seeing this match — store state, no events
        _state[mid] = {
            "home_score": curr_home,
            "away_score": curr_away,
            "status": curr_status,
        }
        return []

    prev_home = prev["home_score"]
    prev_away = prev["away_score"]
    prev_status = prev["status"]

    # Detect goals (home)
    goals_home = curr_home - prev_home
    for _ in range(max(goals_home, 0)):
        score_after_h = prev_home + _ + 1
        score_after_a = curr_away
        event_id = make_event_id(mid, "goal_home", f"{score_after_h}{score_after_a}")
        new_events.append({
            "id": event_id,
            "match_id": mid,
            "event_type": "goal",
            "team_name": match["home_team"],
            "team_logo": match["home_team_logo"],
            "home_team": match["home_team"],
            "away_team": match["away_team"],
            "home_score": score_after_h,
            "away_score": score_after_a,
            "clock_label": match["clock"],
            "tournament": match["tournament"],
            "created_at": datetime.now(timezone.utc).isoformat(),
        })

    # Detect goals (away)
    goals_away = curr_away - prev_away
    for _ in range(max(goals_away, 0)):
        score_after_h = curr_home
        score_after_a = prev_away + _ + 1
        event_id = make_event_id(mid, "goal_away", f"{score_after_h}{score_after_a}")
        new_events.append({
            "id": event_id,
            "match_id": mid,
            "event_type": "goal",
            "team_name": match["away_team"],
            "team_logo": match["away_team_logo"],
            "home_team": match["home_team"],
            "away_team": match["away_team"],
            "home_score": score_after_h,
            "away_score": score_after_a,
            "clock_label": match["clock"],
            "tournament": match["tournament"],
            "created_at": datetime.now(timezone.utc).isoformat(),
        })

    # Detect HT transition
    if prev_status == "in" and curr_status == "halftime":
        event_id = make_event_id(mid, "ht", f"{curr_home}{curr_away}")
        new_events.append({
            "id": event_id,
            "match_id": mid,
            "event_type": "ht",
            "team_name": match["home_team"],
            "home_team": match["home_team"],
            "away_team": match["away_team"],
            "home_score": curr_home,
            "away_score": curr_away,
            "clock_label": "HT",
            "tournament": match["tournament"],
            "created_at": datetime.now(timezone.utc).isoformat(),
        })

    # Detect FT transition
    if prev_status in ("in", "halftime") and curr_status == "post":
        event_id = make_event_id(mid, "ft", f"{curr_home}{curr_away}")
        new_events.append({
            "id": event_id,
            "match_id": mid,
            "event_type": "ft",
            "team_name": match["home_team"],
            "home_team": match["home_team"],
            "away_team": match["away_team"],
            "home_score": curr_home,
            "away_score": curr_away,
            "clock_label": "FT",
            "tournament": match["tournament"],
            "created_at": datetime.now(timezone.utc).isoformat(),
        })

    # Update state
    _state[mid] = {
        "home_score": curr_home,
        "away_score": curr_away,
        "status": curr_status,
    }

    return new_events

# ── Main poll cycle ────────────────────────────────────────────────────────────

async def poll_cycle(client: httpx.AsyncClient) -> bool:
    """
    Run one poll cycle.
    Returns True if any live matches were found.
    """
    events = await fetch_all_events(client)
    if not events:
        logger.debug("No events from any league")
        return False

    matches = [m for e in events if (m := parse_match(e)) is not None]
    live_count = sum(1 for m in matches if m["status"] in ("in", "halftime"))

    logger.info(f"Fetched {len(matches)} matches ({live_count} live)")

    tasks = []
    for match in matches:
        tasks.append(supabase.upsert_match(client, match))
        new_events = detect_and_build_events(match)
        for ev in new_events:
            tasks.append(supabase.insert_event(client, ev))

    if tasks:
        await asyncio.gather(*tasks, return_exceptions=True)

    return live_count > 0


async def main() -> None:
    logger.info("=" * 60)
    logger.info("FIFA 2026 Match Poller starting…")
    logger.info(f"Leagues: {', '.join(LEAGUE_SLUGS)}")
    logger.info(f"Supabase: {'connected' if supabase.enabled() else 'DISABLED (log-only mode)'}")
    logger.info("=" * 60)

    limits = httpx.Limits(max_keepalive_connections=10, max_connections=20)
    async with httpx.AsyncClient(limits=limits, follow_redirects=True) as client:
        while True:
            try:
                has_live = await poll_cycle(client)
                interval = POLL_INTERVAL_LIVE if has_live else POLL_INTERVAL_IDLE
                logger.debug(f"Next poll in {interval}s")
                await asyncio.sleep(interval)
            except asyncio.CancelledError:
                logger.info("Poller cancelled — shutting down")
                break
            except Exception as e:
                logger.error(f"Unexpected error in poll cycle: {e}", exc_info=True)
                await asyncio.sleep(30)  # Back off on unexpected error


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Stopped by user")
