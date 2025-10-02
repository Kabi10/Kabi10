package com.senthapps.slagrimarket.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// MATERIAL 3 SHAPE SYSTEM - Rounded Corners & Component Shapes
// ============================================================================

// Corner radius values following Material 3 guidelines
object CornerRadius {
    val None = 0.dp
    val ExtraSmall = 4.dp
    val Small = 8.dp
    val Medium = 12.dp
    val Large = 16.dp
    val ExtraLarge = 24.dp
    val Full = 50.dp
}

// Material 3 shape definitions
val Shapes = Shapes(
    // Extra small components (chips, small buttons)
    extraSmall = RoundedCornerShape(CornerRadius.ExtraSmall),
    
    // Small components (buttons, text fields)
    small = RoundedCornerShape(CornerRadius.Small),
    
    // Medium components (cards, dialogs)
    medium = RoundedCornerShape(CornerRadius.Medium),
    
    // Large components (bottom sheets, large cards)
    large = RoundedCornerShape(CornerRadius.Large),
    
    // Extra large components (modals, full-screen dialogs)
    extraLarge = RoundedCornerShape(CornerRadius.ExtraLarge)
)

// ============================================================================
// CUSTOM SHAPES FOR SPECIFIC COMPONENTS
// ============================================================================

object AppShapes {
    // Button shapes
    val ButtonSmall = RoundedCornerShape(CornerRadius.Small)
    val ButtonMedium = RoundedCornerShape(CornerRadius.Medium)
    val ButtonLarge = RoundedCornerShape(CornerRadius.Large)
    val ButtonPill = RoundedCornerShape(CornerRadius.Full)
    
    // Card shapes
    val CardSmall = RoundedCornerShape(CornerRadius.Small)
    val CardMedium = RoundedCornerShape(CornerRadius.Medium)
    val CardLarge = RoundedCornerShape(CornerRadius.Large)
    
    // Input field shapes
    val TextField = RoundedCornerShape(CornerRadius.Small)
    val TextFieldFocused = RoundedCornerShape(CornerRadius.Medium)
    
    // Modal and dialog shapes
    val Dialog = RoundedCornerShape(CornerRadius.Large)
    val BottomSheet = RoundedCornerShape(
        topStart = CornerRadius.Large,
        topEnd = CornerRadius.Large,
        bottomStart = CornerRadius.None,
        bottomEnd = CornerRadius.None
    )
    
    // Navigation shapes
    val NavigationBar = RoundedCornerShape(
        topStart = CornerRadius.Medium,
        topEnd = CornerRadius.Medium,
        bottomStart = CornerRadius.None,
        bottomEnd = CornerRadius.None
    )
    
    // Chip and tag shapes
    val Chip = RoundedCornerShape(CornerRadius.Small)
    val Tag = RoundedCornerShape(CornerRadius.ExtraSmall)
    
    // Image shapes
    val ImageSmall = RoundedCornerShape(CornerRadius.Small)
    val ImageMedium = RoundedCornerShape(CornerRadius.Medium)
    val ImageLarge = RoundedCornerShape(CornerRadius.Large)
    val ImageCircle = RoundedCornerShape(CornerRadius.Full)
    
    // Container shapes
    val Container = RoundedCornerShape(CornerRadius.Medium)
    val ContainerLarge = RoundedCornerShape(CornerRadius.Large)
    
    // Special shapes for marketplace components
    val ProductCard = RoundedCornerShape(CornerRadius.Medium)
    val PriceTag = RoundedCornerShape(CornerRadius.Small)
    val CategoryCard = RoundedCornerShape(CornerRadius.Large)
    val SearchBar = RoundedCornerShape(CornerRadius.Full)
}
