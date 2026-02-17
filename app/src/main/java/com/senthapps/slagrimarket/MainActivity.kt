package com.senthapps.slagrimarket

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.senthapps.slagrimarket.data.preferences.AccessibilityPreferences
import com.senthapps.slagrimarket.data.preferences.LanguagePreferences
import com.senthapps.slagrimarket.navigation.JaffnaMarketplaceNavigation
import com.senthapps.slagrimarket.ui.theme.LocalAppLanguage
import com.senthapps.slagrimarket.ui.theme.LocalTextScale
import com.senthapps.slagrimarket.ui.theme.SLAgrimarketTheme
import com.senthapps.slagrimarket.ui.theme.languageCodeToAppLanguage
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var languagePreferences: LanguagePreferences

    @Inject
    lateinit var accessibilityPreferences: AccessibilityPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge display safely - may not be supported on all devices
        try {
            enableEdgeToEdge()
        } catch (e: Exception) {
            Timber.d("EdgeToEdge not available on this device: ${e.message}")
        }
        setContent {
            // Collect language preference from DataStore
            val languageCode by languagePreferences.getLanguage()
                .collectAsState(initial = LanguagePreferences.DEFAULT_LANGUAGE)
            val appLanguage = languageCodeToAppLanguage(languageCode)

            // Collect accessibility preferences
            val textScale by accessibilityPreferences.getTextScale()
                .collectAsState(initial = AccessibilityPreferences.DEFAULT_SCALE)

            // Provide language and text scale globally via CompositionLocal
            CompositionLocalProvider(
                LocalAppLanguage provides appLanguage,
                LocalTextScale provides textScale
            ) {
                SLAgrimarketTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // 🔐 AUTHENTICATION CONTROL
                        // TODO: Enable authentication when SMS/OTP backend is ready
                        // For now, skip auth in all builds to allow Play Store testing
                        val requireAuth = false // Will be: !BuildConfig.DEBUG
                        if (!requireAuth) {
                            Timber.d("⚠️  Authentication disabled - SMS/OTP backend not ready")
                        }
                        JaffnaMarketplaceNavigation(startWithAuth = requireAuth)
                    }
                }
            }
        }
    }
}