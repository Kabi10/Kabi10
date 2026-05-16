package com.senthapps.slagrimarket.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================================
// HUMAN INDUSTRIAL TYPOGRAPHY SYSTEM v1.0
// Heavy, grounded, hand-painted signboard feel
// System fonts only - loads instantly, feels native
// ============================================================================

val AgrimarketFontFamily = FontFamily.Default

// Typography Scale per Human Industrial Design System
// ─────────────────────────────────────────────────────
// Screen Title:     24sp Bold UPPERCASE
// Product Name:     20sp Bold UPPERCASE
// Price:            28sp Bold
// Section Label:    14sp Bold UPPERCASE
// Body/Metadata:    16sp Regular
// Button Text:      18sp Bold UPPERCASE
// Input Text:       18sp Regular
// Empty State:      18sp Regular

object HumanIndustrialType {
    // Screen titles - 28sp Bold UPPERCASE (bumped for older farmers)
    val screenTitle = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.5.sp
    )

    // Product names in lists - 22sp Bold UPPERCASE
    val productName = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    // Prices - 32sp Bold (largest text for scannability)
    val price = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 32.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    )

    // Price in listing rows - 28sp Bold
    val priceSmall = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    )

    // Price row product name - 20sp Bold UPPERCASE
    val priceRowProduct = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )

    // Section labels - 16sp Bold UPPERCASE
    val sectionLabel = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    )

    // Body/Metadata - 18sp Regular (bumped for readability)
    val body = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Button text - 20sp Bold UPPERCASE
    val button = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )

    // Input text - 20sp Regular
    val input = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )

    // Empty state - 20sp Regular
    val emptyState = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 20.sp,
        lineHeight = 27.sp,
        letterSpacing = 0.sp
    )

    // Unit labels (per kg, etc.) - 16sp Regular
    val unit = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )

    // Timestamp - 14sp Regular
    val timestamp = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // Home tile text - 28sp Bold UPPERCASE
    val homeTile = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 1.sp
    )

    // Category tile text - 26sp Bold UPPERCASE
    val categoryTile = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = 1.sp
    )

    // Status badge - 14sp Bold UPPERCASE
    val statusBadge = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )

    // Quantity text - 16sp Bold UPPERCASE
    val quantity = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 16.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    )
}

// Material3 Typography mapping for backward compatibility
val AgrimarketTypography = Typography(
    // Screen titles
    displayLarge = HumanIndustrialType.price,
    displayMedium = HumanIndustrialType.screenTitle,
    displaySmall = HumanIndustrialType.productName,

    // Headlines
    headlineLarge = HumanIndustrialType.screenTitle,
    headlineMedium = HumanIndustrialType.productName,
    headlineSmall = HumanIndustrialType.priceRowProduct,

    // Titles
    titleLarge = HumanIndustrialType.screenTitle,
    titleMedium = HumanIndustrialType.productName,
    titleSmall = HumanIndustrialType.priceRowProduct,

    // Body
    bodyLarge = HumanIndustrialType.input,
    bodyMedium = HumanIndustrialType.body,
    bodySmall = HumanIndustrialType.unit,

    // Labels
    labelLarge = HumanIndustrialType.button,
    labelMedium = HumanIndustrialType.sectionLabel,
    labelSmall = HumanIndustrialType.timestamp
)

// Legacy compatibility
val Typography = AgrimarketTypography
