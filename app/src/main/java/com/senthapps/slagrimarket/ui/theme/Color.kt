package com.senthapps.slagrimarket.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// AGRIMARKET INDUSTRIAL COLOR SYSTEM
// Brutal, high-contrast, machinery-inspired palette
// ============================================================================

// CORE PALETTE - Pure, uncompromising colors
val AgrimarketWhite = Color(0xFFFFFFFF)          // Pure white background
val AgrimarketOffWhite = Color(0xFFF5F5F5)       // Alternate background (alternating rows)
val AgrimarketBlack = Color(0xFF000000)          // Pure black text and borders
val AgrimarketNearBlack = Color(0xFF1A1A1A)      // Near-black text variant

// ACTION COLORS - Functional, not decorative
val AgrimarketOrange = Color(0xFFE65100)         // Primary action (deep orange/amber)
val AgrimarketRed = Color(0xFFC41E3A)            // Destructive action (deep red)

// SECONDARY TEXT - Gray but visible in sunlight
val AgrimarketGray = Color(0xFF757575)           // Secondary text, units, timestamps

// Legacy compatibility (keep existing color names for gradual migration)
val Green600 = Color(0xFF16a34a)
val Blue400 = Color(0xFF60a5fa)
val Blue600 = Color(0xFF2563eb)
val Error600 = AgrimarketRed
val ErrorRed = AgrimarketRed
val Gray900 = AgrimarketNearBlack
val Gray800 = AgrimarketNearBlack
val Gray700 = Color(0xFF374151)
val Gray600 = Color(0xFF4b5563)
val Gray500 = Color(0xFF6b7280)
val Gray400 = Color(0xFF9ca3af)
val Gray300 = Color(0xFFd1d5db)
val Gray200 = Color(0xFFe5e7eb)
val Gray100 = Color(0xFFf3f4f6)
val Gray50 = AgrimarketOffWhite
val BackgroundLight = AgrimarketWhite
val BackgroundDark = AgrimarketNearBlack
val SurfaceLight = AgrimarketWhite
val SurfaceDark = AgrimarketNearBlack

// Warning colors for backward compatibility
val Warning500 = Color(0xFFf59e0b)
val YellowWarning = Warning500

// Success colors for backward compatibility
val Success600 = Color(0xFF16a34a)

// Surface elevation colors (all same as white - no elevation)
val SurfaceElevation1 = AgrimarketWhite
val SurfaceElevation2 = AgrimarketWhite
val SurfaceElevation3 = AgrimarketWhite
val SurfaceElevation4 = AgrimarketWhite
val SurfaceElevation5 = AgrimarketWhite

// Gradient brushes for backward compatibility (all set to solid colors)
val DarkGradientBackground = Brush.verticalGradient(listOf(AgrimarketWhite, AgrimarketWhite))
val HeroGradientBackground = Brush.verticalGradient(listOf(AgrimarketWhite, AgrimarketWhite))
val PrimaryGradient = Brush.horizontalGradient(listOf(AgrimarketWhite, AgrimarketWhite))
val SecondaryGradient = Brush.horizontalGradient(listOf(AgrimarketWhite, AgrimarketWhite))
val CardGradientOverlay = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
val CardElevationGradient = Brush.verticalGradient(listOf(AgrimarketWhite, AgrimarketWhite))