package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.BuildConfig
import com.senthapps.slagrimarket.data.api.AuthApiService
import com.senthapps.slagrimarket.data.api.LoginRequest
import com.senthapps.slagrimarket.data.api.RegisterRequest
import com.senthapps.slagrimarket.data.api.SendOtpRequest
import com.senthapps.slagrimarket.data.api.UpdateProfileRequest
import com.senthapps.slagrimarket.data.api.UserApiService
import com.senthapps.slagrimarket.data.api.VerifyOtpRequest
import com.senthapps.slagrimarket.data.dao.UserDao
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.preferences.AuthPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val userApiService: UserApiService,
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao
) {
    // Debug mode uses BuildConfig.DEBUG directly
    private val isDebugMode: Boolean = BuildConfig.DEBUG

    // Production: Use AuthPreferences for consistent authentication state
    // DEBUG BYPASS: Return fake user flow in debug builds
    val currentUser: Flow<User?> = if (isDebugMode) {
        kotlinx.coroutines.flow.flowOf(
            User(
                id = "debug_user_123",
                name = "Debug Farmer",
                phone = "+94771234567",
                userType = UserType.FARMER,
                verified = true,
                language = "en",
                createdAt = "2025-01-04T00:00:00Z"
            )
        )
    } else {
        authPreferences.currentUser
    }

    val isLoggedIn: Flow<Boolean> = if (isDebugMode) {
        kotlinx.coroutines.flow.flowOf(true)
    } else {
        authPreferences.isLoggedIn
    }

    init {
        // DEBUG: Persist fake user to database to satisfy FK constraints
        if (isDebugMode) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val debugUser = User(
                        id = "debug_user_123",
                        name = "Debug Farmer",
                        phone = "+94771234567",
                        userType = UserType.FARMER,
                        verified = true,
                        language = "en",
                        createdAt = "2025-01-04T00:00:00Z"
                    )
                    userDao.insertUser(debugUser)
                    Timber.d("🔧 DEBUG: Persisted debug user to database for FK constraints")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to persist debug user (may already exist)")
                }
            }
        }

        // Log authentication state on app start for debugging
        logAuthenticationState()
    }

    private fun logAuthenticationState() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val existingUser = authPreferences.getCurrentUser()
                val hasToken = !authPreferences.getAccessToken().isNullOrBlank()

                if (existingUser != null && hasToken) {
                    Timber.d("✅ Authenticated user found: ${existingUser.name}")
                } else {
                    Timber.d("⚠️ No authenticated user - login required")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error checking authentication state")
            }
        }
    }
    
    data class OtpResult(val otpId: String, val otp: String?)

    suspend fun sendOtp(phoneNumber: String): Result<OtpResult> {
        return try {
            // Normalize phone number to +94XXXXXXXXX format
            val normalizedPhone = normalizePhoneNumber(phoneNumber)
            Timber.d("Sending OTP to: $normalizedPhone (original: $phoneNumber)")
            
            val response = authApiService.sendOtp(SendOtpRequest(normalizedPhone))
            if (response.isSuccessful && response.body()?.success == true) {
                val responseBody = response.body()!!
                val otpId = responseBody.otpId ?: ""
                val otpCode = responseBody.otp

                // Log OTP for development testing
                otpCode?.let { otp ->
                    Timber.d("🔑 DEVELOPMENT OTP: $otp (Phone: $normalizedPhone)")
                    android.util.Log.d("AUTH_OTP", "🔑 OTP CODE: $otp for $normalizedPhone")
                }

                Result.success(OtpResult(otpId, otpCode))
            } else {
                val errorMessage = response.body()?.message ?: "Failed to send OTP"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending OTP")
            Result.failure(e)
        }
    }
    
    /**
     * Normalize phone number to +94XXXXXXXXX format required by backend
     */
    private fun normalizePhoneNumber(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9+]"), "")
        return when {
            cleaned.startsWith("+94") -> cleaned
            cleaned.startsWith("94") -> "+$cleaned"
            cleaned.startsWith("0") -> "+94${cleaned.substring(1)}"
            else -> "+94$cleaned"
        }
    }
    
    suspend fun verifyOtp(phoneNumber: String, otp: String, otpId: String? = null): Result<User> {
        return try {
            val normalizedPhone = normalizePhoneNumber(phoneNumber)
            val response = authApiService.verifyOtp(VerifyOtpRequest(normalizedPhone, otp, otpId))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val accessToken = body.token
                val refreshToken = body.refreshToken
                val user = body.user
                
                // Check for null values
                if (accessToken == null || refreshToken == null || user == null) {
                    Timber.e("Verify OTP response missing required fields: token=${accessToken != null}, refreshToken=${refreshToken != null}, user=${user != null}")
                    return Result.failure(Exception("Authentication failed - incomplete server response"))
                }
                
                // Save tokens and user data
                authPreferences.saveTokens(accessToken, refreshToken)
                authPreferences.saveUser(user)
                userDao.insertUser(user)
                
                Result.success(user)
            } else {
                val errorMessage = response.body()?.message ?: "Invalid OTP"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error verifying OTP")
            Result.failure(e)
        }
    }
    
    suspend fun loginWithPassword(phoneNumber: String, password: String): Result<User> {
        return try {
            val normalizedPhone = normalizePhoneNumber(phoneNumber)
            val response = authApiService.login(LoginRequest(normalizedPhone, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                if (body.token == null || body.refreshToken == null || body.user == null) {
                    return Result.failure(Exception("Incomplete server response"))
                }
                authPreferences.saveTokens(body.token, body.refreshToken)
                authPreferences.saveUser(body.user)
                userDao.insertUser(body.user)
                Result.success(body.user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Login failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Login error")
            Result.failure(e)
        }
    }

    suspend fun registerWithPassword(phoneNumber: String, password: String, userType: String): Result<User> {
        return try {
            val normalizedPhone = normalizePhoneNumber(phoneNumber)
            val response = authApiService.register(RegisterRequest(normalizedPhone, password, userType))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                if (body.token == null || body.refreshToken == null || body.user == null) {
                    return Result.failure(Exception("Incomplete server response"))
                }
                authPreferences.saveTokens(body.token, body.refreshToken)
                authPreferences.saveUser(body.user)
                userDao.insertUser(body.user)
                Result.success(body.user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Registration failed"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Register error")
            Result.failure(e)
        }
    }

    suspend fun logout() {
        try {
            authPreferences.clearAuth()
            // Note: In a real app, you might want to call a logout endpoint
            Timber.d("User logged out successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error during logout")
        }
    }
    
    suspend fun getCurrentUser(): User? {
        // DEBUG BYPASS: Return fake authenticated user to skip auth flow
        if (isDebugMode) {
            Timber.d("🔧 DEBUG: Returning fake authenticated user to bypass auth")
            return User(
                id = "debug_user_123",
                name = "Debug Farmer",
                phone = "+94771234567",
                userType = UserType.FARMER,
                verified = true,
                language = "en",
                createdAt = "2025-01-04T00:00:00Z"
            )
        }

        // Return authenticated user from preferences
        return authPreferences.getCurrentUser()
    }

    suspend fun refreshToken(): Result<String> {
        return try {
            val refreshToken = authPreferences.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                return Result.failure(Exception("No refresh token available"))
            }

            val response = authApiService.refreshToken(com.senthapps.slagrimarket.data.api.RefreshTokenRequest(refreshToken))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val newToken = body.token
                val newRefreshToken = body.refreshToken ?: refreshToken // Use new one if provided, else keep old

                if (newToken == null) {
                    return Result.failure(Exception("Token refresh failed - missing access token"))
                }

                // Save new tokens
                authPreferences.saveTokens(newToken, newRefreshToken)
                Result.success(newToken)
            } else {
                val errorMessage = response.body()?.toString() ?: "Token refresh failed"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing token")
            Result.failure(e)
        }
    }

    suspend fun isUserLoggedIn(): Boolean {
        val token = authPreferences.getAccessToken()
        return !token.isNullOrBlank()
    }

    suspend fun updateUserProfile(
        userId: String,
        name: String,
        location: String
    ): Result<User> {
        return try {
            val currentUser = getCurrentUser()
            if (currentUser == null) {
                return Result.failure(Exception("User not found"))
            }

            // Update via API first
            try {
                val response = userApiService.updateProfile(
                    UpdateProfileRequest(name = name, location = location)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    Timber.d("Profile updated on server: $name, $location")
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to update profile on API, updating locally")
            }

            val updatedUser = currentUser.copy(
                name = name,
                location = location
            )

            // Update in local database
            userDao.updateUser(updatedUser)

            // Update in preferences
            authPreferences.saveUser(updatedUser)

            Timber.d("Profile updated: $name, $location")
            Result.success(updatedUser)
        } catch (e: Exception) {
            Timber.e(e, "Error updating profile")
            Result.failure(e)
        }
    }
}
