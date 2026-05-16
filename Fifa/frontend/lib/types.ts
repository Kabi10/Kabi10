// ── Core match types ────────────────────────────────────────────────────────

export type MatchStatus = 'pre' | 'in' | 'post' | 'halftime' | 'delayed'

export interface Team {
  id: string
  name: string
  shortName: string
  logo: string
  color?: string
}

export interface Match {
  id: string
  homeTeam: Team
  awayTeam: Team
  homeScore: number
  awayScore: number
  status: MatchStatus
  /** Display clock string: "73'", "HT", "FT", "45+2'" */
  clock: string
  minute: number
  tournament: string
  tournamentLogo?: string
  venue?: string
  /** ISO timestamp of last data update */
  updatedAt: string
}

// ── Event types ──────────────────────────────────────────────────────────────

export type EventType =
  | 'goal'
  | 'own_goal'
  | 'penalty_goal'
  | 'penalty_miss'
  | 'red_card'
  | 'yellow_card'
  | 'second_yellow'
  | 'substitution'
  | 'ht'
  | 'ft'
  | 'var_goal'

export interface MatchEvent {
  id: string
  matchId: string
  eventType: EventType
  /** Team the event belongs to */
  teamName: string
  teamLogo?: string
  playerName?: string
  assistName?: string
  minute?: number
  clockLabel?: string
  homeTeam: string
  awayTeam: string
  homeScore: number
  awayScore: number
  tournament: string
  createdAt: string
}

// ── Stats ────────────────────────────────────────────────────────────────────

export interface MatchStats {
  matchId: string
  /** [home, away] */
  possession: [number, number]
  shots: [number, number]
  shotsOnTarget: [number, number]
  corners: [number, number]
  fouls: [number, number]
  yellowCards: [number, number]
  redCards: [number, number]
  /** xG if available */
  xg?: [number, number]
}

// ── API response shapes ──────────────────────────────────────────────────────

export interface LiveResponse {
  matches: Match[]
  events: MatchEvent[]
  lastUpdated: string
  source: string
}

// ── ESPN raw types (internal) ─────────────────────────────────────────────────

export interface EspnEvent {
  id: string
  name: string
  shortName: string
  competitions: EspnCompetition[]
  season?: { slug: string; year: number }
}

export interface EspnCompetition {
  id: string
  competitors: EspnCompetitor[]
  status: EspnStatus
  venue?: { fullName: string }
  league?: { name: string; logos?: Array<{ href: string }> }
  details?: EspnDetail[]
}

export interface EspnCompetitor {
  id: string
  homeAway: 'home' | 'away'
  score: string
  winner?: boolean
  team: {
    id: string
    displayName: string
    shortDisplayName: string
    logo: string
    alternateColor?: string
    color?: string
  }
  statistics?: Array<{ name: string; displayValue: string }>
}

export interface EspnStatus {
  clock: number
  displayClock: string
  period: number
  type: {
    id: string
    name: string
    state: 'pre' | 'in' | 'post'
    completed: boolean
    description: string
    detail: string
    shortDetail: string
  }
}

export interface EspnDetail {
  athletesInvolved?: Array<{ displayName: string; position?: string }>
  team?: { id: string }
  type?: { id: string; text: string }
  clock?: { displayValue: string }
  scoringPlay?: boolean
}
