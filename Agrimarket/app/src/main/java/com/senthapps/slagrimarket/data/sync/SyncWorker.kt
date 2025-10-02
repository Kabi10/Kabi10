package com.senthapps.slagrimarket.data.sync

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.senthapps.slagrimarket.data.repository.AuthRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Background worker for data synchronization
 * Extends CoroutineWorker for background execution with coroutine support
 */
@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val syncManager: SyncManager,
    private val authRepository: AuthRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("SyncWorker: Starting background sync")
            
            // Check if user is logged in
            if (!authRepository.isUserLoggedIn()) {
                Timber.d("SyncWorker: User not logged in, skipping sync")
                return@withContext Result.success()
            }
            
            // Set foreground notification for long-running sync
            setForeground(createForegroundInfo())
            
            // Perform sync operation
            val syncResult = syncManager.performSync()
            
            syncResult.fold(
                onSuccess = {
                    Timber.d("SyncWorker: Sync completed successfully")
                    
                    // Output data with sync statistics
                    val stats = syncManager.getSyncStatistics()
                    val outputData = workDataOf(
                        KEY_PENDING_OPS to stats.pendingOperations,
                        KEY_SUCCESSFUL_OPS to stats.successfulOperations,
                        KEY_FAILED_OPS to stats.failedOperations,
                        KEY_LAST_SYNC_TIME to stats.lastSyncTime
                    )
                    
                    Result.success(outputData)
                },
                onFailure = { error ->
                    Timber.e(error, "SyncWorker: Sync failed")
                    
                    // Determine if we should retry
                    val shouldRetry = shouldRetryOnError(error)
                    
                    if (shouldRetry && runAttemptCount < MAX_RETRY_ATTEMPTS) {
                        Timber.d("SyncWorker: Retrying sync (attempt ${runAttemptCount + 1}/$MAX_RETRY_ATTEMPTS)")
                        Result.retry()
                    } else {
                        Timber.e("SyncWorker: Max retries reached or non-retryable error")
                        val outputData = workDataOf(
                            KEY_ERROR_MESSAGE to (error.message ?: "Sync failed")
                        )
                        Result.failure(outputData)
                    }
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker: Unexpected error during sync")
            
            if (runAttemptCount < MAX_RETRY_ATTEMPTS) {
                Result.retry()
            } else {
                val outputData = workDataOf(
                    KEY_ERROR_MESSAGE to (e.message ?: "Unexpected error")
                )
                Result.failure(outputData)
            }
        }
    }
    
    /**
     * Create foreground notification for sync progress
     */
    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(
            applicationContext,
            com.senthapps.slagrimarket.JaffnaMarketplaceApplication.CHANNEL_ID_SYNC
        )
            .setContentTitle("Syncing Data")
            .setContentText("Synchronizing your data with the server...")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }
    
    /**
     * Determine if error is retryable
     */
    private fun shouldRetryOnError(error: Throwable): Boolean {
        return when {
            error is java.net.UnknownHostException -> true
            error is java.net.SocketTimeoutException -> true
            error is java.io.IOException -> true
            error.message?.contains("timeout", ignoreCase = true) == true -> true
            error.message?.contains("network", ignoreCase = true) == true -> true
            else -> false
        }
    }
    
    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val MAX_RETRY_ATTEMPTS = 3
        
        // Output data keys
        const val KEY_PENDING_OPS = "pending_ops"
        const val KEY_SUCCESSFUL_OPS = "successful_ops"
        const val KEY_FAILED_OPS = "failed_ops"
        const val KEY_LAST_SYNC_TIME = "last_sync_time"
        const val KEY_ERROR_MESSAGE = "error_message"
        
        // Work request tags
        const val WORK_TAG_SYNC = "sync_work"
        const val WORK_TAG_PERIODIC_SYNC = "periodic_sync_work"
        const val WORK_NAME_PERIODIC_SYNC = "periodic_sync"
        
        /**
         * Create one-time sync work request
         */
        fun createOneTimeSyncWork(): OneTimeWorkRequest {
            return OneTimeWorkRequestBuilder<SyncWorker>()
                .setConstraints(createSyncConstraints())
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_TAG_SYNC)
                .build()
        }
        
        /**
         * Create periodic sync work request
         * Runs every 15 minutes when conditions are met
         */
        fun createPeriodicSyncWork(): PeriodicWorkRequest {
            return PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES // Flex interval
            )
                .setConstraints(createSyncConstraints())
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag(WORK_TAG_PERIODIC_SYNC)
                .build()
        }
        
        /**
         * Create constraints for sync work
         * - Requires network connectivity
         * - Battery not low
         * - Device idle (optional, for periodic sync)
         */
        private fun createSyncConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        }
        
        /**
         * Create constraints for WiFi-only sync
         */
        fun createWiFiOnlyConstraints(): Constraints {
            return Constraints.Builder()
                .setRequiredNetworkType(NetworkType.UNMETERED)
                .setRequiresBatteryNotLow(true)
                .build()
        }
    }
}

