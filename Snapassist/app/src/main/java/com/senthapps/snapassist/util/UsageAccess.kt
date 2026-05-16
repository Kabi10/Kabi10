package com.senthapps.snapassist.util

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import android.os.Process

/**
 * UsageAccess utility for managing Usage Access permissions.
 * This permission is required to detect the current foreground app using UsageStatsManager.
 */
object UsageAccess {
    
    private const val TAG = "UsageAccess"
    
    /**
     * Check if Usage Access permission is granted for this app
     */
    fun isGranted(context: Context): Boolean {
        return try {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(
                "android:get_usage_stats",
                Process.myUid(),
                context.packageName
            )
            
            val granted = mode == AppOpsManager.MODE_ALLOWED
            Log.d(TAG, "Usage Access permission granted: $granted")
            granted
            
        } catch (e: Exception) {
            Log.e(TAG, "Error checking Usage Access permission", e)
            false
        }
    }
    
    /**
     * Ensure Usage Access is granted. If not, redirect user to settings.
     * @return true if already granted, false if user needs to grant it
     */
    fun ensureGranted(context: Context): Boolean {
        val isAlreadyGranted = isGranted(context)
        
        if (!isAlreadyGranted) {
            Log.i(TAG, "Usage Access not granted, redirecting user to settings")
            openUsageAccessSettings(context)
        }
        
        return isAlreadyGranted
    }
    
    /**
     * Open the Usage Access settings screen where user can grant permission
     */
    fun openUsageAccessSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
            Log.d(TAG, "Opened Usage Access settings")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error opening Usage Access settings", e)
            
            // Fallback: try opening general application settings
            try {
                val fallbackIntent = Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                Log.d(TAG, "Opened fallback application settings")
            } catch (fallbackError: Exception) {
                Log.e(TAG, "Error opening fallback settings", fallbackError)
            }
        }
    }
    
    /**
     * Get a user-friendly explanation of why Usage Access is needed
     */
    fun getPermissionExplanation(): String {
        return buildString {
            appendLine("📱 Usage Access Permission Required")
            appendLine()
            appendLine("Snapassist needs Usage Access permission to:")
            appendLine("• Detect which app is currently in the foreground")
            appendLine("• Generate app usage reports when requested")
            appendLine("• Provide accurate device status information")
            appendLine()
            appendLine("This permission allows reading app usage statistics")
            appendLine("but does not access personal data or app content.")
            appendLine()
            appendLine("To grant this permission:")
            appendLine("1. Tap 'Open Settings' below")
            appendLine("2. Find 'Snapassist' in the app list")
            appendLine("3. Toggle the permission ON")
            appendLine("4. Return to this app")
        }
    }
    
    /**
     * Show a notification prompting user to grant Usage Access
     */
    fun showPermissionPrompt(context: Context) {
        try {
            val explanation = getPermissionExplanation()
            
            // Create intent to open usage access settings
            val settingsIntent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val pendingIntent = android.app.PendingIntent.getActivity(
                context,
                0,
                settingsIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = Notifs.highPriority(
                ctx = context,
                title = "Permission Required",
                text = "Tap to grant Usage Access for app reporting",
                contentIntent = pendingIntent
            )
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify("usage_access_prompt".hashCode(), notification)
            
            Log.d(TAG, "Usage Access permission prompt notification shown")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing Usage Access permission prompt", e)
        }
    }
    
    /**
     * Get status information about Usage Access permission
     */
    fun getStatusInfo(context: Context): String {
        return try {
            val isGranted = isGranted(context)
            buildString {
                appendLine("Usage Access Status:")
                appendLine("Permission granted: $isGranted")
                appendLine("Package: ${context.packageName}")
                appendLine("UID: ${Process.myUid()}")
                
                if (!isGranted) {
                    appendLine()
                    appendLine("To grant permission, go to:")
                    appendLine("Settings > Apps > Special app access > Usage access")
                    appendLine("Find Snapassist and toggle it ON")
                }
            }
        } catch (e: Exception) {
            "Error getting status: ${e.message}"
        }
    }
}