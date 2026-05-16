'use client'

import Image from 'next/image'
import { clsx } from 'clsx'
import { formatDistanceToNow } from 'date-fns'
import type { MatchEvent, EventType } from '@/lib/types'

interface EventFeedProps {
  events: MatchEvent[]
  className?: string
}

export function EventFeed({ events, className }: EventFeedProps) {
  if (events.length === 0) {
    return (
      <div className={clsx('text-center py-12', className)}>
        <p className="text-white/30 text-sm">Watching for live events…</p>
        <p className="text-white/20 text-xs mt-1">Goals and cards will appear here instantly</p>
      </div>
    )
  }

  return (
    <div className={clsx('space-y-1', className)}>
      {events.map((event, i) => (
        <EventRow key={event.id} event={event} isLatest={i === 0} />
      ))}
    </div>
  )
}

function EventRow({
  event,
  isLatest,
}: {
  event: MatchEvent
  isLatest: boolean
}) {
  const config = EVENT_CONFIG[event.eventType] ?? EVENT_CONFIG.goal

  return (
    <div
      className={clsx(
        'flex items-start gap-3 rounded-xl px-3 py-2.5 transition-colors',
        isLatest ? 'bg-pitch-700 animate-fade-in' : 'hover:bg-pitch-800',
      )}
    >
      {/* Icon badge */}
      <div
        className={clsx(
          'shrink-0 w-7 h-7 rounded-full flex items-center justify-center text-sm mt-0.5',
          config.badgeCls,
        )}
      >
        {config.icon}
      </div>

      {/* Content */}
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2 flex-wrap">
          {/* Team logo */}
          {event.teamLogo && (
            <div className="relative w-4 h-4 shrink-0">
              <Image
                src={event.teamLogo}
                alt={event.teamName}
                fill
                sizes="16px"
                className="object-contain"
              />
            </div>
          )}
          <span className={clsx('text-sm font-bold', config.textCls)}>
            {config.label}
          </span>
          {event.clockLabel && (
            <span className="text-xs font-bold text-white/40 tabular-nums">
              {event.clockLabel}
            </span>
          )}
        </div>

        {/* Player name */}
        {event.playerName && (
          <p className="text-white/80 text-sm font-medium truncate">
            {event.playerName}
          </p>
        )}

        {/* Match context */}
        <p className="text-white/40 text-xs truncate mt-0.5">
          {event.homeTeam} {event.homeScore} – {event.awayScore} {event.awayTeam}
        </p>
      </div>

      {/* Time ago */}
      <span className="text-white/25 text-xs shrink-0 mt-0.5 tabular-nums">
        {formatDistanceToNow(new Date(event.createdAt), { addSuffix: false })
          .replace('about ', '')
          .replace(' minutes', 'm')
          .replace(' minute', 'm')
          .replace(' hours', 'h')
          .replace(' hour', 'h')
          .replace(' seconds', 's')
          .replace('less than a', '<1')
        }
      </span>
    </div>
  )
}

// ── Event display config ──────────────────────────────────────────────────────

const EVENT_CONFIG: Record<
  EventType,
  { icon: string; label: string; badgeCls: string; textCls: string }
> = {
  goal: {
    icon: '⚽',
    label: 'GOAL',
    badgeCls: 'bg-goal/20 text-goal',
    textCls: 'text-goal',
  },
  own_goal: {
    icon: '⚽',
    label: 'OWN GOAL',
    badgeCls: 'bg-danger/20 text-danger',
    textCls: 'text-danger',
  },
  penalty_goal: {
    icon: '⚽',
    label: 'PENALTY',
    badgeCls: 'bg-goal/20 text-goal',
    textCls: 'text-goal',
  },
  penalty_miss: {
    icon: '✗',
    label: 'MISS',
    badgeCls: 'bg-danger/20 text-danger',
    textCls: 'text-danger',
  },
  red_card: {
    icon: '🟥',
    label: 'RED CARD',
    badgeCls: 'bg-danger/20',
    textCls: 'text-danger',
  },
  yellow_card: {
    icon: '🟨',
    label: 'YELLOW',
    badgeCls: 'bg-yellow-500/20',
    textCls: 'text-yellow-400',
  },
  second_yellow: {
    icon: '🟨🟥',
    label: '2ND YELLOW',
    badgeCls: 'bg-danger/20',
    textCls: 'text-danger',
  },
  substitution: {
    icon: '↕',
    label: 'SUB',
    badgeCls: 'bg-blue-500/20 text-blue-400',
    textCls: 'text-blue-400',
  },
  ht: {
    icon: '⏸',
    label: 'HALF TIME',
    badgeCls: 'bg-white/10 text-white/50',
    textCls: 'text-white/50',
  },
  ft: {
    icon: '✓',
    label: 'FULL TIME',
    badgeCls: 'bg-live/20 text-live',
    textCls: 'text-live',
  },
  var_goal: {
    icon: '📺',
    label: 'VAR GOAL',
    badgeCls: 'bg-goal/20 text-goal',
    textCls: 'text-goal',
  },
}
