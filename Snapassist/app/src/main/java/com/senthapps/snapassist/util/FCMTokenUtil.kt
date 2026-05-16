package com.senthapps.snapassist.util

import android.content.Context
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility for managing FCM token storage and rotation in Firestore
 */
object FCMTokenUtil {
    
    private const val TAG = "FCMTokenUtil"
    private const val COLLECTION_DEVICES = "devices"
    
    /**
     * Device information data class
     */
    data class DeviceInfo(
        val token: String,
        val deviceId: String,
        val deviceModel: String,
        val deviceManufacturer: String,
        val androidVersion: String,
        val appVersion: String,
        val userId: String,
        val lastUpdated: String,
        val isActive: Boolean = true
    )
    
    /**
     * Get current FCM token and save to Firestore
     */
    suspend fun updateTokenInFirestore(context: Context): String? {
        return try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                Log.w(TAG, "User not authenticated, cannot save token")
                return null
            }
            
            // Get current FCM token
            val token = FirebaseMessaging.getInstance().token.await()
            Log.d(TAG, "Retrieved FCM token: $token")
            
            // Save to Firestore
            saveTokenToFirestore(context, token, currentUser.uid)
            
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update token in Firestore", e)
            null
        }
    }
    
    /**
     * Save FCM token to Firestore with device information
     */
    private suspend fun saveTokenToFirestore(context: Context, token: String, userId: String) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val deviceId = getDeviceId(context)
            
            val deviceInfo = DeviceInfo(
                token = token,
                deviceId = deviceId,
                deviceModel = Build.MODEL,
                deviceManufacturer = Build.MANUFACTURER,
                androidVersion = Build.VERSION.RELEASE,
                appVersion = "1.0", // TODO: Get from BuildConfig
                userId = userId,
                lastUpdated = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                isActive = true
            )
            
            // Use device ID as document ID to ensure uniqueness per device
            firestore.collection(COLLECTION_DEVICES)
                .document(deviceId)
                .set(deviceInfo)
                .await()
            
            Log.d(TAG, "Successfully saved token to Firestore for device: $deviceId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save token to Firestore", e)
            throw e
        }
    }
    
    /**
     * Mark device as inactive in Firestore
     */
    suspend fun markDeviceInactive(context: Context) {
        try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser ?: return
            
            val firestore = FirebaseFirestore.getInstance()
            val deviceId = getDeviceId(context)
            
            firestore.collection(COLLECTION_DEVICES)
                .document(deviceId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "lastUpdated" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    )
                )
                .await()
            
            Log.d(TAG, "Marked device as inactive: $deviceId")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark device as inactive", e)
        }
    }
    
    /**
     * Get all active tokens for the current user
     */
    suspend fun getUserActiveTokens(): List<String> {
        return try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser == null) {
                Log.w(TAG, "User not authenticated")
                return emptyList()
            }
            
            val firestore = FirebaseFirestore.getInstance()
            val querySnapshot = firestore.collection(COLLECTION_DEVICES)
                .whereEqualTo("userId", currentUser.uid)
                .whereEqualTo("isActive", true)
                .get()
                .await()
            
            val tokens = querySnapshot.documents.mapNotNull { doc ->
                doc.getString("token")
            }
            
            Log.d(TAG, "Retrieved ${tokens.size} active tokens for user")
            tokens
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user active tokens", e)
            emptyList()
        }
    }
    
    /**
     * Get unique device identifier
     */
    private fun getDeviceId(context: Context): String {
        return try {
            // Use Android ID as device identifier
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "unknown_device_${System.currentTimeMillis()}"
        } catch (e: Exception) {
            Log.w(TAG, "Failed to get device ID", e)
            "unknown_device_${System.currentTimeMillis()}"
        }
    }
    
    /**
     * Handle token rotation - called when FCM token changes
     */
    suspend fun handleTokenRotation(context: Context, newToken: String) {
        try {
            Log.d(TAG, "Handling token rotation to: $newToken")
            
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            
            if (currentUser != null) {
                saveTokenToFirestore(context, newToken, currentUser.uid)
                Log.d(TAG, "Token rotation completed successfully")
            } else {
                Log.w(TAG, "User not authenticated during token rotation")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle token rotation", e)
        }
    }
    
    /**
     * Clean up old/inactive tokens for the current user
     */
    suspend fun cleanupOldTokens(maxAge: Long = 30 * 24 * 60 * 60 * 1000L) { // 30 days
        try {
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser ?: return
            
            val firestore = FirebaseFirestore.getInstance()
            val cutoffTime = System.currentTimeMillis() - maxAge
            
            // This is a simplified cleanup - in production you'd want more sophisticated logic
            Log.d(TAG, "Cleanup old tokens functionality - implement based on your needs")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cleanup old tokens", e)
        }
    }
}