import type {
  Match,
  MatchEvent,
  MatchStats,
  MatchStatus,
  EspnEvent,
  EspnCompetition,
  EspnCompetitor,
} from './types'

// ── ESPN unofficial API base ─────────────────────────────────────────────────
const ESPN_BASE = 'https://site.api.espn.com/apis/site/v2/sports/soccer'

// Leagues tried in priority order — falls back to whichever has live matches
export const LEAGUE_SLUGS = [
  'fifa.world',   // FIFA World Cup 2026
  'uefa.champions', // UEFA Champions League
  'eng.1',        // Premier League
  'esp.1',        // La Liga
  'ger.1',        // Bundesliga
  'ita.1',        // Serie A
  'fra.1',        // Ligue 1
]

// ── Fetch scoreboard ─────────────────────────────────────────────────────────

export async function fetchScoreboard(
  league: string,
  revalidate = 30,
): Promise<EspnEvent[]> {
  const url = `${ESPN_BASE}/${league}/scoreboard`
  const res = await fetch(url, {
    next: { revalidate },
    headers: { 'User-Agent': 'Mozilla/5.0 (compatible)' },
  })
  if (!res.ok) return []
  const data = await res.json()
  return (data.events as EspnEvent[]) ?? []
}

/** Fetch all live matches across all configured leagues */
export async function fetchAllLiveMatches(): Promise<Match[]> {
  const results = await Promise.allSettled(
    LEAGUE_SLUGS.map(slug => fetchScoreboard(slug)),
  )

  const all: Match[] = []
  for (const r of results) {
    if (r.status === 'fulfilled') {
      all.push(...r.value.map(transformEvent).filter(Boolean) as Match[])
    }
  }

  // Sort: live matches first, then by tournament
  return all.sort((a, b) => {
    const order = { in: 0, halftime: 1, pre: 2, post: 3, delayed: 4 }
    return (order[a.status] ?? 5) - (order[b.status] ?? 5)
  })
}

/** Fetch matches for a single league slug */
export async function fetchLeagueMatches(league: string): Promise<Match[]> {
  const events = await fetchScoreboard(league)
  return events.map(transformEvent).filter(Boolean) as Match[]
}

// ── Transform ESPN event → internal Match ───────────────────────────────────

export function transformEvent(event: EspnEvent): Match | null {
  const competition = event.competitions?.[0]
  if (!competition) return null

  const home = competition.competitors.find(c => c.homeAway === 'home')
  const away = competition.competitors.find(c => c.homeAway === 'away')
  if (!home || !away) return null

  const status = mapStatus(competition.status.type.name)
  const clock = competition.status.type.shortDetail || ''
  const minute = parseMinute(clock)

  // Derive tournament name from league header or event
  const tournament =
    competition.league?.name ??
    event.season?.slug?.replace(/-/g, ' ').toUpperCase() ??
    'Football'

  return {
    id: event.id,
    homeTeam: {
      id: home.team.id,
      name: home.team.displayName,
      shortName: home.team.shortDisplayName,
      logo: home.team.logo,
      color: home.team.color ? `#${home.team.color}` : undefined,
    },
    awayTeam: {
      id: away.team.id,
      name: away.team.displayName,
      shortName: away.team.shortDisplayName,
      logo: away.team.logo,
      color: away.team.color ? `#${away.team.color}` : undefined,
    },
    homeScore: parseInt(home.score ?? '0', 10),
    awayScore: parseInt(away.score ?? '0', 10),
    status,
    clock,
    minute,
    tournament,
    tournamentLogo: competition.league?.logos?.[0]?.href,
    venue: competition.venue?.fullName,
    updatedAt: new Date().toISOString(),
  }
}

// ── Parse match stats ─────────────────────────────────────────────────────────

export function extractStats(
  competition: EspnCompetition,
  matchId: string,
): MatchStats | null {
  const home = competition.competitors.find(c => c.homeAway === 'home')
  const away = competition.competitors.find(c => c.homeAway === 'away')
  if (!home?.statistics || !away?.statistics) return null

  const getStat = (competitor: EspnCompetitor, name: string): number => {
    const stat = competitor.statistics?.find(
      s => s.name.toLowerCase() === name.toLowerCase(),
    )
    return parseFloat(stat?.displayValue?.replace('%', '') ?? '0') || 0
  }

  return {
    matchId,
    possession: [getStat(home, 'possessionPct'), getStat(away, 'possessionPct')],
    shots: [getStat(home, 'totalShots'), getStat(away, 'totalShots')],
    shotsOnTarget: [getStat(home, 'shotsOnTarget'), getStat(away, 'shotsOnTarget')],
    corners: [getStat(home, 'cornerKicks'), getStat(away, 'cornerKicks')],
    fouls: [getStat(home, 'fouls'), getStat(away, 'fouls')],
    yellowCards: [getStat(home, 'yellowCards'), getStat(away, 'yellowCards')],
    redCards: [getStat(home, 'redCards'), getStat(away, 'redCards')],
  }
}

// ── Helpers ──────────────────────────────────────────────────────────────────

function mapStatus(espnName: string): MatchStatus {
  switch (espnName) {
    case 'STATUS_IN_PROGRESS': return 'in'
    case 'STATUS_HALFTIME':    return 'halftime'
    case 'STATUS_FINAL':
    case 'STATUS_FULL_TIME':   return 'post'
    case 'STATUS_SCHEDULED':
    case 'STATUS_POSTPONED':
    default:                   return 'pre'
  }
}

function parseMinute(clock: string): number {
  const match = clock.match(/^(\d+)/)
  return match ? parseInt(match[1], 10) : 0
}

// ── Detect new events by diffing previous vs current scores ──────────────────

export function detectGoals(
  prev: Match[],
  curr: Match[],
): Array<{ match: Match; team: 'home' | 'away'; scorerLabel: string }> {
  const goals: Array<{ match: Match; team: 'home' | 'away'; scorerLabel: string }> = []

  for (const current of curr) {
    const previous = prev.find(m => m.id === current.id)
    if (!previous) continue
    if (current.status === 'pre') continue

    if (current.homeScore > previous.homeScore) {
      goals.push({ match: current, team: 'home', scorerLabel: current.homeTeam.shortName })
    }
    if (current.awayScore > previous.awayScore) {
      goals.push({ match: current, team: 'away', scorerLabel: current.awayTeam.shortName })
    }
  }

  return goals
}
