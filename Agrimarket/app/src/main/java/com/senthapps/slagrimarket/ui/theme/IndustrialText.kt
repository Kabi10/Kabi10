package com.senthapps.slagrimarket.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.senthapps.slagrimarket.ui.home.AppLanguage

// ============================================================================
// INDUSTRIAL TEXT UTILITIES v1.0
// Language-aware text formatting for Sinhala/Tamil/English
// Rule: NEVER uppercase Sinhala or Tamil text
// ============================================================================

/**
 * CompositionLocal for current app language.
 * Default: TAMIL (per LanguagePreferences.DEFAULT_LANGUAGE)
 *
 * Usage in top-level composable (MainActivity):
 *   CompositionLocalProvider(LocalAppLanguage provides language) { ... }
 *
 * Usage in child composables:
 *   val language = LocalAppLanguage.current
 *   Text(text = "HELLO".industrialFormat(language))
 */
val LocalAppLanguage = compositionLocalOf { AppLanguage.TAMIL }

/**
 * CompositionLocal for text scale factor (accessibility).
 * Default: 1.0f (normal). Large text mode: 1.35f
 *
 * Usage in composables:
 *   val scale = LocalTextScale.current
 *   Text(fontSize = (16 * scale).sp)
 */
val LocalTextScale = compositionLocalOf { 1.0f }

/**
 * Convert language code string to AppLanguage enum.
 * Maps DataStore string codes to UI enum.
 *
 * @param code Language code from LanguagePreferences ("en", "ta", "si")
 * @return Corresponding AppLanguage enum value
 */
fun languageCodeToAppLanguage(code: String): AppLanguage {
    return when (code) {
        "en" -> AppLanguage.ENGLISH
        "si" -> AppLanguage.SINHALA
        "ta" -> AppLanguage.TAMIL
        else -> AppLanguage.TAMIL // Default per LanguagePreferences.DEFAULT_LANGUAGE
    }
}

/**
 * Format text for Industrial Design System display.
 *
 * - English: Returns UPPERCASE
 * - Sinhala/Tamil: Returns original text (no transformation)
 *
 * @param language Current app language
 * @return Formatted text safe for display
 */
fun String.industrialFormat(language: AppLanguage): String {
    return when (language) {
        AppLanguage.ENGLISH -> this.uppercase()
        AppLanguage.SINHALA, AppLanguage.TAMIL -> this
    }
}

/**
 * Format text using CompositionLocal language.
 * Convenience for use inside @Composable functions.
 *
 * Usage:
 *   Text(text = "hello".industrialFormat())
 */
@Composable
fun String.industrialFormat(): String {
    val language = LocalAppLanguage.current
    return industrialFormat(language)
}

/**
 * Check if current language allows uppercase transformation.
 *
 * @param language Current app language
 * @return true if uppercase is allowed (English only)
 */
fun canUppercase(language: AppLanguage): Boolean {
    return language == AppLanguage.ENGLISH
}
