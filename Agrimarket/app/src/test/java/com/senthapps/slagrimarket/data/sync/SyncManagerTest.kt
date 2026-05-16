package com.senthapps.slagrimarket.data.sync

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import androidx.work.testing.WorkManagerTestInitHelper
import com.senthapps.slagrimarket.data.api.AuthApiService
import com.senthapps.slagrimarket.data.api.ListingApiService
import com.senthapps.slagrimarket.data.api.SyncApiService
import com.senthapps.slagrimarket.data.api.TransactionApiService
import com.senthapps.slagrimarket.data.dao.ListingDao
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.dao.TransactionDao
import com.senthapps.slagrimarket.data.dao.UserDao
import com.senthapps.slagrimarket.data.model.BatchSyncResponse
import com.senthapps.slagrimarket.data.model.ConflictInfo
import com.senthapps.slagrimarket.data.model.LocalOp
import com.senthapps.slagrimarket.data.model.OperationError
import com.senthapps.slagrimarket.data.model.OpType
import com.senthapps.slagrimarket.data.model.PaginatedListings
import com.senthapps.slagrimarket.data.model.PaginatedTransactions
import com.senthapps.slagrimarket.data.model.ServerData
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.squareup.moshi.Moshi
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.Response
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
@OptIn(ExperimentalCoroutinesApi::class)
class SyncManagerTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var localOpDao: LocalOpDao
    private lateinit var userDao: UserDao
    private lateinit var listingDao: ListingDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var authApiService: AuthApiService
    private lateinit var listingApiService: ListingApiService
    private lateinit var transactionApiService: TransactionApiService
    private lateinit var syncApiService: SyncApiService
    private lateinit var authRepository: AuthRepository
    private lateinit var context: Context
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var sharedPrefsEditor: SharedPreferences.Editor

    private val moshi: Moshi = Moshi.Builder().build()

    private lateinit var syncManager: SyncManager

    private fun makeOp(id: String) = LocalOp(
        opId = id,
        type = OpType.CREATE_LISTING,
        payload = "{}",
        clientTs = Instant.now().toString(),
        attempts = 0,
        synced = false
    )

    private fun makeSuccessResponse(
        appliedOps: List<String> = emptyList(),
        conflicts: List<ConflictInfo> = emptyList(),
        errors: List<OperationError> = emptyList()
    ) = Response.success(
        BatchSyncResponse(
            success = true,
            appliedOps = appliedOps,
            conflicts = conflicts,
            errors = errors,
            serverData = ServerData(
                users = emptyList(),
                listings = PaginatedListings(data = emptyList(), total = 0, page = 1, limit = 50),
                transactions = PaginatedTransactions(data = emptyList(), total = 0, page = 1, limit = 50)
            ),
            serverTimestamp = Instant.now().toString()
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        localOpDao = mockk(relaxed = true)
        userDao = mockk(relaxed = true)
        listingDao = mockk(relaxed = true)
        transactionDao = mockk(relaxed = true)
        authApiService = mockk(relaxed = true)
        listingApiService = mockk(relaxed = true)
        transactionApiService = mockk(relaxed = true)
        syncApiService = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)

        sharedPrefsEditor = mockk(relaxed = true)
        sharedPrefs = mockk(relaxed = true)
        every { sharedPrefs.getString(any(), null) } returns null
        every { sharedPrefs.edit() } returns sharedPrefsEditor
        every { sharedPrefsEditor.putString(any(), any()) } returns sharedPrefsEditor

        // Use a real Application context from Robolectric so WorkManager.getInstance() works
        val appContext = ApplicationProvider.getApplicationContext<Context>()
        WorkManagerTestInitHelper.initializeTestWorkManager(appContext)

        // Wrap the app context to intercept SharedPreferences reads/writes
        context = mockk(relaxed = true)
        every { context.getApplicationContext() } returns appContext
        every { context.getSharedPreferences(any(), any()) } returns sharedPrefs

        // Prevent auto-sync coroutine from triggering actual syncs
        coEvery { authRepository.isUserLoggedIn() } returns false

        syncManager = SyncManager(
            context = context,
            localOpDao = localOpDao,
            userDao = userDao,
            listingDao = listingDao,
            transactionDao = transactionDao,
            authApiService = authApiService,
            listingApiService = listingApiService,
            transactionApiService = transactionApiService,
            syncApiService = syncApiService,
            authRepository = authRepository,
            moshi = moshi
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---- performSync: empty ops path ----

    @Test
    fun `performSync with no pending ops calls getSyncData and succeeds`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns emptyList()
        coEvery { syncApiService.getSyncData(any(), any(), any(), any(), any()) } returns
            makeSuccessResponse()

        val result = syncManager.performSync()

        assertTrue(result.isSuccess)
        coVerify { syncApiService.getSyncData(any(), any(), any(), any(), any()) }
    }

    @Test
    fun `performSync with no pending ops sets isSyncing false on completion`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns emptyList()
        coEvery { syncApiService.getSyncData(any(), any(), any(), any(), any()) } returns
            makeSuccessResponse()

        syncManager.performSync()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(syncManager.syncState.value.isSyncing)
    }

    // ---- performSync: batch processing ----

    @Test
    fun `performSync with pending ops calls syncOperations`() = runTest {
        val ops = listOf(makeOp("op-1"), makeOp("op-2"))
        coEvery { localOpDao.getPendingOps() } returns ops
        coEvery { syncApiService.syncOperations(any()) } returns makeSuccessResponse(
            appliedOps = listOf("op-1", "op-2")
        )

        val result = syncManager.performSync()

        assertTrue(result.isSuccess)
        coVerify { syncApiService.syncOperations(any()) }
    }

    @Test
    fun `performSync marks applied ops as synced`() = runTest {
        val ops = listOf(makeOp("op-1"))
        coEvery { localOpDao.getPendingOps() } returns ops
        coEvery { syncApiService.syncOperations(any()) } returns makeSuccessResponse(
            appliedOps = listOf("op-1")
        )

        syncManager.performSync()

        coVerify { localOpDao.markOpsAsSynced(listOf("op-1")) }
    }

    @Test
    fun `performSync marks conflict ops as failed with conflict reason`() = runTest {
        val ops = listOf(makeOp("op-conflict"))
        coEvery { localOpDao.getPendingOps() } returns ops
        coEvery { syncApiService.syncOperations(any()) } returns makeSuccessResponse(
            conflicts = listOf(ConflictInfo(opId = "op-conflict", reason = "stale version"))
        )

        syncManager.performSync()

        coVerify { localOpDao.markOpAsFailed("op-conflict", "Conflict: stale version") }
    }

    @Test
    fun `performSync marks error ops as failed`() = runTest {
        val ops = listOf(makeOp("op-err"))
        coEvery { localOpDao.getPendingOps() } returns ops
        coEvery { syncApiService.syncOperations(any()) } returns makeSuccessResponse(
            errors = listOf(OperationError(opId = "op-err", error = "invalid payload"))
        )

        syncManager.performSync()

        coVerify { localOpDao.markOpAsFailed("op-err", "invalid payload") }
    }

    @Test
    fun `performSync updates conflictCount in syncState`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns listOf(makeOp("op-c"))
        coEvery { syncApiService.syncOperations(any()) } returns makeSuccessResponse(
            conflicts = listOf(ConflictInfo(opId = "op-c", reason = "outdated"))
        )
        // After cleanup, getConflictOpsCount reflects persisted conflicts
        coEvery { localOpDao.getConflictOpsCount() } returns 1

        syncManager.performSync()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, syncManager.syncState.value.conflictCount)
    }

    // ---- stale ops cleanup ----

    @Test
    fun `performSync calls deleteFailedNonConflictOps and deleteOldSyncedOps after success`() = runTest {
        // Cleanup only runs when ops are actually processed (empty-ops path returns early)
        coEvery { localOpDao.getPendingOps() } returns listOf(makeOp("op-cleanup"))
        coEvery { syncApiService.syncOperations(any()) } returns makeSuccessResponse(
            appliedOps = listOf("op-cleanup")
        )

        syncManager.performSync()

        coVerify { localOpDao.deleteFailedNonConflictOps(maxAttempts = 3) }
        coVerify { localOpDao.deleteOldSyncedOps(any()) }
    }

    @Test
    fun `performSync does not call cleanup when transport failure occurs`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns listOf(makeOp("op-1"))
        coEvery { syncApiService.syncOperations(any()) } returns
            Response.error(500, okhttp3.ResponseBody.create(null, "server error"))

        syncManager.performSync()

        coVerify(exactly = 0) { localOpDao.deleteOldSyncedOps(any()) }
    }

    // ---- transport failure ----

    @Test
    fun `performSync returns failure on transport error`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns listOf(makeOp("op-1"))
        coEvery { syncApiService.syncOperations(any()) } returns
            Response.error(500, okhttp3.ResponseBody.create(null, ""))

        val result = syncManager.performSync()

        assertTrue(result.isFailure)
    }

    @Test
    fun `performSync sets error message in syncState on transport failure`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns listOf(makeOp("op-1"))
        coEvery { syncApiService.syncOperations(any()) } returns
            Response.error(503, okhttp3.ResponseBody.create(null, ""))

        syncManager.performSync()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(syncManager.syncState.value.error)
    }

    // ---- retry backoff ----

    @Test
    fun `performSyncWithBackoff resets retryCount on success`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns emptyList()
        coEvery { syncApiService.getSyncData(any(), any(), any(), any(), any()) } returns
            makeSuccessResponse()

        val result = syncManager.performSyncWithBackoff()

        assertTrue(result.isSuccess)
    }

    @Test
    fun `performSyncWithBackoff returns failure after maxRetries exhausted`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns listOf(makeOp("op-retry"))
        coEvery { syncApiService.syncOperations(any()) } returns
            Response.error(500, okhttp3.ResponseBody.create(null, ""))

        val result = syncManager.performSyncWithBackoff()

        assertTrue(result.isFailure)
    }

    @Test
    fun `performSyncWithBackoff clears error state after eventual success`() = runTest {
        var callCount = 0
        coEvery { localOpDao.getPendingOps() } answers {
            callCount++
            if (callCount <= 2) listOf(makeOp("op-$callCount")) else emptyList()
        }
        coEvery { syncApiService.syncOperations(any()) } returns
            Response.error(500, okhttp3.ResponseBody.create(null, ""))
        coEvery { syncApiService.getSyncData(any(), any(), any(), any(), any()) } returns
            makeSuccessResponse()

        // Third call onwards succeeds (empty pending ops → getSyncData)
        syncManager.performSyncWithBackoff()
        testDispatcher.scheduler.advanceUntilIdle()
        // Last successful pass clears the error
        assertNull(syncManager.syncState.value.error)
    }

    // ---- retryFailedOperations ----

    @Test
    fun `retryFailedOperations resets op status before syncing`() = runTest {
        val failedOp = makeOp("op-failed").copy(attempts = 3)
        coEvery { localOpDao.getFailedOps() } returns listOf(failedOp)
        coEvery { localOpDao.getPendingOps() } returns emptyList()
        coEvery { syncApiService.getSyncData(any(), any(), any(), any(), any()) } returns
            makeSuccessResponse()

        val result = syncManager.retryFailedOperations()

        assertTrue(result.isSuccess)
        coVerify { localOpDao.resetOpStatus("op-failed") }
    }

    @Test
    fun `retryFailedOperations skips conflict ops so user must dismiss them`() = runTest {
        val conflictOp = makeOp("op-conflict").copy(
            attempts = 3,
            errorMessage = "Conflict: stale version"
        )
        val failedOp = makeOp("op-failed").copy(attempts = 3, errorMessage = "network error")
        coEvery { localOpDao.getFailedOps() } returns listOf(conflictOp, failedOp)
        coEvery { localOpDao.getPendingOps() } returns emptyList()
        coEvery { syncApiService.getSyncData(any(), any(), any(), any(), any()) } returns
            makeSuccessResponse()

        syncManager.retryFailedOperations()

        coVerify(exactly = 0) { localOpDao.resetOpStatus("op-conflict") }
        coVerify(exactly = 1) { localOpDao.resetOpStatus("op-failed") }
    }

    // ---- control methods ----

    @Test
    fun `clearSyncedOperations delegates to dao`() = runTest {
        syncManager.clearSyncedOperations()
        coVerify { localOpDao.deleteSyncedOps() }
    }

    @Test
    fun `getSyncStatistics returns values from current syncState`() = runTest {
        coEvery { localOpDao.getPendingOps() } returns emptyList()
        coEvery { syncApiService.getSyncData(any(), any(), any(), any(), any()) } returns
            makeSuccessResponse()
        coEvery { localOpDao.getPendingOpsCount() } returns 0

        syncManager.performSync()
        testDispatcher.scheduler.advanceUntilIdle()

        val stats = syncManager.getSyncStatistics()
        assertFalse(stats.isSyncing)
    }

    @Test
    fun `enableAutoSync false does not affect existing suspend call`() = runTest {
        // Disabling auto sync should not throw or prevent manual sync
        syncManager.enableAutoSync(false)

        coEvery { localOpDao.getPendingOps() } returns emptyList()
        coEvery { syncApiService.getSyncData(any(), any(), any(), any(), any()) } returns
            makeSuccessResponse()

        val result = syncManager.forceSyncNow()
        assertTrue(result.isSuccess)
    }

    // ---- batch chunking ----

    @Test
    fun `performSync processes 51 ops in two separate batches`() = runTest {
        val ops = (1..51).map { makeOp("op-$it") }
        coEvery { localOpDao.getPendingOps() } returns ops
        coEvery { syncApiService.syncOperations(any()) } returns makeSuccessResponse()

        syncManager.performSync()

        // 51 ops → batch of 50 + batch of 1 → 2 calls
        coVerify(exactly = 2) { syncApiService.syncOperations(any()) }
    }

    // ---- conflict count persistence across restarts ----

    @Test
    fun `loadInitialConflictCount sets conflictCount from persisted conflict ops`() = runTest {
        coEvery { localOpDao.getConflictOpsCount() } returns 3

        syncManager.loadInitialConflictCount()

        assertEquals(3, syncManager.syncState.value.conflictCount)
    }

    @Test
    fun `loadInitialConflictCount leaves conflictCount at zero when DAO returns zero`() = runTest {
        coEvery { localOpDao.getConflictOpsCount() } returns 0

        syncManager.loadInitialConflictCount()

        assertEquals(0, syncManager.syncState.value.conflictCount)
    }
}
