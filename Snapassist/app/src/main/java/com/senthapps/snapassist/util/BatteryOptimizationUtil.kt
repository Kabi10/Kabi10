package com.senthapps.snapassist.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi

/**
 * Utility for managing battery optimization settings to improve FCM reliability
 */
object BatteryOptimizationUtil {
    
    private const val TAG = "BatteryOptimizationUtil"
    
    /**
     * Check if the app is whitelisted from battery optimizations
     */
    fun isIgnoringBatteryOptimizations(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(context.packageName)
        } else {
            true // Not applicable on older versions
        }
    }
    
    /**
     * Check if we can request battery optimization exemption
     */
    fun canRequestBatteryOptimization(context: Context): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && 
               !isIgnoringBatteryOptimizations(context)
    }
    
    /**
     * Create intent to request battery optimization exemption
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun createBatteryOptimizationIntent(context: Context): Intent {
        return Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
    
    /**
     * Create intent to open battery optimization settings
     */
    fun createBatteryOptimizationSettingsIntent(): Intent {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
        } else {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        }
    }
    
    /**
     * Get a user-friendly explanation of why battery optimization should be disabled
     */
    fun getBatteryOptimizationExplanation(): String {
        return "To ensure reliable remote camera activation, Snapassist needs to be excluded from battery optimization. " +
                "This allows the app to receive FCM messages instantly even when running in the background. " +
                "Without this exemption, your device may delay or block remote capture commands."
    }
    
    /**
     * Get battery optimization status description
     */
    fun getBatteryOptimizationStatus(context: Context): String {
        return if (isIgnoringBatteryOptimizations(context)) {
            "✓ Battery optimization disabled - optimal FCM performance"
        } else {
            "⚠ Battery optimization enabled - may affect FCM reliability"
        }
    }
    
    /**
     * Log battery optimization status
     */
    fun logBatteryOptimizationStatus(context: Context) {
        val status = getBatteryOptimizationStatus(context)
        Log.d(TAG, "Battery optimization status: $status")
        
        if (!isIgnoringBatteryOptimizations(context)) {
            Log.w(TAG, "App is not whitelisted from battery optimization - FCM may be unreliable")
        }
    }
    
    /**
     * Check if the device is known to have aggressive battery optimization
     */
    fun hasAggressiveBatteryOptimization(): Boolean {
        val manufacturer = Build.MANUFACTURER.lowercase()
        val aggressiveManufacturers = listOf(
            "xiaomi", "huawei", "honor", "oppo", "vivo", "oneplus", "samsung"
        )
        
        return aggressiveManufacturers.any { manufacturer.contains(it) }
    }
    
    /**
     * Get device-specific battery optimization guidance
     */
    fun getDeviceSpecificGuidance(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        
        return when {
            manufacturer.contains("xiaomi") -> 
                "On MIUI devices, also check 'Autostart' and 'Background app refresh' settings in Security app."
            
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> 
                "On EMUI devices, also enable 'Manual startup' and disable 'Intelligent hibernation' in Phone Manager."
            
            manufacturer.contains("oppo") -> 
                "On ColorOS devices, also enable 'Allow background activity' and 'Auto startup' in Settings > Battery."
            
            manufacturer.contains("vivo") -> 
                "On FunTouch OS devices, also enable 'High background app limit' and 'Auto startup' in iManager."
            
            manufacturer.contains("oneplus") -> 
                "On OxygenOS devices, also disable 'Battery optimization' and enable 'Auto startup' in Settings."
            
            manufacturer.contains("samsung") -> 
                "On Samsung devices, also add the app to 'Never sleeping apps' in Settings > Device care > Battery."
            
            else -> 
                "Check your device's app management settings for additional power saving options that may affect background apps."
        }
    }
}