import { createClient } from '@supabase/supabase-js'
import type { MatchEvent } from './types'

// ── Client (browser-safe, uses anon key) ─────────────────────────────────────

const supabaseUrl = process.env.NEXT_PUBLIC_SUPABASE_URL ?? ''
const supabaseAnonKey = process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY ?? ''

// Singleton — only created when env vars are present
export const supabase =
  supabaseUrl && supabaseAnonKey
    ? createClient(supabaseUrl, supabaseAnonKey)
    : null

// ── Server-only client (uses service role) ───────────────────────────────────

export function createServerClient() {
  const url = process.env.SUPABASE_URL ?? process.env.NEXT_PUBLIC_SUPABASE_URL ?? ''
  const key = process.env.SUPABASE_SERVICE_ROLE_KEY ?? process.env.NEXT_PUBLIC_SUPABASE_ANON_KEY ?? ''
  if (!url || !key) return null
  return createClient(url, key)
}

// ── Fetch recent events ───────────────────────────────────────────────────────

export async function fetchRecentEvents(limit = 20): Promise<MatchEvent[]> {
  if (!supabase) {
    // No Supabase configured — return empty (MVP fallback: client tracks goals in state)
    return []
  }

  const { data, error } = await supabase
    .from('match_events')
    .select('*')
    .order('created_at', { ascending: false })
    .limit(limit)

  if (error) {
    console.error('Supabase fetch error:', error.message)
    return []
  }

  return (data ?? []).map(row => ({
    id: row.id,
    matchId: row.match_id,
    eventType: row.event_type,
    teamName: row.team_name,
    teamLogo: row.team_logo,
    playerName: row.player_name,
    minute: row.minute,
    clockLabel: row.clock_label,
    homeTeam: row.home_team,
    awayTeam: row.away_team,
    homeScore: row.home_score,
    awayScore: row.away_score,
    tournament: row.tournament,
    createdAt: row.created_at,
  })) as MatchEvent[]
}

// ── Subscribe to real-time events ─────────────────────────────────────────────

export function subscribeToEvents(
  onEvent: (event: MatchEvent) => void,
) {
  if (!supabase) return () => {}

  const channel = supabase
    .channel('match_events_live')
    .on(
      'postgres_changes',
      { event: 'INSERT', schema: 'public', table: 'match_events' },
      payload => {
        const row = payload.new as Record<string, unknown>
        onEvent({
          id: row.id as string,
          matchId: row.match_id as string,
          eventType: row.event_type as MatchEvent['eventType'],
          teamName: row.team_name as string,
          teamLogo: row.team_logo as string | undefined,
          playerName: row.player_name as string | undefined,
          minute: row.minute as number | undefined,
          clockLabel: row.clock_label as string | undefined,
          homeTeam: row.home_team as string,
          awayTeam: row.away_team as string,
          homeScore: row.home_score as number,
          awayScore: row.away_score as number,
          tournament: row.tournament as string,
          createdAt: row.created_at as string,
        })
      },
    )
    .subscribe()

  // Return cleanup function
  return () => supabase.removeChannel(channel)
}
