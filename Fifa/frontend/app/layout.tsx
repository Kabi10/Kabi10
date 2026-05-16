import type { Metadata, Viewport } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'FIFA 2026 Live — Instant Match Updates',
  description:
    'Real-time FIFA World Cup 2026 scores, goal alerts, and match stats. No login required.',
  keywords: ['FIFA 2026', 'World Cup live', 'football scores', 'live goals', 'soccer results'],
  openGraph: {
    title: 'FIFA 2026 Live — Instant Match Updates',
    description: 'Real-time scores, goal alerts, and stats. Auto-updating every 30 seconds.',
    type: 'website',
    locale: 'en_US',
  },
  twitter: {
    card: 'summary_large_image',
    title: 'FIFA 2026 Live',
    description: 'Real-time World Cup scores and goal alerts',
  },
  robots: 'index, follow',
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  themeColor: '#04080f',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className="dark">
      <body className="min-h-screen bg-pitch-950 text-white antialiased pitch-texture">
        {children}
      </body>
    </html>
  )
}
