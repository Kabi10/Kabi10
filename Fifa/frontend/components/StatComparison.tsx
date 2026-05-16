'use client'

import { clsx } from 'clsx'
import type { Match, MatchStats } from '@/lib/types'

interface StatComparisonProps {
  match: Match
  stats?: MatchStats
  className?: string
}

export function StatComparison({ match, stats, className }: StatComparisonProps) {
  if (!stats) {
    return (
      <div className={clsx('bg-pitch-800 rounded-2xl border border-white/8 p-5', className)}>
        <h3 className="text-white/60 text-xs font-semibold uppercase tracking-wider mb-4">
          Match Stats
        </h3>
        <p className="text-white/30 text-sm text-center py-4">
          Stats available during live matches
        </p>
      </div>
    )
  }

  const rows: StatRow[] = [
    {
      label: 'Possession',
      home: stats.possession[0],
      away: stats.possession[1],
      suffix: '%',
      format: v => `${v}%`,
    },
    {
      label: 'Shots',
      home: stats.shots[0],
      away: stats.shots[1],
    },
    {
      label: 'On Target',
      home: stats.shotsOnTarget[0],
      away: stats.shotsOnTarget[1],
    },
    {
      label: 'Corners',
      home: stats.corners[0],
      away: stats.corners[1],
    },
    {
      label: 'Fouls',
      home: stats.fouls[0],
      away: stats.fouls[1],
      inverse: true, // lower is better
    },
    ...(stats.xg
      ? [{ label: 'xG', home: stats.xg[0], away: stats.xg[1], format: (v: number) => v.toFixed(1), highlight: true }]
      : []),
  ]

  return (
    <div className={clsx('bg-pitch-800 rounded-2xl border border-white/8 p-5', className)}>
      {/* Header */}
      <div className="flex items-center justify-between mb-5">
        <h3 className="text-white/60 text-xs font-semibold uppercase tracking-wider">
          Match Stats
        </h3>
        <span className="text-white/30 text-xs">
          {match.homeTeam.shortName} vs {match.awayTeam.shortName}
        </span>
      </div>

      {/* Stat rows */}
      <div className="space-y-4">
        {rows.map(row => (
          <StatRow
            key={row.label}
            row={row}
            homeColor={match.homeTeam.color}
            awayColor={match.awayTeam.color}
          />
        ))}
      </div>
    </div>
  )
}

// ── StatRow ───────────────────────────────────────────────────────────────────

interface StatRow {
  label: string
  home: number
  away: number
  suffix?: string
  format?: (v: number) => string
  inverse?: boolean
  highlight?: boolean
}

function StatRow({
  row,
  homeColor,
  awayColor,
}: {
  row: StatRow
  homeColor?: string
  awayColor?: string
}) {
  const total = row.home + row.away || 1
  const homePct = (row.home / total) * 100
  const awayPct = (row.away / total) * 100

  const homeWins = row.inverse
    ? row.home <= row.away
    : row.home >= row.away

  const format = row.format ?? ((v: number) => String(v))

  return (
    <div>
      {/* Label + values */}
      <div className="flex items-center justify-between mb-1.5">
        <span
          className={clsx(
            'text-sm font-bold tabular-nums',
            homeWins ? 'text-white' : 'text-white/40',
          )}
        >
          {format(row.home)}
        </span>

        <span
          className={clsx(
            'text-xs font-medium',
            row.highlight ? 'text-goal' : 'text-white/40',
          )}
        >
          {row.label}
        </span>

        <span
          className={clsx(
            'text-sm font-bold tabular-nums',
            !homeWins ? 'text-white' : 'text-white/40',
          )}
        >
          {format(row.away)}
        </span>
      </div>

      {/* Dual bar */}
      <div className="flex h-1.5 rounded-full overflow-hidden gap-0.5">
        <div
          className="rounded-l-full transition-all duration-700"
          style={{
            width: `${homePct}%`,
            backgroundColor: homeColor ?? '#3b82f6',
          }}
        />
        <div
          className="rounded-r-full transition-all duration-700 ml-auto"
          style={{
            width: `${awayPct}%`,
            backgroundColor: awayColor ?? '#ef4444',
          }}
        />
      </div>
    </div>
  )
}
