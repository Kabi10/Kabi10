package com.senthapps.slagrimarket.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

// Extension property to create DataStore
private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "language_preferences")

@Singleton
class LanguagePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.languageDataStore
    
    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("selected_language")
        
        // Supported languages
        const val ENGLISH = "en"
        const val TAMIL = "ta"
        const val SINHALA = "si"
        
        val SUPPORTED_LANGUAGES = listOf(ENGLISH, TAMIL, SINHALA)
        
        // Language display names
        val LANGUAGE_NAMES = mapOf(
            ENGLISH to "English",
            TAMIL to "தமிழ்",
            SINHALA to "සිංහල"
        )
        
        // Language codes for display
        val LANGUAGE_CODES = mapOf(
            ENGLISH to "EN",
            TAMIL to "TA",
            SINHALA to "SI"
        )
    }
    
    /**
     * Get the current selected language
     * Defaults to Tamil if no language is set
     */
    val selectedLanguage: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_KEY] ?: TAMIL
    }
    
    /**
     * Set the selected language
     */
    suspend fun setLanguage(language: String) {
        if (language in SUPPORTED_LANGUAGES) {
            dataStore.edit { preferences ->
                preferences[LANGUAGE_KEY] = language
            }
            Timber.d("Language set to: $language")
        } else {
            Timber.w("Unsupported language: $language")
        }
    }
    
    /**
     * Get the current language synchronously (for initial setup)
     */
    suspend fun getCurrentLanguage(): String {
        var currentLang = TAMIL
        dataStore.data.map { preferences ->
            preferences[LANGUAGE_KEY] ?: TAMIL
        }.collect { currentLang = it }
        return currentLang
    }
    
    /**
     * Get the display name for a language code
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return LANGUAGE_NAMES[languageCode] ?: languageCode
    }
    
    /**
     * Get the short code for a language
     */
    fun getLanguageShortCode(languageCode: String): String {
        return LANGUAGE_CODES[languageCode] ?: languageCode.uppercase()
    }
    
    /**
     * Get the next language in the cycle (for toggle functionality)
     */
    fun getNextLanguage(currentLanguage: String): String {
        val currentIndex = SUPPORTED_LANGUAGES.indexOf(currentLanguage)
        return if (currentIndex == -1 || currentIndex == SUPPORTED_LANGUAGES.size - 1) {
            SUPPORTED_LANGUAGES[0]
        } else {
            SUPPORTED_LANGUAGES[currentIndex + 1]
        }
    }
}
