package com.senthapps.snapassist.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object NetworkUtil {
    
    private const val TAG = "NetworkUtil"
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()
    
    private var connectivityManager: ConnectivityManager? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null
    
    /**
     * Initialize network monitoring
     */
    fun initialize(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    super.onAvailable(network)
                    Log.d(TAG, "Network available")
                    _isConnected.value = true
                }
                
                override fun onLost(network: Network) {
                    super.onLost(network)
                    Log.d(TAG, "Network lost")
                    _isConnected.value = false
                }
                
                override fun onCapabilitiesChanged(
                    network: Network,
                    networkCapabilities: NetworkCapabilities
                ) {
                    super.onCapabilitiesChanged(network, networkCapabilities)
                    val connected = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    Log.d(TAG, "Network capabilities changed - connected: $connected")
                    _isConnected.value = connected
                }
            }
            
            val networkRequest = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
                
            connectivityManager?.registerNetworkCallback(networkRequest, networkCallback!!)
        }
        
        // Initial check
        _isConnected.value = isNetworkAvailable(context)
    }
    
    /**
     * Check if network is currently available
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            @Suppress("DEPRECATION")
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            activeNetworkInfo?.isConnectedOrConnecting == true
        }
    }
    
    /**
     * Cleanup network monitoring
     */
    fun cleanup() {
        networkCallback?.let { callback ->
            connectivityManager?.unregisterNetworkCallback(callback)
        }
        networkCallback = null
        connectivityManager = null
    }
}

/**
 * Retry mechanism with exponential backoff
 */
class RetryUtil {
    
    companion object {
        private const val TAG = "RetryUtil"
        
        /**
         * Execute operation with retry and exponential backoff
         * 
         * @param maxRetries Maximum number of retry attempts
         * @param initialDelayMs Initial delay between retries
         * @param maxDelayMs Maximum delay between retries
         * @param operation Suspend function to retry
         * @return Result of the operation
         */
        suspend fun <T> withRetry(
            maxRetries: Int = 3,
            initialDelayMs: Long = 1000,
            maxDelayMs: Long = 30000,
            operation: suspend () -> T
        ): T {
            var lastException: Exception? = null
            var currentDelay = initialDelayMs
            
            repeat(maxRetries + 1) { attempt ->
                try {
                    return operation()
                } catch (e: Exception) {
                    lastException = e
                    
                    if (attempt < maxRetries) {
                        Log.w(TAG, "Attempt ${attempt + 1} failed, retrying in ${currentDelay}ms", e)
                        delay(currentDelay)
                        
                        // Exponential backoff with jitter
                        currentDelay = (currentDelay * 2).coerceAtMost(maxDelayMs)
                        // Add jitter to prevent thundering herd
                        currentDelay += (Math.random() * 1000).toLong()
                    } else {
                        Log.e(TAG, "All retry attempts failed", e)
                    }
                }
            }
            
            throw lastException ?: Exception("Retry failed with unknown error")
        }
        
        /**
         * Execute operation with network-aware retry
         * Only retries when network is available
         */
        suspend fun <T> withNetworkRetry(
            context: Context,
            maxRetries: Int = 3,
            operation: suspend () -> T
        ): T {
            return withRetry(maxRetries = maxRetries) {
                if (!NetworkUtil.isNetworkAvailable(context)) {
                    throw NetworkException("Network not available")
                }
                operation()
            }
        }
    }
}

/**
 * Custom exception for network-related errors
 */
class NetworkException(message: String) : Exception(message)