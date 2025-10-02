package com.senthapps.slagrimarket.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    // Primary colors - Green for CTAs and primary actions (WCAG AA compliant)
    primary = Green600,              // 4.5:1 contrast ratio on dark backgrounds
    onPrimary = Color.White,         // Maximum contrast
    primaryContainer = Green800,     // Darker container for better contrast
    onPrimaryContainer = Green100,   // Light green text on dark green container

    // Secondary colors - Blue for secondary actions (WCAG AA compliant)
    secondary = Blue400,             // 4.5:1 contrast ratio on dark backgrounds
    onSecondary = Color.White,       // Maximum contrast
    secondaryContainer = Blue800,    // Darker blue container
    onSecondaryContainer = Blue100,  // Light blue text on dark blue container

    // Tertiary colors - Alternative green shades
    tertiary = Green500,
    onTertiary = Color.White,
    tertiaryContainer = Green700,
    onTertiaryContainer = Green200,

    // Error colors (WCAG AA compliant)
    error = Error500,                // 4.5:1 contrast ratio
    onError = Color.White,
    errorContainer = Error700,
    onErrorContainer = Error100,

    // Background colors - Dark gradient system
    background = BackgroundDark,     // Gray900 for maximum depth
    onBackground = Color.White,      // Maximum contrast for text

    // Surface colors - Cards and elevated components
    surface = SurfaceDark,           // Gray800 for primary surfaces
    onSurface = Color.White,         // Primary text color
    surfaceVariant = Gray700,        // For card backgrounds with slight elevation
    onSurfaceVariant = Gray300,      // Secondary text with good contrast

    // Surface containers with proper elevation hierarchy
    surfaceContainer = Gray800,           // Base container
    surfaceContainerLow = Gray900,        // Lower elevation
    surfaceContainerHigh = Gray700,       // Higher elevation
    surfaceContainerHighest = Gray600,    // Highest elevation

    // Outline and borders (WCAG AA compliant)
    outline = Gray500,               // 4.5:1 contrast for borders
    outlineVariant = Gray600,        // Stronger borders when needed

    // Inverse colors for special cases
    inverseSurface = Gray100,
    inverseOnSurface = Gray900,
    inversePrimary = Green700,

    // Surface tints for Material 3 elevation
    surfaceTint = Green600,

    // Scrim for overlays
    scrim = Color.Black.copy(alpha = 0.6f)
)

private val LightColorScheme = lightColorScheme(
    // Primary colors (WCAG AA compliant)
    primary = Green600,              // Good contrast on light backgrounds
    onPrimary = Color.White,         // Maximum contrast
    primaryContainer = Green100,     // Light green container
    onPrimaryContainer = Green800,   // Dark green text on light container

    // Secondary colors (WCAG AA compliant)
    secondary = Blue600,             // Good contrast on light backgrounds
    onSecondary = Color.White,       // Maximum contrast
    secondaryContainer = Blue100,    // Light blue container
    onSecondaryContainer = Blue800,  // Dark blue text on light container

    // Tertiary colors
    tertiary = Green500,
    onTertiary = Color.White,
    tertiaryContainer = Green200,
    onTertiaryContainer = Green700,

    // Error colors (WCAG AA compliant)
    error = Error600,                // Good contrast on light backgrounds
    onError = Color.White,
    errorContainer = Error100,
    onErrorContainer = Error700,

    // Background colors
    background = BackgroundLight,    // Pure white background
    onBackground = Gray900,          // Dark text for maximum contrast

    // Surface colors
    surface = SurfaceLight,          // Pure white surface
    onSurface = Gray900,             // Dark text for primary content
    surfaceVariant = Gray100,        // Subtle surface variation
    onSurfaceVariant = Gray700,      // Secondary text with good contrast

    // Surface containers with proper elevation hierarchy
    surfaceContainer = Gray50,            // Base container
    surfaceContainerLow = Color.White,    // Lower elevation (pure white)
    surfaceContainerHigh = Gray100,       // Higher elevation
    surfaceContainerHighest = Gray200,    // Highest elevation

    // Outline and borders (WCAG AA compliant)
    outline = Gray400,               // Good contrast for borders
    outlineVariant = Gray300,        // Lighter borders when needed

    // Inverse colors for special cases
    inverseSurface = Gray800,
    inverseOnSurface = Gray100,
    inversePrimary = Green400,

    // Surface tints for Material 3 elevation
    surfaceTint = Green600,

    // Scrim for overlays
    scrim = Color.Black.copy(alpha = 0.4f)
)

@Composable
fun SLAgrimarketTheme(
    darkTheme: Boolean = true, // Force dark theme for modern design
    // Dynamic color is available on Android 12+ - can be enabled for user preference
    dynamicColor: Boolean = false, // Disabled by default for consistent branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Support for Material You dynamic colors on Android 12+
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) {
                // Use dynamic colors but maintain our green primary theme
                dynamicDarkColorScheme(context).copy(
                    primary = Green600,
                    secondary = Blue400,
                    tertiary = Green500,
                    surface = SurfaceDark,
                    background = BackgroundDark
                )
            } else {
                dynamicLightColorScheme(context).copy(
                    primary = Green600,
                    secondary = Blue600,
                    tertiary = Green500,
                    surface = SurfaceLight,
                    background = BackgroundLight
                )
            }
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // System UI integration - Status bar and navigation bar
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            // Set status bar content color based on theme
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}