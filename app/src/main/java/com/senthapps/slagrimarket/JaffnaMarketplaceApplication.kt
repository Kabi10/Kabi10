package com.senthapps.slagrimarket

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class JaffnaMarketplaceApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Create notification channels
        createNotificationChannels()

        Timber.d("Jaffna Marketplace Application started")
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
