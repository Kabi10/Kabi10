package com.senthapps.slagrimarket.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Persists the last-used listing values for QuickListing pre-fill.
 * Written on successful post, read on QuickListingScreen open.
 */
class LastUsedPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val LAST_CROP_TYPE = stringPreferencesKey("last_crop_type")
        private val LAST_PRICE = stringPreferencesKey("last_price")
        private val LAST_LOCATION = stringPreferencesKey("last_location")
    }

    suspend fun saveLastListing(cropType: String, price: String, location: String) {
        dataStore.edit { prefs ->
            if (cropType.isNotBlank()) prefs[LAST_CROP_TYPE] = cropType
            if (price.isNotBlank()) prefs[LAST_PRICE] = price
            if (location.isNotBlank()) prefs[LAST_LOCATION] = location
        }
    }

    fun getLastCropType(): Flow<String> =
        dataStore.data.map { it[LAST_CROP_TYPE] ?: "" }

    fun getLastPrice(): Flow<String> =
        dataStore.data.map { it[LAST_PRICE] ?: "" }

    fun getLastLocation(): Flow<String> =
        dataStore.data.map { it[LAST_LOCATION] ?: "" }
}
