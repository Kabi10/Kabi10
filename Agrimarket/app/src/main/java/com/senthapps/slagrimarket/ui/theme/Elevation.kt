package com.senthapps.slagrimarket.ui.theme

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// MATERIAL 3 ELEVATION SYSTEM - Shadows & Surface Levels
// ============================================================================

// Elevation levels following Material 3 guidelines
object ElevationLevels {
    val Level0 = 0.dp    // Surface level (no elevation)
    val Level1 = 1.dp    // Raised elements (cards, buttons)
    val Level2 = 3.dp    // Floating action buttons, snackbars
    val Level3 = 6.dp    // Modals, dialogs
    val Level4 = 8.dp    // Navigation drawers
    val Level5 = 12.dp   // App bars when scrolled
    val Level6 = 16.dp   // Floating elements
    val Level7 = 24.dp   // Highest elevation (tooltips, menus)
}

// ============================================================================
// COMPONENT-SPECIFIC ELEVATIONS
// ============================================================================

object AppElevations {
    // Card elevations
    val CardResting = ElevationLevels.Level1
    val CardHovered = ElevationLevels.Level2
    val CardPressed = ElevationLevels.Level0
    val CardDragged = ElevationLevels.Level3
    
    // Button elevations
    val ButtonResting = ElevationLevels.Level1
    val ButtonHovered = ElevationLevels.Level2
    val ButtonPressed = ElevationLevels.Level0
    val ButtonDisabled = ElevationLevels.Level0
    
    // FAB elevations
    val FabResting = ElevationLevels.Level2
    val FabHovered = ElevationLevels.Level3
    val FabPressed = ElevationLevels.Level1
    
    // Modal and dialog elevations
    val Dialog = ElevationLevels.Level7
    val Modal = ElevationLevels.Level6
    val BottomSheet = ElevationLevels.Level4
    val Tooltip = ElevationLevels.Level7
    
    // Navigation elevations
    val NavigationBar = ElevationLevels.Level2
    val NavigationDrawer = ElevationLevels.Level4
    val AppBar = ElevationLevels.Level0
    val AppBarScrolled = ElevationLevels.Level5
    
    // Surface elevations for dark theme
    val SurfaceLevel1 = ElevationLevels.Level1
    val SurfaceLevel2 = ElevationLevels.Level2
    val SurfaceLevel3 = ElevationLevels.Level3
    val SurfaceLevel4 = ElevationLevels.Level4
    val SurfaceLevel5 = ElevationLevels.Level5
    
    // Marketplace-specific elevations
    val ProductCard = ElevationLevels.Level1
    val ProductCardHovered = ElevationLevels.Level2
    val PriceCard = ElevationLevels.Level2
    val CategoryCard = ElevationLevels.Level1
    val SearchBar = ElevationLevels.Level1
    val FilterChip = ElevationLevels.Level0
    val FilterChipSelected = ElevationLevels.Level1
}

// ============================================================================
// CARD ELEVATION CONFIGURATIONS
// ============================================================================

/**
 * Card elevation configurations for different component types
 * Note: CardElevation objects should be created within @Composable functions
 */
object AppCardElevations {
    // Standard card elevation values
    object Default {
        val defaultElevation = AppElevations.CardResting
        val pressedElevation = AppElevations.CardPressed
        val focusedElevation = AppElevations.CardHovered
        val hoveredElevation = AppElevations.CardHovered
        val draggedElevation = AppElevations.CardDragged
        val disabledElevation = ElevationLevels.Level0
    }

    // Product card elevation values
    object ProductCard {
        val defaultElevation = AppElevations.ProductCard
        val pressedElevation = AppElevations.CardPressed
        val focusedElevation = AppElevations.ProductCardHovered
        val hoveredElevation = AppElevations.ProductCardHovered
        val draggedElevation = AppElevations.CardDragged
        val disabledElevation = ElevationLevels.Level0
    }

    // Category card elevation values
    object CategoryCard {
        val defaultElevation = AppElevations.CategoryCard
        val pressedElevation = AppElevations.CardPressed
        val focusedElevation = AppElevations.CardHovered
        val hoveredElevation = AppElevations.CardHovered
        val draggedElevation = AppElevations.CardDragged
        val disabledElevation = ElevationLevels.Level0
    }

    // Price card elevation values (slightly higher for emphasis)
    object PriceCard {
        val defaultElevation = AppElevations.PriceCard
        val pressedElevation = AppElevations.CardPressed
        val focusedElevation = ElevationLevels.Level3
        val hoveredElevation = ElevationLevels.Level3
        val draggedElevation = AppElevations.CardDragged
        val disabledElevation = ElevationLevels.Level0
    }
}

// ============================================================================
// ELEVATION UTILITIES
// ============================================================================

object ElevationUtils {
    /**
     * Get appropriate elevation for dark theme surfaces
     * Higher elevations get lighter colors in dark theme
     */
    fun getSurfaceElevationColor(elevation: Dp): androidx.compose.ui.graphics.Color {
        return when (elevation) {
            ElevationLevels.Level0 -> SurfaceDark
            ElevationLevels.Level1 -> SurfaceElevation1
            ElevationLevels.Level2 -> SurfaceElevation2
            ElevationLevels.Level3 -> SurfaceElevation3
            ElevationLevels.Level4 -> SurfaceElevation4
            ElevationLevels.Level5 -> SurfaceElevation5
            else -> SurfaceElevation5
        }
    }
    
    /**
     * Check if elevation is appropriate for the component
     */
    fun isValidElevation(elevation: Dp): Boolean {
        return elevation >= ElevationLevels.Level0 && elevation <= ElevationLevels.Level7
    }
}
