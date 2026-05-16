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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant
import java.time.temporal.ChronoUnit
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
    @Volatile private var retryCount = 0
    private val maxRetries = 3
    private var baseBackoffDelayMs = 1000L // 1 second
    private var autoSyncJob: Job? = null

    init {
        startAutoSync()
        enqueuePeriodicSync()
        syncScope.launch { loadInitialConflictCount() }
    }

    internal suspend fun loadInitialConflictCount() {
        try {
            val count = localOpDao.getConflictOpsCount()
            if (count > 0) {
                _syncState.value = _syncState.value.copy(conflictCount = count)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to load initial conflict count")
        }
    }
    
    private fun startAutoSync() {
        autoSyncJob?.cancel()
        autoSyncJob = syncScope.launch {
            while (isAutoSyncEnabled) {
                try {
                    if (authRepository.isUserLoggedIn()) {
                        performSyncWithBackoff()
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

            // Step 1: Get all pending operations
            val allPendingOps = localOpDao.getPendingOps()

            if (allPendingOps.isEmpty()) {
                // No pending ops, just fetch server data
                fetchServerData()
                updateSyncState(successCount = 0, failureCount = 0)
                return Result.success(Unit)
            }

            // Step 2: Process in batches of 50 (MAX_SYNC_OPERATIONS_PER_REQUEST)
            val batches = allPendingOps.chunked(50)
            var totalSuccess = 0
            var totalFailures = 0
            val allConflicts = mutableListOf<com.senthapps.slagrimarket.data.model.ConflictInfo>()

            for (batch in batches) {
                val result = processBatch(batch)
                totalSuccess += result.successCount
                totalFailures += result.failureCount
                allConflicts.addAll(result.conflicts)
            }

            // Step 3: Log conflicts (future: expose to UI)
            if (allConflicts.isNotEmpty()) {
                Timber.w("Sync completed with ${allConflicts.size} conflicts")
            }

            updateSyncState(successCount = totalSuccess, failureCount = totalFailures, conflictCount = allConflicts.size)
            cleanupStaleOps()
            // Re-read actual conflict count from DB after cleanup so the UI reflects
            // all persisted (unresolved) conflicts, not just the current batch.
            val persistedConflictCount = localOpDao.getConflictOpsCount()
            _syncState.value = _syncState.value.copy(conflictCount = persistedConflictCount)
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
    
    private suspend fun processBatch(operations: List<LocalOp>): BatchResult {
        val lastSyncTime = getLastSyncTimestamp()

        val syncRequest = com.senthapps.slagrimarket.data.model.SyncRequest(
            lastSyncAt = lastSyncTime,
            operations = operations
        )

        val response = syncApiService.syncOperations(syncRequest)

        if (!response.isSuccessful || response.body() == null) {
            // Transport failure (network error, 5xx) — leave ops pending so the
            // backoff retry in performSyncWithBackoff picks them up next attempt.
            // Only server-acknowledged rejections (in batchResponse.errors) should
            // permanently mark ops failed.
            Timber.w("Batch sync transport failure: HTTP ${response.code()}, ${operations.size} ops left pending")
            throw Exception("Batch sync failed: HTTP ${response.code()}")
        }

        val batchResponse = response.body()!!

        // Process successful operations
        if (batchResponse.appliedOps.isNotEmpty()) {
            localOpDao.markOpsAsSynced(batchResponse.appliedOps)
        }

        // Process failed operations
        batchResponse.errors.forEach { opError ->
            localOpDao.markOpAsFailed(opError.opId, opError.error)
        }

        // Process conflicts
        batchResponse.conflicts.forEach { conflict ->
            localOpDao.markOpAsFailed(conflict.opId, "Conflict: ${conflict.reason}")
        }

        // Update local database with server data
        updateLocalDatabaseFromServerData(batchResponse.serverData)

        // Store last sync timestamp
        saveLastSyncTimestamp(batchResponse.serverTimestamp)

        return BatchResult(
            successCount = batchResponse.appliedOps.size,
            failureCount = batchResponse.errors.size + batchResponse.conflicts.size,
            conflicts = batchResponse.conflicts
        )
    }

    private data class BatchResult(
        val successCount: Int,
        val failureCount: Int,
        val conflicts: List<com.senthapps.slagrimarket.data.model.ConflictInfo>
    )

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
    
    private suspend fun updateLocalDatabaseFromServerData(serverData: com.senthapps.slagrimarket.data.model.ServerData) {
        try {
            // Update users (current user only)
            if (serverData.users.isNotEmpty()) {
                userDao.insertUsers(serverData.users)
                Timber.d("Synced ${serverData.users.size} users from server")
            }

            // Update listings (first page from batch response)
            if (serverData.listings.data.isNotEmpty()) {
                listingDao.insertListings(serverData.listings.data)
                Timber.d("Synced ${serverData.listings.data.size} of ${serverData.listings.total} listings")
            }

            // Update transactions (first page from batch response)
            if (serverData.transactions.data.isNotEmpty()) {
                transactionDao.insertTransactions(serverData.transactions.data)
                Timber.d("Synced ${serverData.transactions.data.size} of ${serverData.transactions.total} transactions")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update local database from server data")
            // Don't throw - this shouldn't fail the entire sync
        }
    }

    private suspend fun fetchServerData(): Result<Unit> {
        try {
            val lastSyncTime = getLastSyncTimestamp()

            val response = syncApiService.getSyncData(
                lastSyncAt = lastSyncTime,
                listingPage = 1,
                listingLimit = 50,
                transactionPage = 1,
                transactionLimit = 50
            )

            if (response.isSuccessful && response.body() != null) {
                val syncData = response.body()!!
                updateLocalDatabaseFromServerData(syncData.serverData)
                saveLastSyncTimestamp(syncData.serverTimestamp)
                return Result.success(Unit)
            }

            return Result.failure(Exception("Failed to fetch server data: ${response.code()}"))
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch server data")
            return Result.failure(e)
        }
    }

    private suspend fun syncDataFromServer() {
        try {
            // Fetch latest listings from server
            val listingsResponse = listingApiService.getListings()
            if (listingsResponse.isSuccessful && listingsResponse.body() != null) {
                val serverListings = listingsResponse.body()!!.listings
                // Insert or update listings from server
                // Note: Room's REPLACE strategy handles conflicts appropriately
                listingDao.insertListings(serverListings)
                Timber.d("Synced ${serverListings.size} listings from server")
            }

            // Fetch latest transactions from server
            val transactionsResponse = transactionApiService.getTransactions()
            if (transactionsResponse.isSuccessful && transactionsResponse.body() != null) {
                val serverTransactions = transactionsResponse.body()!!.transactions
                transactionDao.insertTransactions(serverTransactions)
                Timber.d("Synced ${serverTransactions.size} transactions from server")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync data from server")
            // Don't throw here as this is not critical for local operations
        }
    }
    
    private fun getLastSyncTimestamp(): String? {
        return context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            .getString("last_sync_timestamp", null)
    }

    private fun saveLastSyncTimestamp(timestamp: String) {
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString("last_sync_timestamp", timestamp)
            .apply()
    }

    private suspend fun updateSyncState(successCount: Int, failureCount: Int, conflictCount: Int = 0) {
        _syncState.value = _syncState.value.copy(
            isSyncing = false,
            lastSyncTime = System.currentTimeMillis(),
            pendingOperations = localOpDao.getPendingOpsCount(),
            successfulOperations = successCount,
            failedOperations = failureCount,
            conflictCount = conflictCount
        )
    }

    fun enableAutoSync(enabled: Boolean) {
        isAutoSyncEnabled = enabled
        if (enabled) {
            startAutoSync()
        } else {
            autoSyncJob?.cancel()
            autoSyncJob = null
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
                .filter { it.errorMessage?.startsWith("Conflict:") != true }

            for (op in failedOps) {
                localOpDao.resetOpStatus(op.opId)
            }

            performSync()
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun clearSyncedOperations() {
        localOpDao.deleteSyncedOps()
    }

    suspend fun dismissConflicts() {
        try {
            localOpDao.deleteConflictOps()
            _syncState.value = _syncState.value.copy(conflictCount = 0)
        } catch (e: Exception) {
            Timber.e(e, "Failed to dismiss conflicts")
        }
    }
    
    fun getSyncStatistics(): SyncStatistics {
        val state = _syncState.value
        return SyncStatistics(
            pendingOperations = state.pendingOperations,
            successfulOperations = state.successfulOperations,
            failedOperations = state.failedOperations,
            conflictCount = state.conflictCount,
            lastSyncTime = state.lastSyncTime,
            isSyncing = state.isSyncing
        )
    }

    private suspend fun cleanupStaleOps() {
        try {
            // Delete permanently-failed ops but preserve conflict ops so the user
            // can see and dismiss them via the ConflictBanner in SyncSettingsScreen.
            localOpDao.deleteFailedNonConflictOps(maxAttempts = 3)
            // Remove synced ops older than 7 days
            val cutoffTs = Instant.now().minus(7, ChronoUnit.DAYS).toString()
            localOpDao.deleteOldSyncedOps(cutoffTs)
        } catch (e: Exception) {
            Timber.e(e, "Failed to cleanup stale ops")
        }
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
    val conflictCount: Int = 0,
    val error: String? = null
)

data class SyncStatistics(
    val pendingOperations: Int,
    val successfulOperations: Int,
    val failedOperations: Int,
    val conflictCount: Int,
    val lastSyncTime: Long,
    val isSyncing: Boolean
)
