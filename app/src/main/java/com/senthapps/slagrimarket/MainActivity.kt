package com.senthapps.slagrimarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.senthapps.slagrimarket.navigation.JaffnaMarketplaceNavigation
import com.senthapps.slagrimarket.ui.theme.SLAgrimarketTheme
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display safely - may not be supported on all devices
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Timber.d("EdgeToEdge not available on this device: ${e.message}")
        }
        setContent {
            SLAgrimarketTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 🔐 AUTHENTICATION CONTROL
                    // DEBUG: Skip auth in debug builds to bypass broken backend
                    // PRODUCTION: Require authentication via phone/OTP
                    val requireAuth = !BuildConfig.DEBUG
                    if (BuildConfig.DEBUG) {
                        Timber.d("🔧 DEBUG: Skipping authentication - going straight to app")
                    }
                    JaffnaMarketplaceNavigation(startWithAuth = requireAuth)
                }
            }
        }
    }
}