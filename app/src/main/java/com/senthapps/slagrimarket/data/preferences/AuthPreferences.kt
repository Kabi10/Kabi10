package com.senthapps.slagrimarket.data.preferences

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.senthapps.slagrimarket.data.model.User
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    
    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    private val userAdapter = moshi.adapter(User::class.java)
    
    private val _currentUser = MutableStateFlow<User?>(getCurrentUser())
    val currentUser: Flow<User?> = _currentUser.asStateFlow()
    
    private val _isLoggedIn = MutableStateFlow(sharedPreferences.getString(KEY_ACCESS_TOKEN, null) != null)
    val isLoggedIn: Flow<Boolean> = _isLoggedIn.asStateFlow()
    
    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "current_user"
        private const val KEY_PHONE = "phone_number"
    }
    
    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
        _isLoggedIn.value = true
        Timber.d("Tokens saved successfully")
    }
    
    suspend fun saveUser(user: User) {
        try {
            val userJson = userAdapter.toJson(user)
            sharedPreferences.edit()
                .putString(KEY_USER, userJson)
                .putString(KEY_PHONE, user.phone)
                .apply()
            _currentUser.value = user
            Timber.d("User saved: ${user.name}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to save user")
        }
    }
    
    suspend fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    suspend fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    fun getCurrentUser(): User? {
        return try {
            val userJson = sharedPreferences.getString(KEY_USER, null)
            if (userJson != null) {
                userAdapter.fromJson(userJson)
            } else null
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current user")
            null
        }
    }
    
    suspend fun getPhoneNumber(): String? {
        return sharedPreferences.getString(KEY_PHONE, null)
    }
    
    suspend fun clearAuth() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER)
            .remove(KEY_PHONE)
            .apply()
        _currentUser.value = null
        _isLoggedIn.value = false
        Timber.d("Auth data cleared")
    }
    
    suspend fun updateAccessToken(newToken: String) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, newToken)
            .apply()
        Timber.d("Access token updated")
    }
}
