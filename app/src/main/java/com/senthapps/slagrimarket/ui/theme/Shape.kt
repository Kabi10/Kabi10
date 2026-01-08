package com.senthapps.slagrimarket.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

// ============================================================================
// AGRIMARKET INDUSTRIAL SHAPE SYSTEM
// Hard rectangles, no rounded corners, machinery aesthetic
// ============================================================================

// All shapes are rectangles - no rounded corners
val AgrimarketShapes = Shapes(
    extraSmall = RoundedCornerShape(0.dp),
    small = RoundedCornerShape(0.dp),
    medium = RoundedCornerShape(0.dp),
    large = RoundedCornerShape(0.dp),
    extraLarge = RoundedCornerShape(0.dp)
)

// Legacy compatibility
val Shapes = AgrimarketShapes

// Legacy CornerRadius object for backward compatibility
object CornerRadius {
    val None = 0.dp
    val ExtraSmall = 0.dp
    val Small = 0.dp
    val Medium = 0.dp
    val Large = 0.dp
    val ExtraLarge = 0.dp
    val Full = 0.dp
}

// Legacy AppShapes object for backward compatibility
object AppShapes {
    val ButtonSmall = RoundedCornerShape(0.dp)
    val ButtonMedium = RoundedCornerShape(0.dp)
    val ButtonLarge = RoundedCornerShape(0.dp)
    val ButtonPill = RoundedCornerShape(0.dp)
    val CardSmall = RoundedCornerShape(0.dp)
    val CardMedium = RoundedCornerShape(0.dp)
    val CardLarge = RoundedCornerShape(0.dp)
    val TextField = RoundedCornerShape(0.dp)
    val TextFieldFocused = RoundedCornerShape(0.dp)
    val Dialog = RoundedCornerShape(0.dp)
    val BottomSheet = RoundedCornerShape(0.dp)
    val NavigationBar = RoundedCornerShape(0.dp)
    val Chip = RoundedCornerShape(0.dp)
    val Tag = RoundedCornerShape(0.dp)
    val ImageSmall = RoundedCornerShape(0.dp)
    val ImageMedium = RoundedCornerShape(0.dp)
    val ImageLarge = RoundedCornerShape(0.dp)
    val ImageCircle = RoundedCornerShape(0.dp)
    val Container = RoundedCornerShape(0.dp)
    val ContainerLarge = RoundedCornerShape(0.dp)
    val ProductCard = RoundedCornerShape(0.dp)
    val PriceTag = RoundedCornerShape(0.dp)
    val CategoryCard = RoundedCornerShape(0.dp)
    val SearchBar = RoundedCornerShape(0.dp)
}

// ============================================================================
// SPACING SYSTEM - 16dp Grid System (not 8dp)
// Chunky, uniform, grid-based spacing for thick fingers
// ============================================================================

object Spacing {
    val None = 0.dp
    val Base = 16.dp        // Base unit
    val Double = 32.dp      // Between major sections
    val Triple = 48.dp      // Large gaps
    val Quad = 64.dp        // Touch targets

    // Legacy compatibility
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val XXLarge = 32.dp
    val XXXLarge = 48.dp
}

// Border widths
object BorderWidth {
    val Thin = 1.dp         // Dividers, subtle borders
    val Standard = 2.dp     // Input fields, regular borders
    val Thick = 3.dp        // Tile divisions, emphasized borders
}
