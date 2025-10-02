package com.senthapps.slagrimarket.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.R

// ============================================================================
// FONT FAMILIES - Multilingual Support (English, Tamil, Sinhala)
// ============================================================================

// Use system default font family which includes proper multilingual support
// Android system fonts include Roboto, Noto Sans Tamil, and Noto Sans Sinhala by default
// This ensures Tamil and Sinhala characters render correctly
val AppFontFamily = FontFamily.Default

// Alternative font family for headings with better multilingual support
val HeadingFontFamily = FontFamily.Default

// ============================================================================
// FONT WEIGHT DEFINITIONS
// ============================================================================

object AppFontWeights {
    val Light = FontWeight.Light        // 300
    val Regular = FontWeight.Normal     // 400
    val Medium = FontWeight.Medium      // 500
    val SemiBold = FontWeight.SemiBold  // 600
    val Bold = FontWeight.Bold          // 700
    val ExtraBold = FontWeight.ExtraBold // 800
}

// ============================================================================
// TYPOGRAPHY SYSTEM - Multilingual & Accessibility Optimized
// ============================================================================

// Material 3 typography styles optimized for English, Tamil, and Sinhala
// Increased line heights for better readability in complex scripts
// Respects system font size settings for accessibility
val Typography = Typography(
    // Display styles - For large, prominent text
    displayLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = AppFontWeights.Bold,
        fontSize = 57.sp,
        lineHeight = 68.sp,        // Increased for Tamil/Sinhala diacritics
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = AppFontWeights.Bold,
        fontSize = 45.sp,
        lineHeight = 56.sp,        // Increased for better readability
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = AppFontWeights.Bold,
        fontSize = 36.sp,
        lineHeight = 48.sp,        // Increased for complex scripts
        letterSpacing = 0.sp
    ),

    // Headline styles - For section headers and important text
    headlineLarge = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = AppFontWeights.Bold,
        fontSize = 32.sp,
        lineHeight = 44.sp,        // Increased for Tamil/Sinhala
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = AppFontWeights.Bold,
        fontSize = 28.sp,
        lineHeight = 40.sp,        // Better spacing for complex scripts
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = HeadingFontFamily,
        fontWeight = AppFontWeights.SemiBold,
        fontSize = 24.sp,
        lineHeight = 36.sp,        // Increased for readability
        letterSpacing = 0.sp
    ),
    // Title styles - For card titles, section headers
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.SemiBold,
        fontSize = 22.sp,
        lineHeight = 32.sp,        // Increased for Tamil/Sinhala readability
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Medium,
        fontSize = 16.sp,
        lineHeight = 26.sp,        // Better spacing for complex scripts
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,        // Increased for better readability
        letterSpacing = 0.1.sp
    ),

    // Body styles - For main content text
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Regular,
        fontSize = 16.sp,
        lineHeight = 28.sp,        // Generous line height for Tamil/Sinhala
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Regular,
        fontSize = 14.sp,
        lineHeight = 24.sp,        // Increased for complex scripts
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Regular,
        fontSize = 12.sp,
        lineHeight = 20.sp,        // Better spacing for small text
        letterSpacing = 0.4.sp
    ),
    // Label styles - For buttons, form labels, captions
    labelLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Medium,
        fontSize = 14.sp,
        lineHeight = 22.sp,        // Better spacing for button text
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Medium,
        fontSize = 12.sp,
        lineHeight = 18.sp,        // Increased for Tamil/Sinhala
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Medium,
        fontSize = 11.sp,
        lineHeight = 18.sp,        // Better readability for small labels
        letterSpacing = 0.5.sp
    )
)

// ============================================================================
// ADDITIONAL TYPOGRAPHY UTILITIES
// ============================================================================

// Custom text styles for specific use cases
object AppTextStyles {
    // For price displays and important numbers
    val PriceDisplay = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    )

    // For captions and metadata
    val Caption = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Regular,
        fontSize = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    )

    // For overline text (categories, tags)
    val Overline = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Medium,
        fontSize = 10.sp,
        lineHeight = 16.sp,
        letterSpacing = 1.5.sp
    )

    // For button text with better multilingual support
    val ButtonText = TextStyle(
        fontFamily = AppFontFamily,
        fontWeight = AppFontWeights.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
}