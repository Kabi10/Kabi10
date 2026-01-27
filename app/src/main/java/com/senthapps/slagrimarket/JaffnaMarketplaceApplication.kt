package com.senthapps.slagrimarket

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class JaffnaMarketplaceApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // Application-scoped coroutine scope for background work
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging (lightweight, stays on main thread)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Move heavy initialization off the main thread to reduce startup jank
        applicationScope.launch {
            // DEBUG: Initialize Firebase Anonymous Auth for Storage uploads
            if (BuildConfig.DEBUG) {
                initializeDebugFirebaseAuth()
            }

            // Initialize Firebase Crashlytics
            initializeCrashlytics()
        }

        // Create notification channels (lightweight, can stay on main thread)
        createNotificationChannels()

        Timber.d("Jaffna Marketplace Application started")
    }

    /**
     * Initialize Firebase Anonymous Authentication for DEBUG builds
     * This allows Firebase Storage uploads to work without real phone/OTP auth
     * Note: Requires Anonymous Authentication to be enabled in Firebase Console
     */
    private fun initializeDebugFirebaseAuth() {
        try {
            val auth = FirebaseAuth.getInstance()

            // Check if already signed in
            if (auth.currentUser != null) {
                Timber.d("🔧 DEBUG: Firebase user already signed in: ${auth.currentUser?.uid}")
                return
            }

            // Sign in anonymously - this is optional and may fail if not configured
            auth.signInAnonymously()
                .addOnSuccessListener { result ->
                    Timber.d("🔧 DEBUG: Firebase anonymous auth successful: ${result.user?.uid}")
                }
                .addOnFailureListener { e ->
                    // Log at debug level - this is expected if Anonymous Auth is not enabled
                    Timber.d("🔧 DEBUG: Firebase anonymous auth not available (enable in Firebase Console if needed): ${e.message}")
                }
        } catch (e: Exception) {
            // Gracefully handle any Firebase initialization errors
            Timber.d("🔧 DEBUG: Firebase auth initialization skipped: ${e.message}")
        }
    }

    /**
     * Initialize Firebase Crashlytics for crash reporting
     * - Enabled in release builds for production crash monitoring
     * - Disabled in debug builds to avoid polluting crash data
     */
    private fun initializeCrashlytics() {
        val crashlytics = FirebaseCrashlytics.getInstance()

        // Disable Crashlytics in debug builds
        crashlytics.isCrashlyticsCollectionEnabled = !BuildConfig.DEBUG

        if (!BuildConfig.DEBUG) {
            // Set custom keys for easier debugging in production
            crashlytics.setCustomKey("app_version", BuildConfig.VERSION_NAME)
            crashlytics.setCustomKey("build_type", "release")
            Timber.d("✅ Firebase Crashlytics initialized for production")
        } else {
            Timber.d("⚠️ Firebase Crashlytics disabled in debug build")
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.DEBUG)
            .build()

    /**
     * Create notification channels for Android O and above
     */
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            // Sync notification channel
            val syncChannel = NotificationChannel(
                CHANNEL_ID_SYNC,
                "Data Synchronization",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifications for background data synchronization"
                setShowBadge(false)
            }

            // General notifications channel
            val generalChannel = NotificationChannel(
                CHANNEL_ID_GENERAL,
                "General Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }

            // Order updates channel
            val orderChannel = NotificationChannel(
                CHANNEL_ID_ORDERS,
                "Order Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for order status updates"
            }

            notificationManager.createNotificationChannels(
                listOf(syncChannel, generalChannel, orderChannel)
            )

            Timber.d("Notification channels created")
        }
    }

    companion object {
        const val CHANNEL_ID_SYNC = "sync_channel"
        const val CHANNEL_ID_GENERAL = "general_channel"
        const val CHANNEL_ID_ORDERS = "orders_channel"
    }
}
