package com.senthapps.slagrimarket.data.network

import com.senthapps.slagrimarket.BuildConfig
import com.senthapps.slagrimarket.data.api.RefreshTokenRequest
import com.senthapps.slagrimarket.data.api.RefreshTokenResponse
import com.senthapps.slagrimarket.data.preferences.AuthPreferences
import com.squareup.moshi.Moshi
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    private val authPreferences: AuthPreferences,
    private val moshi: Moshi
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Limit retry count to prevent infinite loops
        if (responseCount(response) >= 3) {
            return null
        }

        Timber.d("TokenAuthenticator: 401 Unauthorized detected. Attempting refresh...")

        val refreshToken = runBlocking { authPreferences.getRefreshToken() }
        if (refreshToken.isNullOrBlank()) {
            Timber.w("TokenAuthenticator: No refresh token available")
            return null
        }

        // Perform synchronous token refresh
        val newToken = refreshAccessToken(refreshToken) ?: return null

        // Retry the request with the new token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newToken")
            .build()
    }

    private fun refreshAccessToken(refreshToken: String): String? {
        return try {
            // Create a separate client to avoid circular dependency and interceptors
            val client = OkHttpClient()
            
            val adapter = moshi.adapter(RefreshTokenRequest::class.java)
            val requestBody = adapter.toJson(RefreshTokenRequest(refreshToken))
                .toRequestBody("application/json".toMediaType())

            val request = Request.Builder()
                .url("${BuildConfig.BASE_URL}auth/refresh-token.js") // Ensure .js extension for Vercel
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            
            if (response.isSuccessful) {
                val responseBody = response.body?.string()
                if (responseBody != null) {
                    val responseAdapter = moshi.adapter(RefreshTokenResponse::class.java)
                    val refreshResponse = responseAdapter.fromJson(responseBody)
                    
                    if (refreshResponse?.success == true && refreshResponse.token != null) {
                        val newToken = refreshResponse.token
                        // Save new tokens synchronously
                        runBlocking {
                            // If backend returns a new refresh token, save it too
                            val newRefreshToken = refreshResponse.refreshToken ?: refreshToken
                            authPreferences.saveTokens(newToken, newRefreshToken)
                        }
                        Timber.d("TokenAuthenticator: Token refreshed successfully")
                        return newToken
                    }
                }
            }
            
            Timber.e("TokenAuthenticator: Refresh failed with code ${response.code}")
            null
        } catch (e: Exception) {
            Timber.e(e, "TokenAuthenticator: Exception during refresh")
            null
        }
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
