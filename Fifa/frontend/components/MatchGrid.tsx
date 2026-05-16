'use client'

import { useMemo } from 'react'
import { clsx } from 'clsx'
import { MatchCard } from './MatchCard'
import type { Match } from '@/lib/types'

interface MatchGridProps {
  matches: Match[]
  newMatchIds?: Set<string>
  onMatchClick?: (match: Match) => void
  className?: string
}

export function MatchGrid({
  matches,
  newMatchIds = new Set(),
  onMatchClick,
  className,
}: MatchGridProps) {
  const grouped = useMemo(() => groupByStatus(matches), [matches])

  if (matches.length === 0) {
    return (
      <div className={clsx('text-center py-16', className)}>
        <div className="text-5xl mb-4">⚽</div>
        <p className="text-white/40 text-sm">No matches found right now</p>
        <p className="text-white/25 text-xs mt-1">
          Checking Premier League, La Liga, Champions League and more…
        </p>
      </div>
    )
  }

  return (
    <div className={className}>
      {/* Live matches */}
      {grouped.live.length > 0 && (
        <Section
          title="Live"
          badge={grouped.live.length}
          badgeCls="bg-live/20 text-live"
          pulse
        >
          <Grid>
            {grouped.live.map(m => (
              <MatchCard
                key={m.id}
                match={m}
                isNew={newMatchIds.has(m.id)}
                onClick={onMatchClick}
              />
            ))}
          </Grid>
        </Section>
      )}

      {/* Halftime */}
      {grouped.halftime.length > 0 && (
        <Section title="Half Time" badge={grouped.halftime.length} badgeCls="bg-yellow-500/20 text-yellow-400">
          <Grid>
            {grouped.halftime.map(m => (
              <MatchCard key={m.id} match={m} onClick={onMatchClick} />
            ))}
          </Grid>
        </Section>
      )}

      {/* Upcoming */}
      {grouped.pre.length > 0 && (
        <Section title="Upcoming" badge={grouped.pre.length} badgeCls="bg-white/10 text-white/40">
          <Grid>
            {grouped.pre.map(m => (
              <MatchCard key={m.id} match={m} onClick={onMatchClick} />
            ))}
          </Grid>
        </Section>
      )}

      {/* Finished */}
      {grouped.post.length > 0 && (
        <Section title="Finished" badge={grouped.post.length} badgeCls="bg-white/5 text-white/30">
          <Grid>
            {grouped.post.map(m => (
              <MatchCard key={m.id} match={m} onClick={onMatchClick} />
            ))}
          </Grid>
        </Section>
      )}
    </div>
  )
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function groupByStatus(matches: Match[]) {
  return {
    live:     matches.filter(m => m.status === 'in'),
    halftime: matches.filter(m => m.status === 'halftime'),
    pre:      matches.filter(m => m.status === 'pre'),
    post:     matches.filter(m => m.status === 'post'),
  }
}

function Section({
  title,
  badge,
  badgeCls,
  pulse,
  children,
}: {
  title: string
  badge: number
  badgeCls: string
  pulse?: boolean
  children: React.ReactNode
}) {
  return (
    <div className="mb-8">
      <div className="flex items-center gap-2 mb-4">
        {pulse && <span className="w-2 h-2 rounded-full bg-live animate-pulse-live" />}
        <h2 className="text-white/80 text-sm font-bold uppercase tracking-wider">{title}</h2>
        <span className={clsx('text-xs font-bold px-2 py-0.5 rounded-full', badgeCls)}>
          {badge}
        </span>
      </div>
      {children}
    </div>
  )
}

function Grid({ children }: { children: React.ReactNode }) {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
      {children}
    </div>
  )
}
