package com.senthapps.snapassist.util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.senthapps.snapassist.util.AppVisibility

/**
 * LaunchController handles policy-compliant app launching according to Android 10+ restrictions.
 * 
 * Background activity start restrictions:
 * - Android 10+ blocks background activity starts from services
 * - Foreground services still count as "background" for this purpose
 * - Solution: Use notification with PendingIntent when app is in background
 */
object LaunchController {
    
    private const val TAG = "LaunchController"
    
    /**
     * Request to launch an app package.
     * If the app is in foreground, launches immediately.
     * If in background, shows a notification for user to tap.
     */
    fun requestLaunch(context: Context, pkg: String) {
        try {
            val packageManager = context.packageManager
            val launchIntent = packageManager.getLaunchIntentForPackage(pkg)
            
            if (launchIntent == null) {
                Log.w(TAG, "No launchable activity found for package: $pkg")
                showErrorNotification(context, "App not found", "Cannot find app: $pkg")
                return
            }
            
            // Add flags for new task
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            
            if (AppVisibility.isForeground(context)) {
                // Safe to launch directly when app is in foreground
                Log.d(TAG, "App is foreground, launching $pkg directly")
                context.startActivity(launchIntent)
            } else {
                // Post notification with PendingIntent when in background
                Log.d(TAG, "App is background, showing notification for $pkg")
                showLaunchNotification(context, pkg, launchIntent)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error launching package: $pkg", e)
            showErrorNotification(context, "Launch failed", "Failed to launch: $pkg")
        }
    }
    
    /**
     * Show a notification that allows user to launch the target app
     */
    private fun showLaunchNotification(context: Context, pkg: String, launchIntent: Intent) {
        try {
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            val pendingIntent = PendingIntent.getActivity(context, 0, launchIntent, flags)
            
            // Get app name for display
            val packageManager = context.packageManager
            val appName = try {
                val appInfo = packageManager.getApplicationInfo(pkg, 0)
                packageManager.getApplicationLabel(appInfo).toString()
            } catch (e: Exception) {
                pkg // Fallback to package name
            }
            
            val notification = Notifs.highPriority(
                ctx = context,
                title = "Launch requested",
                text = "Tap to open $appName",
                contentIntent = pendingIntent
            )
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(pkg.hashCode(), notification)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error showing launch notification for $pkg", e)
        }
    }
    
    /**
     * Show an error notification when launch fails
     */
    private fun showErrorNotification(context: Context, title: String, message: String) {
        try {
            val notification = Notifs.highPriority(
                ctx = context,
                title = title,
                text = message,
                contentIntent = null
            )
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(title.hashCode(), notification)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing error notification", e)
        }
    }
}