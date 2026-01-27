package com.senthapps.slagrimarket.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// HUMAN INDUSTRIAL DESIGN SYSTEM v1.0
// "A marketplace that lives on a phone"
// ============================================================================

// PRIMARY PALETTE - The colors of Sri Lankan agriculture
object HumanIndustrial {
    // Primary Colors
    val Earth = Color(0xFF8B4513)      // Terracotta - borders, headers, accents
    val Gold = Color(0xFFB8860B)       // Harvest gold - buttons, prices, success
    val Green = Color(0xFF2D5016)      // Field green - confirmed, vegetables
    val Rice = Color(0xFFFAF6F1)       // Warm off-white - backgrounds
    val Ink = Color(0xFF1A1A1A)        // Near-black - primary text

    // Secondary Colors
    val Stone = Color(0xFF6B6B6B)      // Secondary text, metadata, timestamps
    val Dust = Color(0xFFE8E4DF)       // Dividers, alternate rows, disabled
    val Urgent = Color(0xFFA63D2F)     // Errors, cancellations only

    // Category-specific backgrounds
    val VegetablesBackground = Green   // #2D5016
    val FruitsBackground = Gold        // #B8860B
    val GrainsBackground = Color(0xFFD4A84B)  // Lighter gold
    val LivestockBackground = Earth    // #8B4513
}

// NOTE: Spacing values are defined in Shape.kt as part of the Spacing object
// Use Spacing.xs, Spacing.sm, Spacing.md, Spacing.lg, Spacing.xl with .dp extension

// Border widths
object Borders {
    const val standard = 2   // Default border width
    const val tile = 4       // Tiles and headers
    const val accent = 4     // Left accent bars on list items
}

// Touch target sizes
object TouchTargets {
    const val minimum = 48   // Minimum touch target
    const val button = 56    // Full-width buttons
}

// Convenience aliases for existing code compatibility
val AgrimarketWhite = HumanIndustrial.Rice
val AgrimarketOffWhite = HumanIndustrial.Dust
val AgrimarketBlack = HumanIndustrial.Ink
val AgrimarketNearBlack = HumanIndustrial.Ink
val AgrimarketOrange = HumanIndustrial.Gold  // Primary action now Gold
val AgrimarketRed = HumanIndustrial.Urgent
val AgrimarketGray = HumanIndustrial.Stone

// Legacy compatibility (keep existing color names for gradual migration)
val Green600 = HumanIndustrial.Green
val Blue400 = HumanIndustrial.Gold
val Blue600 = HumanIndustrial.Earth
val Error600 = HumanIndustrial.Urgent
val ErrorRed = HumanIndustrial.Urgent
val Gray900 = HumanIndustrial.Ink
val Gray800 = HumanIndustrial.Ink
val Gray700 = Color(0xFF374151)
val Gray600 = HumanIndustrial.Stone
val Gray500 = HumanIndustrial.Stone
val Gray400 = Color(0xFF9ca3af)
val Gray300 = HumanIndustrial.Dust
val Gray200 = HumanIndustrial.Dust
val Gray100 = HumanIndustrial.Rice
val Gray50 = HumanIndustrial.Rice
val BackgroundLight = HumanIndustrial.Rice
val BackgroundDark = HumanIndustrial.Ink
val SurfaceLight = HumanIndustrial.Rice
val SurfaceDark = HumanIndustrial.Ink

// Warning colors for backward compatibility
val Warning500 = HumanIndustrial.Gold
val YellowWarning = HumanIndustrial.Gold

// Success colors for backward compatibility
val Success600 = HumanIndustrial.Green

// Surface elevation colors (all same as Rice - no elevation)
val SurfaceElevation1 = HumanIndustrial.Rice
val SurfaceElevation2 = HumanIndustrial.Rice
val SurfaceElevation3 = HumanIndustrial.Rice
val SurfaceElevation4 = HumanIndustrial.Rice
val SurfaceElevation5 = HumanIndustrial.Rice

// Gradient brushes for backward compatibility (all set to solid colors)
val DarkGradientBackground = Brush.verticalGradient(listOf(HumanIndustrial.Rice, HumanIndustrial.Rice))
val HeroGradientBackground = Brush.verticalGradient(listOf(HumanIndustrial.Rice, HumanIndustrial.Rice))
val PrimaryGradient = Brush.horizontalGradient(listOf(HumanIndustrial.Rice, HumanIndustrial.Rice))
val SecondaryGradient = Brush.horizontalGradient(listOf(HumanIndustrial.Rice, HumanIndustrial.Rice))
val CardGradientOverlay = Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent))
val CardElevationGradient = Brush.verticalGradient(listOf(HumanIndustrial.Rice, HumanIndustrial.Rice))
