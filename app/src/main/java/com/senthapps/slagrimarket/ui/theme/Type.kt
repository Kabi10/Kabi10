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
    // Screen titles - 24sp Bold UPPERCASE
    val screenTitle = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.5.sp
    )

    // Product names in lists - 20sp Bold UPPERCASE
    val productName = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    )

    // Prices - 28sp Bold (largest text for scannability)
    val price = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    )

    // Price in listing rows - 24sp Bold
    val priceSmall = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    )

    // Price row product name - 18sp Bold UPPERCASE
    val priceRowProduct = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Section labels - 14sp Bold UPPERCASE
    val sectionLabel = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )

    // Body/Metadata - 16sp Regular
    val body = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp
    )

    // Button text - 18sp Bold UPPERCASE
    val button = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    )

    // Input text - 18sp Regular
    val input = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

    // Empty state - 18sp Regular
    val emptyState = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    )

    // Unit labels (per kg, etc.) - 14sp Regular
    val unit = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp
    )

    // Timestamp - 12sp Regular
    val timestamp = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Normal,      // 400
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    )

    // Home tile text - 24sp Bold UPPERCASE (matches screen title)
    val homeTile = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 1.sp
    )

    // Category tile text - 22sp Bold UPPERCASE
    val categoryTile = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 1.sp
    )

    // Status badge - 12sp Bold UPPERCASE
    val statusBadge = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    // Quantity text - 14sp Bold UPPERCASE
    val quantity = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Bold,        // 700
        fontSize = 14.sp,
        lineHeight = 18.sp,
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
