package com.senthapps.slagrimarket.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ============================================================================
// AGRIMARKET INDUSTRIAL TYPOGRAPHY SYSTEM
// Heavy, flat, brutal typography for machinery interface
// ============================================================================

// System sans-serif only - no custom fonts
val AgrimarketFontFamily = FontFamily.Default

// Typography scale - BINARY sizing (primary vs secondary)
val AgrimarketTypography = Typography(
    // HUGE DISPLAY - For prices on detail screens
    displayLarge = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Black,      // 900
        fontSize = 56.sp,
        lineHeight = 56.sp,                 // Tight 1:1 ratio
        letterSpacing = 0.sp
    ),

    // LARGE PRIMARY - For home screen tiles, section titles
    headlineLarge = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Black,      // 900
        fontSize = 28.sp,
        lineHeight = 34.sp,                 // 1.2x ratio
        letterSpacing = 0.sp
    ),

    // MEDIUM PRIMARY - For detail screen product names, prices in lists
    headlineMedium = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Black,      // 900
        fontSize = 24.sp,
        lineHeight = 30.sp,                 // 1.25x ratio
        letterSpacing = 0.sp
    ),

    // SCREEN TITLES - For top bar titles
    titleLarge = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Black,      // 900
        fontSize = 20.sp,
        lineHeight = 24.sp,                 // 1.2x ratio
        letterSpacing = 0.sp
    ),

    // CONTENT TEXT - For list item names, metadata
    bodyLarge = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Medium,     // 500
        fontSize = 18.sp,
        lineHeight = 23.sp,                 // 1.27x ratio
        letterSpacing = 0.sp
    ),

    // BUTTON TEXT - For button labels
    labelLarge = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Black,      // 900
        fontSize = 18.sp,
        lineHeight = 22.sp,                 // 1.2x ratio
        letterSpacing = 0.sp
    ),

    // SECONDARY TEXT - For units, labels, metadata
    bodyMedium = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Medium,     // 500
        fontSize = 16.sp,
        lineHeight = 20.sp,                 // 1.25x ratio
        letterSpacing = 0.sp
    ),

    // FORM FIELD LABELS - For input field labels
    labelMedium = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Black,      // 900
        fontSize = 14.sp,
        lineHeight = 17.sp,                 // 1.2x ratio
        letterSpacing = 0.sp
    ),

    // TERTIARY TEXT - For timestamps, supporting info
    labelSmall = TextStyle(
        fontFamily = AgrimarketFontFamily,
        fontWeight = FontWeight.Medium,     // 500
        fontSize = 14.sp,
        lineHeight = 17.sp,                 // 1.2x ratio
        letterSpacing = 0.sp
    )
)

// Legacy compatibility - map old names to new system
val Typography = AgrimarketTypography