package com.senthapps.snapassist.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.senthapps.snapassist.service.CameraService

/**
 * Boot receiver to automatically re-arm camera service after device reboot
 * Only re-arms if the service was previously armed before shutdown
 */
class BootReceiver : BroadcastReceiver() {
    
    companion object {
        private const val TAG = "BootReceiver"
        private const val PREFS_NAME = "snapassist_prefs"
        private const val KEY_WAS_ARMED = "was_armed_before_shutdown"
        
        /**
         * Save the armed state for restoration after reboot
         */
        fun saveArmedState(context: Context, wasArmed: Boolean) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_WAS_ARMED, wasArmed).apply()
            Log.d(TAG, "Saved armed state: $wasArmed")
        }
        
        /**
         * Get the previously saved armed state
         */
        fun getArmedState(context: Context): Boolean {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return prefs.getBoolean(KEY_WAS_ARMED, false)
        }
        
        /**
         * Clear the saved armed state
         */
        fun clearArmedState(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().remove(KEY_WAS_ARMED).apply()
            Log.d(TAG, "Cleared saved armed state")
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }
        
        Log.d(TAG, "Boot completed - checking if camera service should be re-armed")
        
        try {
            val wasArmed = getArmedState(context)
            
            if (wasArmed) {
                Log.d(TAG, "Service was armed before shutdown, attempting to re-arm")
                
                // Start the camera service
                val success = CameraService.safeStart(context)
                
                if (success) {
                    Log.d(TAG, "Camera service successfully re-armed after boot")
                    // Clear the saved state since we've successfully restored
                    clearArmedState(context)
                } else {
                    Log.w(TAG, "Failed to re-arm camera service after boot")
                    // Keep the saved state for potential manual re-arm
                }
            } else {
                Log.d(TAG, "Service was not armed before shutdown, no action needed")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error during boot receiver processing", e)
        }
    }
}