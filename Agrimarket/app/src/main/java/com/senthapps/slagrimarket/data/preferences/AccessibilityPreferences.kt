package com.senthapps.slagrimarket.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AccessibilityPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val LARGE_TEXT_KEY = booleanPreferencesKey("large_text_enabled")
        private val TEXT_SCALE_KEY = floatPreferencesKey("text_scale_factor")
        private val HIGH_CONTRAST_KEY = booleanPreferencesKey("high_contrast_enabled")
        private val FIELD_MODE_KEY = booleanPreferencesKey("field_mode_enabled")

        const val DEFAULT_SCALE = 1.0f
        const val LARGE_SCALE = 1.35f
    }

    suspend fun setLargeTextEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[LARGE_TEXT_KEY] = enabled
            preferences[TEXT_SCALE_KEY] = if (enabled) LARGE_SCALE else DEFAULT_SCALE
        }
    }

    suspend fun setHighContrastEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[HIGH_CONTRAST_KEY] = enabled
        }
    }

    fun isLargeTextEnabled(): Flow<Boolean> =
        dataStore.data.map { it[LARGE_TEXT_KEY] ?: false }

    fun getTextScale(): Flow<Float> =
        dataStore.data.map { it[TEXT_SCALE_KEY] ?: DEFAULT_SCALE }

    fun isHighContrastEnabled(): Flow<Boolean> =
        dataStore.data.map { it[HIGH_CONTRAST_KEY] ?: false }

    suspend fun setFieldModeEnabled(enabled: Boolean) {
        dataStore.edit { it[FIELD_MODE_KEY] = enabled }
    }

    fun isFieldModeEnabled(): Flow<Boolean> =
        dataStore.data.map { it[FIELD_MODE_KEY] ?: false }
}
