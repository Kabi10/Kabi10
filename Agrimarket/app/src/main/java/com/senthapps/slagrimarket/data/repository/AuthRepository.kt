package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.BuildConfig
import com.senthapps.slagrimarket.data.api.AuthApiService
import com.senthapps.slagrimarket.data.api.SendOtpRequest
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
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApiService: AuthApiService,
    private val authPreferences: AuthPreferences,
    private val userDao: UserDao
) {

    // MVP: Provide mock user for demonstration
    private val mockUser = User(
        id = "mvp_user_001",
        phone = "0771234567",
        name = "Demo User",
        userType = UserType.FARMER,
        verified = true,
        language = "ta",
        createdAt = java.time.Instant.now().toString()
    )

    // MVP: Use AuthPreferences for consistent authentication state
    val currentUser: Flow<User?> = authPreferences.currentUser
    val isLoggedIn: Flow<Boolean> = authPreferences.isLoggedIn

    init {
        // MVP: Initialize mock authentication state on app start
        initializeMockAuthState()
    }

    private fun initializeMockAuthState() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Check if user is already authenticated
                val existingUser = authPreferences.getCurrentUser()
                val existingToken = authPreferences.getAccessToken()

                if (existingUser == null || existingToken.isNullOrBlank()) {
                    // Initialize mock authentication state
                    val mockAccessToken = "mvp_access_token_${System.currentTimeMillis()}"
                    val mockRefreshToken = "mvp_refresh_token_${System.currentTimeMillis()}"

                    // Save mock authentication data
                    authPreferences.saveTokens(mockAccessToken, mockRefreshToken)
                    authPreferences.saveUser(mockUser)
                    userDao.insertUser(mockUser)

                    Timber.d("🚀 MVP: Mock authentication state initialized - User: ${mockUser.name}")
                } else {
                    Timber.d("🚀 MVP: Existing authentication state found - User: ${existingUser.name}")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error initializing mock authentication state")
            }
        }
    }
    
    suspend fun sendOtp(phoneNumber: String): Result<String> {
        return try {
            val response = authApiService.sendOtp(SendOtpRequest(phoneNumber))
            if (response.isSuccessful && response.body()?.success == true) {
                val responseBody = response.body()!!
                val otpId = responseBody.otpId ?: ""

                // Log OTP for development testing
                responseBody.otp?.let { otp ->
                    Timber.d("🔑 DEVELOPMENT OTP: $otp (Phone: $phoneNumber)")
                    android.util.Log.d("AUTH_OTP", "🔑 OTP CODE: $otp for $phoneNumber")
                }

                Result.success(otpId)
            } else {
                val errorMessage = response.body()?.message ?: "Failed to send OTP"
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Timber.e(e, "Error sending OTP")
            Result.failure(e)
        }
    }
    
    suspend fun verifyOtp(phoneNumber: String, otp: String, otpId: String? = null): Result<User> {
        return try {
            val response = authApiService.verifyOtp(VerifyOtpRequest(phoneNumber, otp, otpId))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val accessToken = body.token!!
                val refreshToken = body.refreshToken!!
                val user = body.user!!
                
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
        // MVP: Return mock user for demonstration
        return mockUser
    }
    
    suspend fun refreshToken(): Result<String> {
        return try {
            val refreshToken = authPreferences.getRefreshToken()
            if (refreshToken.isNullOrBlank()) {
                return Result.failure(Exception("No refresh token available"))
            }
            
            // TODO: Implement refresh token API call
            // For now, just return failure to trigger re-login
            Result.failure(Exception("Token refresh not implemented"))
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing token")
            Result.failure(e)
        }
    }
    
    suspend fun isUserLoggedIn(): Boolean {
        val token = authPreferences.getAccessToken()
        return !token.isNullOrBlank()
    }

    /**
     * 🚨 TEMPORARY DEVELOPMENT BYPASS METHOD 🚨
     * This method bypasses OTP verification for development and testing purposes.
     * It creates a mock user and authentication state without server verification.
     *
     * ⚠️ WARNING: This should NEVER be enabled in production builds!
     *
     * @param phoneNumber The phone number to associate with the mock user
     * @param name The name for the mock user
     * @param userType The type of user (FARMER or BUYER)
     * @return Result containing the mock user data
     */
    suspend fun bypassOtpWithMockUser(phoneNumber: String, name: String, userType: UserType): Result<User> {
        return try {
            // MVP: Always allow bypass for demonstration
            Timber.d("Creating mock user for MVP demonstration")

            Timber.w("🚨 CREATING MOCK USER FOR DEVELOPMENT BYPASS 🚨")

            // Create mock user data
            val mockUser = User(
                id = UUID.randomUUID().toString(),
                phone = phoneNumber,
                name = name,
                userType = userType,
                verified = true,
                language = "ta", // Tamil by default
                createdAt = java.time.Instant.now().toString()
            )

            // Generate mock tokens for development
            val mockAccessToken = "dev_access_token_${System.currentTimeMillis()}"
            val mockRefreshToken = "dev_refresh_token_${System.currentTimeMillis()}"

            // Save mock authentication data
            authPreferences.saveTokens(mockAccessToken, mockRefreshToken)
            authPreferences.saveUser(mockUser)
            userDao.insertUser(mockUser)

            Timber.d("🚨 BYPASS: Mock user created successfully - Name: ${mockUser.name}, Type: ${mockUser.userType}")

            Result.success(mockUser)
        } catch (e: Exception) {
            Timber.e(e, "Error creating mock user for bypass")
            Result.failure(e)
        }
    }
}
