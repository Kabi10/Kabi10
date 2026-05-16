'use client'

import Image from 'next/image'
import { clsx } from 'clsx'
import type { Match } from '@/lib/types'

interface MatchCardProps {
  match: Match
  /** Whether this match had a recent score change (triggers highlight) */
  isNew?: boolean
  onClick?: (match: Match) => void
}

export function MatchCard({ match, isNew, onClick }: MatchCardProps) {
  const isLive = match.status === 'in'
  const isHT   = match.status === 'halftime'
  const isFT   = match.status === 'post'
  const isPre  = match.status === 'pre'

  const statusBadge = isLive
    ? { label: match.clock, cls: 'bg-live/15 text-live border border-live/30' }
    : isHT
    ? { label: 'HT', cls: 'bg-yellow-500/15 text-yellow-400 border border-yellow-500/30' }
    : isFT
    ? { label: 'FT', cls: 'bg-white/10 text-white/50 border border-white/10' }
    : { label: 'PRE', cls: 'bg-white/5 text-white/30 border border-white/10' }

  return (
    <article
      onClick={() => onClick?.(match)}
      className={clsx(
        'relative rounded-2xl border transition-all duration-300 overflow-hidden',
        'cursor-pointer select-none',
        isNew
          ? 'border-goal/50 bg-goal/5 animate-goal-flash'
          : 'border-white/8 bg-pitch-800 hover:bg-pitch-700 hover:border-white/15',
        onClick && 'hover:scale-[1.01] active:scale-[0.99]',
      )}
    >
      {/* Live pulsing bar at top */}
      {isLive && (
        <div className="absolute top-0 left-0 right-0 h-0.5 bg-live animate-pulse-live" />
      )}

      <div className="px-5 py-4">
        {/* Tournament + status row */}
        <div className="flex items-center justify-between mb-4">
          <span className="text-xs text-white/40 font-medium truncate max-w-[140px]">
            {match.tournament}
          </span>
          <span className={clsx('text-xs font-bold px-2 py-0.5 rounded-full', statusBadge.cls)}>
            {isLive && (
              <span className="inline-block w-1.5 h-1.5 rounded-full bg-live mr-1.5 animate-pulse-live" />
            )}
            {statusBadge.label}
          </span>
        </div>

        {/* Score row */}
        <div className="flex items-center justify-between gap-3">
          {/* Home team */}
          <TeamBlock
            name={match.homeTeam.name}
            logo={match.homeTeam.logo}
            score={match.homeScore}
            align="left"
            isWinner={isFT && match.homeScore > match.awayScore}
          />

          {/* Divider */}
          <div className="flex flex-col items-center shrink-0">
            {isPre ? (
              <span className="text-white/30 text-sm font-medium">vs</span>
            ) : (
              <div className="flex items-center gap-2">
                <ScoreDigit value={match.homeScore} isActive={isLive} />
                <span className="text-white/20 text-xl font-light">–</span>
                <ScoreDigit value={match.awayScore} isActive={isLive} />
              </div>
            )}
          </div>

          {/* Away team */}
          <TeamBlock
            name={match.awayTeam.name}
            logo={match.awayTeam.logo}
            score={match.awayScore}
            align="right"
            isWinner={isFT && match.awayScore > match.homeScore}
          />
        </div>

        {/* Venue */}
        {match.venue && (
          <p className="text-center text-xs text-white/25 mt-3 truncate">
            {match.venue}
          </p>
        )}
      </div>
    </article>
  )
}

// ── Sub-components ────────────────────────────────────────────────────────────

function TeamBlock({
  name, logo, score, align, isWinner,
}: {
  name: string
  logo: string
  score: number
  align: 'left' | 'right'
  isWinner: boolean
}) {
  return (
    <div
      className={clsx(
        'flex flex-col items-center gap-2 w-20',
        align === 'right' && 'items-center',
      )}
    >
      <div className="relative w-10 h-10">
        {logo ? (
          <Image
            src={logo}
            alt={name}
            fill
            sizes="40px"
            className="object-contain"
            onError={(e) => {
              (e.target as HTMLImageElement).style.display = 'none'
            }}
          />
        ) : (
          <div className="w-10 h-10 rounded-full bg-white/10 flex items-center justify-center">
            <span className="text-xs text-white/40">{name.slice(0, 2)}</span>
          </div>
        )}
      </div>
      <span
        className={clsx(
          'text-xs font-semibold text-center leading-tight',
          isWinner ? 'text-white' : 'text-white/60',
        )}
      >
        {name.length > 12 ? name.slice(0, 12) + '…' : name}
      </span>
    </div>
  )
}

function ScoreDigit({ value, isActive }: { value: number; isActive: boolean }) {
  return (
    <span
      className={clsx(
        'text-3xl font-black tabular-nums leading-none',
        isActive ? 'text-white' : 'text-white/70',
      )}
    >
      {value}
    </span>
  )
}
