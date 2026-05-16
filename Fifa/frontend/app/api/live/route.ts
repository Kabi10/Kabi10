import { NextResponse } from 'next/server'
import { fetchAllLiveMatches } from '@/lib/espn'
import { fetchRecentEvents } from '@/lib/supabase'
import type { LiveResponse } from '@/lib/types'

// Revalidate every 30 seconds via Next.js route segment config
export const revalidate = 30
export const dynamic = 'force-dynamic'

export async function GET() {
  try {
    const [matches, events] = await Promise.all([
      fetchAllLiveMatches(),
      fetchRecentEvents(30),
    ])

    const response: LiveResponse = {
      matches,
      events,
      lastUpdated: new Date().toISOString(),
      source: 'espn-unofficial',
    }

    return NextResponse.json(response, {
      headers: {
        // Allow client-side polling; CDN caches for 20s
        'Cache-Control': 'public, s-maxage=20, stale-while-revalidate=30',
      },
    })
  } catch (error) {
    console.error('[/api/live] Error:', error)
    return NextResponse.json(
      { matches: [], events: [], lastUpdated: new Date().toISOString(), source: 'error' },
      { status: 500 },
    )
  }
}
