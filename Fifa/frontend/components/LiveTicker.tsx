'use client'

import { clsx } from 'clsx'
import type { Match } from '@/lib/types'

interface LiveTickerProps {
  matches: Match[]
  className?: string
}

/** Horizontal scrolling ticker showing live scores */
export function LiveTicker({ matches, className }: LiveTickerProps) {
  const live = matches.filter(m => m.status === 'in' || m.status === 'halftime')

  if (live.length === 0) return null

  // Duplicate items for seamless loop
  const items = [...live, ...live]

  return (
    <div
      className={clsx(
        'overflow-hidden bg-pitch-900 border-y border-white/6',
        className,
      )}
    >
      <div className="flex items-center h-9">
        {/* "LIVE" label */}
        <div className="shrink-0 flex items-center gap-1.5 px-3 border-r border-white/10 h-full bg-live/10">
          <span className="w-1.5 h-1.5 rounded-full bg-live animate-pulse-live" />
          <span className="text-live text-xs font-black tracking-widest">LIVE</span>
        </div>

        {/* Scrolling matches */}
        <div className="overflow-hidden flex-1">
          <div
            className="flex items-center gap-6 whitespace-nowrap"
            style={{ animation: `ticker ${live.length * 6}s linear infinite` }}
          >
            {items.map((match, i) => (
              <TickerItem key={`${match.id}-${i}`} match={match} />
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}

function TickerItem({ match }: { match: Match }) {
  const isHT = match.status === 'halftime'

  return (
    <span className="flex items-center gap-2 text-xs">
      <span className="text-white/60 font-medium">{match.homeTeam.shortName}</span>
      <span className="text-white font-black tabular-nums">
        {match.homeScore} – {match.awayScore}
      </span>
      <span className="text-white/60 font-medium">{match.awayTeam.shortName}</span>
      <span
        className={clsx(
          'font-bold',
          isHT ? 'text-yellow-400' : 'text-live',
        )}
      >
        {isHT ? 'HT' : match.clock}
      </span>
      <span className="text-white/15 mx-1">·</span>
    </span>
  )
}
