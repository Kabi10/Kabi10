package com.senthapps.slagrimarket.data.network

import com.senthapps.slagrimarket.data.preferences.AuthPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip auth for auth endpoints
        if (originalRequest.url.encodedPath.contains("/auth/")) {
            return chain.proceed(originalRequest)
        }
        
        val token = runBlocking { authPreferences.getAccessToken() }
        
        if (token.isNullOrBlank()) {
            Timber.w("No access token available for request: ${originalRequest.url}")
            return chain.proceed(originalRequest)
        }
        
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        
        return chain.proceed(authenticatedRequest)
    }
}
