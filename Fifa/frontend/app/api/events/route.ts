import { NextRequest, NextResponse } from 'next/server'
import { fetchRecentEvents } from '@/lib/supabase'

export const dynamic = 'force-dynamic'

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url)
  const limit = Math.min(parseInt(searchParams.get('limit') ?? '20', 10), 50)

  try {
    const events = await fetchRecentEvents(limit)
    return NextResponse.json(
      { events, count: events.length },
      { headers: { 'Cache-Control': 'no-store' } },
    )
  } catch (error) {
    console.error('[/api/events] Error:', error)
    return NextResponse.json({ events: [], count: 0 }, { status: 500 })
  }
}
