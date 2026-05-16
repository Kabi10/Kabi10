package com.senthapps.slagrimarket.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ============================================================================
// AGRIMARKET INDUSTRIAL COLOR SCHEME
// Pure high-contrast colors, no elevation, no gradients
// ============================================================================

private val AgrimarketColorScheme = lightColorScheme(
    // Primary action color - Orange
    primary = AgrimarketOrange,
    onPrimary = AgrimarketWhite,
    primaryContainer = AgrimarketOrange,
    onPrimaryContainer = AgrimarketWhite,

    // Secondary color - Black for borders and secondary actions
    secondary = AgrimarketBlack,
    onSecondary = AgrimarketWhite,
    secondaryContainer = AgrimarketWhite,
    onSecondaryContainer = AgrimarketBlack,

    // Error color - Red
    error = AgrimarketRed,
    onError = AgrimarketWhite,
    errorContainer = AgrimarketRed,
    onErrorContainer = AgrimarketWhite,

    // Background - Pure white only
    background = AgrimarketWhite,
    onBackground = AgrimarketBlack,

    // Surface - Pure white (no elevation system)
    surface = AgrimarketWhite,
    onSurface = AgrimarketBlack,
    surfaceVariant = AgrimarketOffWhite,
    onSurfaceVariant = AgrimarketBlack,

    // Surface containers - all flat, no elevation
    surfaceContainer = AgrimarketWhite,
    surfaceContainerLow = AgrimarketWhite,
    surfaceContainerHigh = AgrimarketOffWhite,
    surfaceContainerHighest = AgrimarketOffWhite,

    // Outline - Black borders
    outline = AgrimarketBlack,
    outlineVariant = AgrimarketBlack,

    // No elevation tint
    surfaceTint = Color.Transparent,

    // Scrim for modals
    scrim = AgrimarketBlack.copy(alpha = 0.6f)
)

// ============================================================================
// AGRIMARKET THEME COMPOSABLE
// Industrial design system with no dark mode, no animations, no ripples
// ============================================================================

@Composable
fun SLAgrimarketTheme(
    darkTheme: Boolean = false, // Always light theme - pure white background
    dynamicColor: Boolean = false, // No dynamic colors - consistent branding
    content: @Composable () -> Unit
) {
    // Always use light color scheme - no dark mode
    val colorScheme = AgrimarketColorScheme

    // System UI integration - White status bar and navigation bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = AgrimarketWhite.toArgb()
            window.navigationBarColor = AgrimarketWhite.toArgb()

            // Always use light status bars (dark icons on white background)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AgrimarketTypography,
        shapes = AgrimarketShapes,
        content = content
    )
}