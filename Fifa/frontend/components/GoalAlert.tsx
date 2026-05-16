'use client'

import { useEffect, useState, useCallback } from 'react'
import Image from 'next/image'
import { clsx } from 'clsx'
import type { Match } from '@/lib/types'

export interface GoalAlertData {
  id: string
  match: Match
  team: 'home' | 'away'
  scorerLabel: string
  detectedAt: number
}

interface GoalAlertProps {
  alerts: GoalAlertData[]
  onDismiss: (id: string) => void
}

/** Stack of goal toast notifications (top-right corner) */
export function GoalAlertStack({ alerts, onDismiss }: GoalAlertProps) {
  return (
    <div
      className="fixed top-4 right-4 z-50 flex flex-col gap-3 pointer-events-none"
      aria-live="assertive"
      aria-label="Goal alerts"
    >
      {alerts.slice(0, 3).map(alert => (
        <GoalToast key={alert.id} alert={alert} onDismiss={onDismiss} />
      ))}
    </div>
  )
}

function GoalToast({
  alert,
  onDismiss,
}: {
  alert: GoalAlertData
  onDismiss: (id: string) => void
}) {
  const [exiting, setExiting] = useState(false)

  const team = alert.team === 'home' ? alert.match.homeTeam : alert.match.awayTeam
  const homeScore = alert.match.homeScore
  const awayScore = alert.match.awayScore
  const minute = alert.match.clock

  const dismiss = useCallback(() => {
    setExiting(true)
    setTimeout(() => onDismiss(alert.id), 300)
  }, [alert.id, onDismiss])

  // Auto-dismiss after 8 seconds
  useEffect(() => {
    const timer = setTimeout(dismiss, 8000)
    return () => clearTimeout(timer)
  }, [dismiss])

  return (
    <div
      className={clsx(
        'pointer-events-auto w-80 rounded-2xl overflow-hidden shadow-2xl shadow-black/60',
        'border border-goal/40',
        exiting ? 'animate-slide-out-right' : 'animate-slide-in-right',
      )}
    >
      {/* Gold header bar */}
      <div className="bg-goal px-4 py-2 flex items-center justify-between">
        <div className="flex items-center gap-2">
          <GoalBallIcon />
          <span className="text-black font-black text-sm tracking-widest">G O A L !</span>
        </div>
        <span className="text-black/60 font-semibold text-xs">{minute}</span>
      </div>

      {/* Body */}
      <div className="bg-pitch-800 px-4 py-3">
        <div className="flex items-center gap-3 mb-3">
          {/* Scoring team logo */}
          <div className="relative w-8 h-8 shrink-0">
            {team.logo ? (
              <Image src={team.logo} alt={team.name} fill sizes="32px" className="object-contain" />
            ) : (
              <div className="w-8 h-8 rounded-full bg-white/10" />
            )}
          </div>
          <div>
            <p className="text-goal font-black text-sm leading-tight">{team.name}</p>
            <p className="text-white/40 text-xs">{alert.scorerLabel}</p>
          </div>
        </div>

        {/* Scoreline */}
        <div className="flex items-center justify-between bg-pitch-900 rounded-xl px-3 py-2">
          <div className="flex items-center gap-2">
            <div className="relative w-5 h-5">
              {alert.match.homeTeam.logo && (
                <Image
                  src={alert.match.homeTeam.logo}
                  alt={alert.match.homeTeam.shortName}
                  fill sizes="20px" className="object-contain"
                />
              )}
            </div>
            <span className="text-white/60 text-xs">{alert.match.homeTeam.shortName}</span>
          </div>

          <span className="text-white font-black text-xl tabular-nums">
            {homeScore} – {awayScore}
          </span>

          <div className="flex items-center gap-2">
            <span className="text-white/60 text-xs">{alert.match.awayTeam.shortName}</span>
            <div className="relative w-5 h-5">
              {alert.match.awayTeam.logo && (
                <Image
                  src={alert.match.awayTeam.logo}
                  alt={alert.match.awayTeam.shortName}
                  fill sizes="20px" className="object-contain"
                />
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Auto-dismiss progress bar */}
      <ProgressBar duration={8000} />
    </div>
  )
}

function GoalBallIcon() {
  return (
    <svg className="w-4 h-4 text-black" viewBox="0 0 24 24" fill="currentColor">
      <circle cx="12" cy="12" r="10" />
      <path
        d="M12 2a10 10 0 1 0 0 20A10 10 0 0 0 12 2zM9.5 7l2.5 1.8L14.5 7l.9 3-2.4 1.7H11L8.6 10 9.5 7zm1.2 9.8V14l-1.7-1.2.7-2.3H14.3l.7 2.3-1.7 1.2v2.8a7.9 7.9 0 0 1-2.6 0z"
        fill="white"
        opacity="0.4"
      />
    </svg>
  )
}

function ProgressBar({ duration }: { duration: number }) {
  return (
    <div className="h-0.5 bg-pitch-700">
      <div
        className="h-full bg-goal"
        style={{
          animation: `shrink ${duration}ms linear forwards`,
        }}
      />
      <style>{`
        @keyframes shrink {
          from { width: 100%; }
          to   { width: 0%; }
        }
      `}</style>
    </div>
  )
}
