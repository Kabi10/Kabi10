'use client'

import { useState, useEffect, useCallback, useRef } from 'react'
import { clsx } from 'clsx'
import { MatchGrid }     from '@/components/MatchGrid'
import { EventFeed }     from '@/components/EventFeed'
import { LiveTicker }    from '@/components/LiveTicker'
import { GoalAlertStack, type GoalAlertData } from '@/components/GoalAlert'
import { StatComparison } from '@/components/StatComparison'
import { detectGoals }   from '@/lib/espn'
import { subscribeToEvents } from '@/lib/supabase'
import type { Match, MatchEvent, LiveResponse } from '@/lib/types'

// How often to poll the API (ms)
const POLL_INTERVAL_LIVE = 30_000  // 30s when matches are live
const POLL_INTERVAL_IDLE = 60_000  // 60s when no live matches

export default function HomePage() {
  const [matches, setMatches]         = useState<Match[]>([])
  const [events, setEvents]           = useState<MatchEvent[]>([])
  const [alerts, setAlerts]           = useState<GoalAlertData[]>([])
  const [newMatchIds, setNewMatchIds] = useState<Set<string>>(new Set())
  const [selectedMatch, setSelectedMatch] = useState<Match | null>(null)
  const [lastUpdated, setLastUpdated] = useState<Date | null>(null)
  const [loading, setLoading]         = useState(true)
  const [error, setError]             = useState<string | null>(null)

  const prevMatchesRef = useRef<Match[]>([])
  const pollTimerRef   = useRef<ReturnType<typeof setTimeout>>()

  // ── Fetch latest data from our API route ──────────────────────────────────

  const fetchData = useCallback(async () => {
    try {
      const res = await fetch('/api/live', { cache: 'no-store' })
      if (!res.ok) throw new Error(`HTTP ${res.status}`)

      const data: LiveResponse = await res.json()

      // Detect goals by diffing previous state
      const prev = prevMatchesRef.current
      if (prev.length > 0) {
        const goals = detectGoals(prev, data.matches)
        if (goals.length > 0) {
          const newAlerts: GoalAlertData[] = goals.map(g => ({
            id: `${g.match.id}-${Date.now()}-${g.team}`,
            match: g.match,
            team: g.team,
            scorerLabel: g.scorerLabel,
            detectedAt: Date.now(),
          }))
          setAlerts(prev => [...newAlerts, ...prev].slice(0, 5))

          // Flash the match cards that changed
          const changed = new Set(goals.map(g => g.match.id))
          setNewMatchIds(changed)
          setTimeout(() => setNewMatchIds(new Set()), 4000)
        }
      }

      prevMatchesRef.current = data.matches
      setMatches(data.matches)
      setEvents(data.events)
      setLastUpdated(new Date())
      setError(null)
    } catch (err) {
      console.error('Fetch error:', err)
      setError('Unable to reach live data. Retrying…')
    } finally {
      setLoading(false)
    }
  }, [])

  // ── Polling loop ──────────────────────────────────────────────────────────

  const schedulePoll = useCallback(() => {
    clearTimeout(pollTimerRef.current)
    const hasLive = prevMatchesRef.current.some(
      m => m.status === 'in' || m.status === 'halftime',
    )
    const interval = hasLive ? POLL_INTERVAL_LIVE : POLL_INTERVAL_IDLE
    pollTimerRef.current = setTimeout(async () => {
      await fetchData()
      schedulePoll()
    }, interval)
  }, [fetchData])

  useEffect(() => {
    fetchData().then(schedulePoll)
    return () => clearTimeout(pollTimerRef.current)
  }, [fetchData, schedulePoll])

  // ── Supabase Realtime (if configured) ────────────────────────────────────

  useEffect(() => {
    const unsubscribe = subscribeToEvents(event => {
      setEvents(prev => [event, ...prev].slice(0, 30))

      // Show goal alert for Supabase real-time goal events
      if (event.eventType === 'goal' || event.eventType === 'penalty_goal') {
        const match = prevMatchesRef.current.find(m => m.id === event.matchId)
        if (match) {
          const alert: GoalAlertData = {
            id: event.id,
            match: {
              ...match,
              homeScore: event.homeScore,
              awayScore: event.awayScore,
            },
            team: event.teamName === match.homeTeam.name ? 'home' : 'away',
            scorerLabel: event.playerName ?? event.teamName,
            detectedAt: Date.now(),
          }
          setAlerts(prev => [alert, ...prev].slice(0, 5))
        }
      }
    })
    return unsubscribe
  }, [])

  // ── Keep selectedMatch in sync with latest data ────────────────────────────

  useEffect(() => {
    if (selectedMatch) {
      const updated = matches.find(m => m.id === selectedMatch.id)
      if (updated) setSelectedMatch(updated)
    }
  }, [matches]) // eslint-disable-line react-hooks/exhaustive-deps

  const liveCount = matches.filter(m => m.status === 'in' || m.status === 'halftime').length

  // ── Render ────────────────────────────────────────────────────────────────

  return (
    <>
      {/* Goal alert toasts */}
      <GoalAlertStack
        alerts={alerts}
        onDismiss={id => setAlerts(prev => prev.filter(a => a.id !== id))}
      />

      {/* Live ticker bar */}
      <LiveTicker matches={matches} className="ticker-wrap" />

      {/* Main layout */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        {/* Header */}
        <header className="flex items-start justify-between gap-4 mb-8">
          <div>
            <div className="flex items-center gap-3 mb-1">
              <h1 className="text-2xl sm:text-3xl font-black tracking-tight">
                <span className="text-gradient-gold">FIFA 2026</span>
                <span className="text-white"> Live</span>
              </h1>
              {liveCount > 0 && (
                <span className="flex items-center gap-1.5 bg-live/15 border border-live/30 text-live text-xs font-bold px-2.5 py-1 rounded-full">
                  <span className="w-1.5 h-1.5 rounded-full bg-live animate-pulse-live" />
                  {liveCount} LIVE
                </span>
              )}
            </div>
            <p className="text-white/40 text-sm">
              Auto-updates every 30 seconds · No login required
            </p>
          </div>

          <div className="text-right shrink-0">
            {lastUpdated && (
              <p className="text-white/30 text-xs">
                Updated{' '}
                <span className="tabular-nums">
                  {lastUpdated.toLocaleTimeString([], {
                    hour: '2-digit',
                    minute: '2-digit',
                    second: '2-digit',
                  })}
                </span>
              </p>
            )}
            <RefreshButton onClick={fetchData} loading={loading} />
          </div>
        </header>

        {/* Error banner */}
        {error && (
          <div className="mb-4 px-4 py-3 bg-danger/10 border border-danger/30 rounded-xl text-danger text-sm">
            {error}
          </div>
        )}

        {/* Main grid */}
        <div className="flex flex-col lg:flex-row gap-6">
          {/* Left: matches (takes most space) */}
          <div className="flex-1 min-w-0">
            {loading && matches.length === 0 ? (
              <SkeletonGrid />
            ) : (
              <MatchGrid
                matches={matches}
                newMatchIds={newMatchIds}
                onMatchClick={setSelectedMatch}
              />
            )}
          </div>

          {/* Right sidebar: stats + event feed */}
          <aside className="w-full lg:w-80 xl:w-96 shrink-0 space-y-4">
            {/* Selected match stats */}
            {selectedMatch && (
              <div className="animate-fade-in">
                <div className="flex items-center justify-between mb-2 px-1">
                  <h3 className="text-white/60 text-xs font-bold uppercase tracking-wider">
                    {selectedMatch.homeTeam.shortName} vs {selectedMatch.awayTeam.shortName}
                  </h3>
                  <button
                    onClick={() => setSelectedMatch(null)}
                    className="text-white/30 hover:text-white/60 text-xs"
                  >
                    ✕
                  </button>
                </div>
                <StatComparison match={selectedMatch} />
              </div>
            )}

            {/* Event feed */}
            <div className="bg-pitch-800 rounded-2xl border border-white/8 overflow-hidden">
              <div className="px-4 py-3 border-b border-white/6 flex items-center justify-between">
                <h2 className="text-white/80 text-sm font-bold uppercase tracking-wider">
                  Live Events
                </h2>
                {events.length > 0 && (
                  <span className="text-white/30 text-xs tabular-nums">
                    {events.length} events
                  </span>
                )}
              </div>
              <div className="max-h-[600px] overflow-y-auto p-2">
                <EventFeed events={events} />
              </div>
            </div>

            {/* Info card */}
            <div className="rounded-2xl border border-white/6 p-4 bg-pitch-800/50">
              <p className="text-white/40 text-xs leading-relaxed">
                <span className="text-white/60 font-semibold">How it works:</span> This page
                polls live match data every 30 seconds and shows goal alerts instantly.
                Click any match card to see detailed stats.
              </p>
              <div className="mt-3 flex flex-wrap gap-1.5">
                {['Premier League', 'Champions League', 'La Liga', 'Bundesliga', 'Serie A', 'FIFA 2026'].map(l => (
                  <span key={l} className="text-xs bg-white/5 text-white/30 px-2 py-0.5 rounded-full">
                    {l}
                  </span>
                ))}
              </div>
            </div>
          </aside>
        </div>
      </div>

      {/* Footer */}
      <footer className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 mt-8 border-t border-white/6">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-3 text-white/25 text-xs">
          <p>⚽ FIFA 2026 Live Engine · Powered by public sports data APIs</p>
          <p>Data refreshes every 30s · Not affiliated with FIFA</p>
        </div>
      </footer>
    </>
  )
}

// ── Sub-components ────────────────────────────────────────────────────────────

function RefreshButton({
  onClick,
  loading,
}: {
  onClick: () => void
  loading: boolean
}) {
  return (
    <button
      onClick={onClick}
      disabled={loading}
      className={clsx(
        'mt-1 text-xs px-3 py-1.5 rounded-lg border transition-all',
        'border-white/10 text-white/40 hover:text-white/70 hover:border-white/20',
        loading && 'opacity-50 cursor-not-allowed',
      )}
    >
      {loading ? (
        <span className="flex items-center gap-1.5">
          <svg className="w-3 h-3 animate-spin" viewBox="0 0 24 24" fill="none">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.37 0 0 5.37 0 12h4z" />
          </svg>
          Updating…
        </span>
      ) : (
        '↻ Refresh'
      )}
    </button>
  )
}

function SkeletonGrid() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-3">
      {Array.from({ length: 6 }).map((_, i) => (
        <div
          key={i}
          className="rounded-2xl bg-pitch-800 border border-white/6 h-36 shimmer"
          style={{ animationDelay: `${i * 0.1}s` }}
        />
      ))}
    </div>
  )
}
