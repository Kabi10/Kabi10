-- FIFA 2026 Micro-Content Engine — Supabase Schema
-- Run this in: Supabase Dashboard → SQL Editor → New query → Run
-- Or via: supabase db push (if using Supabase CLI)

-- ── Extensions ────────────────────────────────────────────────────────────────

create extension if not exists "uuid-ossp";

-- ── matches ───────────────────────────────────────────────────────────────────
-- One row per match; upserted on every poll cycle.

create table if not exists matches (
  id                text        primary key,           -- ESPN event ID
  home_team         text        not null,
  away_team         text        not null,
  home_team_short   text        not null default '',
  home_team_logo    text        not null default '',
  away_team_logo    text        not null default '',
  home_score        integer     not null default 0,
  away_score        integer     not null default 0,
  status            text        not null default 'pre', -- pre | in | halftime | post | delayed
  clock             text        not null default '',
  tournament        text        not null default '',
  venue             text        not null default '',
  updated_at        timestamptz not null default now()
);

-- Index for fast live-match queries
create index if not exists matches_status_idx on matches (status);
create index if not exists matches_updated_idx on matches (updated_at desc);

-- ── match_events ──────────────────────────────────────────────────────────────
-- Immutable event log; one row per detected goal/HT/FT.
-- id is a SHA-256 fingerprint (first 16 hex chars) — safe to re-insert.

create table if not exists match_events (
  id           text        primary key,               -- fingerprint: sha256(match_id:event_type:score)[:16]
  match_id     text        not null references matches(id) on delete cascade,
  event_type   text        not null,                  -- goal | ht | ft
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

-- Indexes for feed queries
create index if not exists events_match_idx      on match_events (match_id);
create index if not exists events_created_idx    on match_events (created_at desc);
create index if not exists events_type_idx       on match_events (event_type);

-- ── Row Level Security ────────────────────────────────────────────────────────
-- Public read (no auth required — this is a public website).
-- Write restricted to service role key (used by the Python poller).

alter table matches      enable row level security;
alter table match_events enable row level security;

-- Anyone can read
create policy "public read matches"
  on matches for select
  using (true);

create policy "public read events"
  on match_events for select
  using (true);

-- Only service role can write (poller uses SUPABASE_SERVICE_ROLE_KEY)
-- Service role bypasses RLS by default — no extra policy needed.

-- ── Realtime ──────────────────────────────────────────────────────────────────
-- Enable Realtime for the frontend subscription.
-- Run these in the Supabase Dashboard → Database → Replication tab,
-- OR uncomment and run here (requires pg_publication to exist):

-- alter publication supabase_realtime add table match_events;
-- alter publication supabase_realtime add table matches;

-- ── Helper view ───────────────────────────────────────────────────────────────
-- Convenience view: recent goals only, newest first.

create or replace view recent_goals as
  select *
  from   match_events
  where  event_type in ('goal', 'own_goal', 'penalty_goal', 'var_goal')
  order  by created_at desc
  limit  50;
