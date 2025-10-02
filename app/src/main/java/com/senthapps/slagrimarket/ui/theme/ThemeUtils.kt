package com.senthapps.slagrimarket.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// ============================================================================
// THEME UTILITIES & COMPOSABLE HELPERS
// ============================================================================

/**
 * Utility object providing convenient access to theme-related functions
 */
object ThemeUtils {
    
    /**
     * Get the appropriate surface color for a given elevation level
     */
    @Composable
    fun getSurfaceColorAtElevation(elevation: Dp): Color {
        return MaterialTheme.colorScheme.surface
    }
    
    /**
     * Get the appropriate text color for a given background
     */
    @Composable
    fun getTextColorForBackground(backgroundColor: Color): Color {
        return if (isColorDark(backgroundColor)) {
            Color.White
        } else {
            Color.Black
        }
    }
    
    /**
     * Determine if a color is considered dark
     */
    private fun isColorDark(color: Color): Boolean {
        val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)
        return luminance < 0.5
    }
}

// ============================================================================
// COMPOSABLE THEME COMPONENTS
// ============================================================================

/**
 * A themed surface with gradient background
 */
@Composable
fun GradientSurface(
    gradient: Brush,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            content()
        }
    }
}

/**
 * A themed card with marketplace-specific styling
 */
@Composable
fun MarketplaceCard(
    modifier: Modifier = Modifier,
    elevation: Dp = AppElevations.ProductCard,
    shape: androidx.compose.foundation.shape.RoundedCornerShape = AppShapes.ProductCard,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        content = { content() }
    )
}

/**
 * A themed card specifically for product listings
 */
@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    content: @Composable () -> Unit
) {
    val elevation = if (isSelected) AppElevations.ProductCardHovered else AppElevations.ProductCard
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppCardElevations.ProductCard.defaultElevation,
            pressedElevation = AppCardElevations.ProductCard.pressedElevation,
            focusedElevation = AppCardElevations.ProductCard.focusedElevation,
            hoveredElevation = AppCardElevations.ProductCard.hoveredElevation,
            draggedElevation = AppCardElevations.ProductCard.draggedElevation,
            disabledElevation = AppCardElevations.ProductCard.disabledElevation
        ),
        shape = AppShapes.ProductCard,
        colors = CardDefaults.cardColors(
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        content = { content() }
    )
}

/**
 * A themed card for category display
 */
@Composable
fun CategoryCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppCardElevations.CategoryCard.defaultElevation,
            pressedElevation = AppCardElevations.CategoryCard.pressedElevation,
            focusedElevation = AppCardElevations.CategoryCard.focusedElevation,
            hoveredElevation = AppCardElevations.CategoryCard.hoveredElevation,
            draggedElevation = AppCardElevations.CategoryCard.draggedElevation,
            disabledElevation = AppCardElevations.CategoryCard.disabledElevation
        ),
        shape = AppShapes.CategoryCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        content = { content() }
    )
}

/**
 * A themed card for price display with emphasis
 */
@Composable
fun PriceCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = AppCardElevations.PriceCard.defaultElevation,
            pressedElevation = AppCardElevations.PriceCard.pressedElevation,
            focusedElevation = AppCardElevations.PriceCard.focusedElevation,
            hoveredElevation = AppCardElevations.PriceCard.hoveredElevation,
            draggedElevation = AppCardElevations.PriceCard.draggedElevation,
            disabledElevation = AppCardElevations.PriceCard.disabledElevation
        ),
        shape = AppShapes.PriceTag,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        content = { content() }
    )
}

/**
 * A surface with the app's primary gradient background
 */
@Composable
fun PrimaryGradientSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GradientSurface(
        gradient = PrimaryGradient,
        modifier = modifier,
        content = content
    )
}

/**
 * A surface with the app's dark gradient background
 */
@Composable
fun DarkGradientSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GradientSurface(
        gradient = DarkGradientBackground,
        modifier = modifier,
        content = content
    )
}

/**
 * A surface with hero gradient background for landing sections
 */
@Composable
fun HeroGradientSurface(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    GradientSurface(
        gradient = HeroGradientBackground,
        modifier = modifier,
        content = content
    )
}

// ============================================================================
// MODIFIER EXTENSIONS
// ============================================================================

/**
 * Apply marketplace-specific card styling
 */
fun Modifier.marketplaceCard(): Modifier = this
    .clip(AppShapes.ProductCard)
    .background(
        brush = CardElevationGradient,
        shape = AppShapes.ProductCard
    )

/**
 * Apply search bar styling
 */
fun Modifier.searchBarStyle(): Modifier = this
    .clip(AppShapes.SearchBar)
    .background(
        color = Gray800,
        shape = AppShapes.SearchBar
    )

/**
 * Apply category chip styling
 */
fun Modifier.categoryChip(isSelected: Boolean = false): Modifier = this
    .clip(AppShapes.Chip)
    .background(
        color = if (isSelected) Green600 else Gray700,
        shape = AppShapes.Chip
    )
    .padding(horizontal = 12.dp, vertical = 6.dp)
