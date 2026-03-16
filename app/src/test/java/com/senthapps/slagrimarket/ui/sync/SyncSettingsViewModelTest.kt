package com.senthapps.slagrimarket.ui.sync

import com.senthapps.slagrimarket.data.sync.SyncManager
import com.senthapps.slagrimarket.data.sync.SyncState
import com.senthapps.slagrimarket.data.sync.SyncStatistics
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncSettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var syncManager: SyncManager
    private val syncStateFlow = MutableStateFlow(SyncState())
    private lateinit var viewModel: SyncSettingsViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        syncManager = mockk(relaxed = true)
        every { syncManager.syncState } returns syncStateFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects syncManager syncState`() = runTest {
        syncStateFlow.value = SyncState(
            isSyncing = false,
            pendingOperations = 3,
            successfulOperations = 10,
            failedOperations = 1
        )

        viewModel = SyncSettingsViewModel(syncManager)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSyncing)
        assertEquals(3, state.pendingOperations)
        assertEquals(10, state.successfulOperations)
        assertEquals(1, state.failedOperations)
    }

    @Test
    fun `syncState updates propagate to uiState`() = runTest {
        viewModel = SyncSettingsViewModel(syncManager)
        testDispatcher.scheduler.advanceUntilIdle()

        syncStateFlow.value = SyncState(isSyncing = true, pendingOperations = 5)
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isSyncing)
        assertEquals(5, state.pendingOperations)
    }

    @Test
    fun `toggleAutoSync should update state and call syncManager`() = runTest {
        viewModel = SyncSettingsViewModel(syncManager)

        viewModel.toggleAutoSync(false)
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.autoSyncEnabled)
        verify { syncManager.enableAutoSync(false) }
    }

    @Test
    fun `toggleAutoSync enable should propagate to syncManager`() = runTest {
        viewModel = SyncSettingsViewModel(syncManager)
        viewModel.toggleAutoSync(false)

        viewModel.toggleAutoSync(true)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.autoSyncEnabled)
        verify { syncManager.enableAutoSync(true) }
    }

    @Test
    fun `updateSyncInterval should update state and call syncManager`() = runTest {
        viewModel = SyncSettingsViewModel(syncManager)

        viewModel.updateSyncInterval(60_000L)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(60_000L, viewModel.uiState.value.syncInterval)
        verify { syncManager.setSyncInterval(60_000L) }
    }

    @Test
    fun `forceSyncNow should call syncManager forceSyncNow`() = runTest {
        coEvery { syncManager.forceSyncNow() } returns Result.success(Unit)
        viewModel = SyncSettingsViewModel(syncManager)

        viewModel.forceSyncNow()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { syncManager.forceSyncNow() }
    }

    @Test
    fun `retryFailedOperations should call syncManager retryFailedOperations`() = runTest {
        coEvery { syncManager.retryFailedOperations() } returns Result.success(Unit)
        viewModel = SyncSettingsViewModel(syncManager)

        viewModel.retryFailedOperations()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { syncManager.retryFailedOperations() }
    }

    @Test
    fun `clearSyncedOperations should call syncManager`() = runTest {
        viewModel = SyncSettingsViewModel(syncManager)

        viewModel.clearSyncedOperations()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { syncManager.clearSyncedOperations() }
    }

    @Test
    fun `refreshStatistics should update state from syncManager stats`() = runTest {
        val stats = SyncStatistics(
            pendingOperations = 7,
            successfulOperations = 42,
            failedOperations = 2,
            lastSyncTime = 1234567890L,
            isSyncing = false
        )
        every { syncManager.getSyncStatistics() } returns stats
        viewModel = SyncSettingsViewModel(syncManager)
        // Let observeSyncState complete its initial collection before calling refreshStatistics
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.refreshStatistics()
        // refreshStatistics is synchronous, no need to advance

        val state = viewModel.uiState.value
        assertEquals(7, state.pendingOperations)
        assertEquals(42, state.successfulOperations)
        assertEquals(2, state.failedOperations)
        assertEquals(1234567890L, state.lastSyncTime)
        assertFalse(state.isSyncing)
    }
}
