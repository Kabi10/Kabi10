package com.senthapps.slagrimarket.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// PRIMARY GREEN PALETTE - Agriculture/Farming Theme (WCAG AA Compliant)
// ============================================================================

// Light green shades for gradients and accents
val Green50 = Color(0xFFf0fdf4)   // Lightest green for subtle backgrounds
val Green100 = Color(0xFFdcfce7)  // Very light green for hover states
val Green200 = Color(0xFFbbf7d0)  // Light green for disabled states
val Green300 = Color(0xFF86efac)  // Medium light green for secondary elements

// Core green shades
val Green400 = Color(0xFF4ade80)  // Medium green for secondary actions
val Green500 = Color(0xFF22c55e)  // Standard green for general use
val Green600 = Color(0xFF16a34a)  // Primary CTA color (4.5:1 contrast on dark)
val Green700 = Color(0xFF15803d)  // Dark green for pressed states
val Green800 = Color(0xFF166534)  // Darker green for emphasis
val Green900 = Color(0xFF14532d)  // Darkest green for high contrast

// Legacy green colors (keeping for compatibility)
val Green80 = Color(0xFFA8E6A3)
val Green60 = Color(0xFF81C784)
val Green40 = Color(0xFF4CAF50)
val Green20 = Color(0xFF2E7D32)

// ============================================================================
// SECONDARY BLUE PALETTE - Secondary Actions & Info States (WCAG AA Compliant)
// ============================================================================

val Blue50 = Color(0xFFeff6ff)    // Lightest blue for subtle backgrounds
val Blue100 = Color(0xFFdbeafe)   // Very light blue for hover states
val Blue200 = Color(0xFFbfdbfe)   // Light blue for disabled states
val Blue300 = Color(0xFF93c5fd)   // Medium light blue for secondary elements
val Blue400 = Color(0xFF60a5fa)   // Secondary action color (4.5:1 contrast on dark)
val Blue500 = Color(0xFF3b82f6)   // Standard blue for general use
val Blue600 = Color(0xFF2563eb)   // Medium blue for emphasis
val Blue700 = Color(0xFF1d4ed8)   // Dark blue for pressed states
val Blue800 = Color(0xFF1e40af)   // Darker blue for high contrast
val Blue900 = Color(0xFF1e3a8a)   // Darkest blue for maximum contrast

// ============================================================================
// SEMANTIC COLORS - Success, Warning, Error (WCAG AA Compliant)
// ============================================================================

// Success colors (Green-based)
val Success50 = Color(0xFFf0fdf4)
val Success100 = Color(0xFFdcfce7)
val Success500 = Color(0xFF22c55e)  // Standard success (4.5:1 contrast)
val Success600 = Color(0xFF16a34a)  // Primary success
val Success700 = Color(0xFF15803d)  // Dark success
val GreenSuccess = Success500       // Legacy compatibility

// Warning colors (Amber-based)
val Warning50 = Color(0xFFfffbeb)
val Warning100 = Color(0xFFfef3c7)
val Warning400 = Color(0xFFfbbf24)  // Medium warning
val Warning500 = Color(0xFFf59e0b)  // Standard warning (4.5:1 contrast)
val Warning600 = Color(0xFFd97706)  // Primary warning
val Warning700 = Color(0xFFb45309)  // Dark warning
val YellowWarning = Warning500      // Legacy compatibility

// Error colors (Red-based)
val Error50 = Color(0xFFfef2f2)
val Error100 = Color(0xFFfecaca)
val Error400 = Color(0xFFf87171)    // Medium error
val Error500 = Color(0xFFef4444)    // Standard error (4.5:1 contrast)
val Error600 = Color(0xFFdc2626)    // Primary error
val Error700 = Color(0xFFb91c1c)    // Dark error
val Red40 = Error600                // Legacy compatibility
val Red80 = Error100                // Legacy compatibility

// ============================================================================
// NEUTRAL GRAYS - Backgrounds, Borders, Text (WCAG AA Compliant)
// ============================================================================

// Modern gray scale for dark theme
val Gray50 = Color(0xFFf9fafb)     // Lightest gray
val Gray100 = Color(0xFFf3f4f6)    // Very light gray
val Gray200 = Color(0xFFe5e7eb)    // Light gray for borders
val Gray300 = Color(0xFFd1d5db)    // Medium light gray
val Gray400 = Color(0xFF9ca3af)    // Secondary text (4.5:1 contrast on dark)
val Gray500 = Color(0xFF6b7280)    // Medium gray for icons
val Gray600 = Color(0xFF4b5563)    // Border/outline color (4.5:1 contrast)
val Gray700 = Color(0xFF374151)    // Surface variant
val Gray800 = Color(0xFF1f2937)    // Surface color
val Gray900 = Color(0xFF111827)    // Background color

// Legacy gray colors (keeping for compatibility)
val Grey900 = Gray900
val Grey800 = Gray800
val Grey700 = Gray700
val Grey600 = Gray600
val Grey500 = Gray500
val Grey400 = Gray400
val Grey300 = Gray300
val Grey200 = Gray200
val Grey100 = Gray100
val Grey50 = Gray50
val Grey90 = Color(0xFFF5F5F5)
val Grey80 = Color(0xFFE0E0E0)
val Grey60 = Color(0xFF9E9E9E)
val Grey40 = Color(0xFF616161)
val Grey20 = Color(0xFF212121)

// ============================================================================
// SURFACE COLORS WITH ELEVATION VARIANTS
// ============================================================================

// Background colors
val BackgroundLight = Color(0xFFFFFBFE)
val BackgroundDark = Gray900
val SurfaceLight = Color(0xFFFFFBFE)
val SurfaceDark = Gray800

// Surface elevation variants for dark theme
val SurfaceElevation1 = Color(0xFF1f2937)  // 1dp elevation
val SurfaceElevation2 = Color(0xFF374151)  // 2dp elevation
val SurfaceElevation3 = Color(0xFF4b5563)  // 3dp elevation
val SurfaceElevation4 = Color(0xFF6b7280)  // 4dp elevation
val SurfaceElevation5 = Color(0xFF9ca3af)  // 5dp elevation

// ============================================================================
// GRADIENT BACKGROUNDS
// ============================================================================

// Primary gradient backgrounds
val DarkGradientBackground = Brush.verticalGradient(
    colors = listOf(Gray900, Gray800)
)

val HeroGradientBackground = Brush.verticalGradient(
    colors = listOf(Green600, Green800)
)

val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(Green500, Green600)
)

val SecondaryGradient = Brush.horizontalGradient(
    colors = listOf(Blue400, Blue500)
)

// Card gradient overlays
val CardGradientOverlay = Brush.verticalGradient(
    colors = listOf(
        Color.Transparent,
        Color.Black.copy(alpha = 0.1f)
    )
)

val CardElevationGradient = Brush.verticalGradient(
    colors = listOf(
        Gray800,
        Gray700.copy(alpha = 0.8f)
    )
)