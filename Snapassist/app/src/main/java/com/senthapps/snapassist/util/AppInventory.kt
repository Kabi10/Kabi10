package com.senthapps.snapassist.util

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * AppInventory provides functionality to report installed applications and 
 * current foreground app to Firestore.
 */
object AppInventory {
    
    private const val TAG = "AppInventory"
    private const val MAX_APPS_TO_REPORT = 500 // Limit to prevent huge documents
    private const val USAGE_STATS_QUERY_MINUTES = 5 // Look back 5 minutes for foreground app
    
    /**
     * Generate and upload a report of installed apps and current foreground app
     */
    fun reportInstalledAndTopApp(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting app inventory report generation")
                
                // Get installed apps (filtered by package visibility)
                val installedApps = getInstalledApps(context)
                Log.d(TAG, "Found ${installedApps.size} installed apps")
                
                // Get current/foreground app using UsageStats
                val topApp = getCurrentForegroundApp(context)
                Log.d(TAG, "Current foreground app: $topApp")
                
                // Upload report to Firestore
                uploadReport(context, installedApps, topApp)
                
            } catch (e: Exception) {
                Log.e(TAG, "Error generating app inventory report", e)
            }
        }
    }
    
    /**
     * Get list of installed applications visible to this app
     */
    private fun getInstalledApps(context: Context): List<String> {
        return try {
            val packageManager = context.packageManager
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            
            installedApps
                .filter { appInfo ->
                    // Filter out system apps if desired (optional)
                    // For now, include all visible apps
                    true
                }
                .map { it.packageName }
                .sorted()
                .take(MAX_APPS_TO_REPORT) // Limit to prevent huge reports
                
        } catch (e: Exception) {
            Log.e(TAG, "Error getting installed apps", e)
            emptyList()
        }
    }
    
    /**
     * Get the current foreground app using UsageStatsManager.
     * Requires PACKAGE_USAGE_STATS permission and Usage Access grant.
     */
    private fun getCurrentForegroundApp(context: Context): String? {
        return try {
            // Check if usage access is granted
            if (!UsageAccess.isGranted(context)) {
                Log.w(TAG, "Usage access not granted, cannot determine foreground app")
                return null
            }
            
            val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val endTime = System.currentTimeMillis()
            val startTime = endTime - (USAGE_STATS_QUERY_MINUTES * 60 * 1000) // 5 minutes ago
            
            val usageEvents = usageStatsManager.queryEvents(startTime, endTime)
            var foregroundApp: String? = null
            
            val event = UsageEvents.Event()
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event)
                
                // Look for the most recent MOVE_TO_FOREGROUND event
                if (event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    foregroundApp = event.packageName
                }
            }
            
            Log.d(TAG, "Most recent foreground app: $foregroundApp")
            foregroundApp
            
        } catch (e: Exception) {
            Log.e(TAG, "Error getting current foreground app", e)
            null
        }
    }
    
    /**
     * Upload the app report to Firestore
     */
    private suspend fun uploadReport(context: Context, installedApps: List<String>, topApp: String?) {
        try {
            val firestore = FirebaseFirestore.getInstance()
            val deviceId = "primary" // Could be made configurable
            
            val reportData = mapOf(
                "timestamp" to FieldValue.serverTimestamp(),
                "installed" to installedApps,
                "installedCount" to installedApps.size,
                "topApp" to topApp,
                "deviceInfo" to mapOf(
                    "manufacturer" to android.os.Build.MANUFACTURER,
                    "model" to android.os.Build.MODEL,
                    "androidVersion" to android.os.Build.VERSION.RELEASE
                ),
                "reportVersion" to "1.0"
            )
            
            val documentRef = firestore
                .collection("devices")
                .document(deviceId)
                .collection("reports")
                .document("installed")
            
            documentRef.set(reportData).await()
            
            Log.d(TAG, "App inventory report uploaded successfully")
            Log.d(TAG, "Report contains ${installedApps.size} apps, top app: $topApp")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error uploading app report to Firestore", e)
            throw e
        }
    }
    
    /**
     * Get a summary of installed apps for debugging
     */
    fun getInstalledAppsSummary(context: Context): String {
        return try {
            val apps = getInstalledApps(context)
            val topApp = getCurrentForegroundApp(context)
            
            buildString {
                appendLine("Installed Apps Summary:")
                appendLine("Total apps: ${apps.size}")
                appendLine("Current foreground: $topApp")
                appendLine("Usage access granted: ${UsageAccess.isGranted(context)}")
                appendLine()
                appendLine("Sample apps (first 10):")
                apps.take(10).forEach { app ->
                    appendLine("  - $app")
                }
                if (apps.size > 10) {
                    appendLine("  ... and ${apps.size - 10} more")
                }
            }
        } catch (e: Exception) {
            "Error generating summary: ${e.message}"
        }
    }
}