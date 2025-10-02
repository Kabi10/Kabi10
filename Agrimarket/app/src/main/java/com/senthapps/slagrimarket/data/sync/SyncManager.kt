package com.senthapps.slagrimarket.data.sync

import android.content.Context
import androidx.work.*
import com.senthapps.slagrimarket.data.api.AuthApiService
import com.senthapps.slagrimarket.data.api.CreateListingRequest
import com.senthapps.slagrimarket.data.api.CreateTransactionRequest
import com.senthapps.slagrimarket.data.api.ListingApiService
import com.senthapps.slagrimarket.data.api.SyncApiService
import com.senthapps.slagrimarket.data.api.TransactionApiService
import com.senthapps.slagrimarket.data.api.UpdateListingRequest
import com.senthapps.slagrimarket.data.api.UpdateTransactionStatusRequest
import com.senthapps.slagrimarket.data.dao.ListingDao
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.dao.TransactionDao
import com.senthapps.slagrimarket.data.dao.UserDao
import com.senthapps.slagrimarket.data.model.LocalOp
import com.senthapps.slagrimarket.data.model.OpType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.squareup.moshi.Moshi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localOpDao: LocalOpDao,
    private val userDao: UserDao,
    private val listingDao: ListingDao,
    private val transactionDao: TransactionDao,
    private val authApiService: AuthApiService,
    private val listingApiService: ListingApiService,
    private val transactionApiService: TransactionApiService,
    private val syncApiService: SyncApiService,
    private val authRepository: AuthRepository,
    private val moshi: Moshi
) {
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val workManager = WorkManager.getInstance(context)

    private val _syncState = MutableStateFlow(SyncState())
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private var isAutoSyncEnabled = true
    private var syncIntervalMs = 30_000L // 30 seconds
    private var retryCount = 0
    private val maxRetries = 3
    private var baseBackoffDelayMs = 1000L // 1 second

    init {
        startAutoSync()
        enqueuePeriodicSync()
    }
    
    private fun startAutoSync() {
        syncScope.launch {
            while (isAutoSyncEnabled) {
                try {
                    if (authRepository.isUserLoggedIn()) {
                        performSync()
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Auto sync failed")
                }
                delay(syncIntervalMs)
            }
        }
    }
    
    suspend fun performSync(): Result<Unit> {
        return try {
            _syncState.value = _syncState.value.copy(isSyncing = true, error = null)
            
            // Step 1: Process pending local operations
            val pendingOps = localOpDao.getPendingOps()
            var successCount = 0
            var failureCount = 0
            
            for (op in pendingOps) {
                try {
                    when (op.type) {
                        OpType.CREATE_LISTING -> processCreateListing(op)
                        OpType.UPDATE_LISTING -> processUpdateListing(op)
                        OpType.DELETE_LISTING -> processDeleteListing(op)
                        OpType.CREATE_TRANSACTION -> processCreateTransaction(op)
                        OpType.UPDATE_TRANSACTION -> processUpdateTransaction(op)
                        OpType.UPDATE_USER -> processUpdateUser(op)
                    }
                    
                    // Mark operation as synced
                    localOpDao.markOpAsSynced(op.opId)
                    successCount++
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync operation ${op.opId}")
                    failureCount++
                    
                    // Mark operation as failed
                    localOpDao.markOpAsFailed(op.opId, e.message ?: "Unknown error")
                }
            }
            
            // Step 2: Fetch latest data from server
            syncDataFromServer()
            
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                lastSyncTime = System.currentTimeMillis(),
                pendingOperations = localOpDao.getPendingOpsCount(),
                successfulOperations = successCount,
                failedOperations = failureCount
            )
            
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            _syncState.value = _syncState.value.copy(
                isSyncing = false,
                error = e.message ?: "Sync failed"
            )
            Result.failure(e)
        }
    }
    
    private suspend fun processCreateListing(op: LocalOp) {
        val request = moshi.adapter(CreateListingRequest::class.java).fromJson(op.payload)
            ?: throw Exception("Invalid create listing payload")

        val response = listingApiService.createListing(request)
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Failed to create listing on server")
        }

        val serverListing = response.body()!!

        // Update local listing with server ID
        val localListing = listingDao.getListingByClientId(op.clientId ?: "")
        if (localListing != null) {
            listingDao.updateListingServerId(localListing.id, serverListing.id)
        }
    }
    
    private suspend fun processUpdateListing(op: LocalOp) {
        val request = moshi.adapter(UpdateListingRequest::class.java).fromJson(op.payload)
            ?: throw Exception("Invalid update listing payload")
        
        val listingId = op.entityId ?: throw Exception("Missing listing ID")
        listingApiService.updateListing(listingId, request)
    }
    
    private suspend fun processDeleteListing(op: LocalOp) {
        val listingId = op.entityId ?: throw Exception("Missing listing ID")
        listingApiService.deleteListing(listingId)
    }
    
    private suspend fun processCreateTransaction(op: LocalOp) {
        val request = moshi.adapter(CreateTransactionRequest::class.java).fromJson(op.payload)
            ?: throw Exception("Invalid create transaction payload")

        val response = transactionApiService.createTransaction(request)
        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Failed to create transaction on server")
        }

        val serverTransaction = response.body()!!

        // Update local transaction with server ID
        val localTransaction = transactionDao.getTransactionByClientId(op.clientId ?: "")
        if (localTransaction != null) {
            transactionDao.updateTransactionServerId(localTransaction.id, serverTransaction.id)
        }
    }
    
    private suspend fun processUpdateTransaction(op: LocalOp) {
        val request = moshi.adapter(UpdateTransactionStatusRequest::class.java).fromJson(op.payload)
            ?: throw Exception("Invalid update transaction payload")

        val transactionId = op.entityId ?: throw Exception("Missing transaction ID")
        transactionApiService.updateTransactionStatus(transactionId, request)
    }

    private suspend fun processUpdateUser(op: LocalOp) {
        val request = moshi.adapter(com.senthapps.slagrimarket.data.model.User::class.java).fromJson(op.payload)
            ?: throw Exception("Invalid update user payload")

        // For now, just update locally since user updates are typically profile changes
        userDao.updateUser(request)
    }
    
    private suspend fun syncDataFromServer() {
        try {
            // For now, skip server data sync as it's not critical for local operations
            // TODO: Implement proper sync data fetching when needed
            Timber.d("Server data sync skipped")
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync data from server")
            // Don't throw here as this is not critical for local operations
        }
    }
    
    fun enableAutoSync(enabled: Boolean) {
        isAutoSyncEnabled = enabled
        if (enabled) {
            startAutoSync()
        }
    }
    
    fun setSyncInterval(intervalMs: Long) {
        syncIntervalMs = intervalMs
    }
    
    suspend fun forceSyncNow(): Result<Unit> {
        return performSync()
    }
    
    suspend fun retryFailedOperations(): Result<Unit> {
        return try {
            val failedOps = localOpDao.getFailedOps()
            
            for (op in failedOps) {
                // Reset operation status to pending
                localOpDao.resetOpStatus(op.opId)
            }
            
            // Trigger sync
            performSync()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearSyncedOperations() {
        localOpDao.deleteSyncedOps()
    }
    
    fun getSyncStatistics(): SyncStatistics {
        val state = _syncState.value
        return SyncStatistics(
            pendingOperations = state.pendingOperations,
            successfulOperations = state.successfulOperations,
            failedOperations = state.failedOperations,
            lastSyncTime = state.lastSyncTime,
            isSyncing = state.isSyncing
        )
    }

    // ============================================================================
    // WORKMANAGER INTEGRATION
    // ============================================================================

    /**
     * Enqueue periodic sync work using WorkManager
     * Runs every 15 minutes when network is available and battery is not low
     */
    fun enqueuePeriodicSync() {
        val periodicWork = SyncWorker.createPeriodicSyncWork()

        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME_PERIODIC_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicWork
        )

        Timber.d("Periodic sync work enqueued")
    }

    /**
     * Enqueue one-time sync work
     * Useful for immediate sync requests
     */
    fun enqueueOneTimeSync() {
        val oneTimeWork = SyncWorker.createOneTimeSyncWork()

        workManager.enqueueUniqueWork(
            "one_time_sync",
            ExistingWorkPolicy.REPLACE,
            oneTimeWork
        )

        Timber.d("One-time sync work enqueued")
    }

    /**
     * Cancel all sync work
     */
    fun cancelAllSyncWork() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME_PERIODIC_SYNC)
        workManager.cancelAllWorkByTag(SyncWorker.WORK_TAG_SYNC)
        Timber.d("All sync work cancelled")
    }

    /**
     * Get sync work info
     */
    fun getSyncWorkInfo() = workManager.getWorkInfosForUniqueWorkLiveData(SyncWorker.WORK_NAME_PERIODIC_SYNC)

    // ============================================================================
    // EXPONENTIAL BACKOFF
    // ============================================================================

    /**
     * Calculate exponential backoff delay
     */
    private fun calculateBackoffDelay(): Long {
        val delay = baseBackoffDelayMs * (1 shl retryCount.coerceAtMost(5)) // Max 32x base delay
        return delay.coerceAtMost(300_000L) // Max 5 minutes
    }

    /**
     * Perform sync with exponential backoff on failure
     */
    suspend fun performSyncWithBackoff(): Result<Unit> {
        val result = performSync()

        result.fold(
            onSuccess = {
                // Reset retry count on success
                retryCount = 0
            },
            onFailure = {
                // Increment retry count and schedule retry with backoff
                if (retryCount < maxRetries) {
                    retryCount++
                    val backoffDelay = calculateBackoffDelay()
                    Timber.d("Sync failed, retrying in ${backoffDelay}ms (attempt $retryCount/$maxRetries)")
                    delay(backoffDelay)
                    return performSyncWithBackoff() // Recursive retry
                } else {
                    Timber.e("Sync failed after $maxRetries retries")
                    retryCount = 0 // Reset for next attempt
                }
            }
        )

        return result
    }
}

data class SyncState(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0L,
    val pendingOperations: Int = 0,
    val successfulOperations: Int = 0,
    val failedOperations: Int = 0,
    val error: String? = null
)

data class SyncStatistics(
    val pendingOperations: Int,
    val successfulOperations: Int,
    val failedOperations: Int,
    val lastSyncTime: Long,
    val isSyncing: Boolean
)
