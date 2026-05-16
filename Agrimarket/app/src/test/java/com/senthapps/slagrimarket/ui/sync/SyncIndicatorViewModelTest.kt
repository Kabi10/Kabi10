package com.senthapps.slagrimarket.ui.sync

import app.cash.turbine.test
import com.senthapps.slagrimarket.data.sync.SyncManager
import com.senthapps.slagrimarket.data.sync.SyncState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SyncIndicatorViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var syncManager: SyncManager
    private val syncStateFlow = MutableStateFlow(SyncState())

    private lateinit var viewModel: SyncIndicatorViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        syncManager = mockk(relaxed = true)
        every { syncManager.syncState } returns syncStateFlow
        viewModel = SyncIndicatorViewModel(syncManager)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `syncState initial value reflects SyncManager default state`() = runTest {
        advanceUntilIdle()
        assertFalse(viewModel.syncState.value.isSyncing)
    }

    @Test
    fun `syncState isSyncing true when SyncManager is syncing`() = runTest {
        syncStateFlow.value = SyncState(isSyncing = true)
        advanceUntilIdle()

        assertTrue(viewModel.syncState.value.isSyncing)
    }

    @Test
    fun `syncState isSyncing false when SyncManager finishes`() = runTest {
        syncStateFlow.value = SyncState(isSyncing = true)
        advanceUntilIdle()
        syncStateFlow.value = SyncState(isSyncing = false)
        advanceUntilIdle()

        assertFalse(viewModel.syncState.value.isSyncing)
    }

    @Test
    fun `syncState pendingOperations reflects SyncManager count`() = runTest {
        syncStateFlow.value = SyncState(pendingOperations = 5)
        advanceUntilIdle()

        assertEquals(5, viewModel.syncState.value.pendingOperations)
    }

    @Test
    fun `syncState conflictCount reflects SyncManager conflicts`() = runTest {
        syncStateFlow.value = SyncState(conflictCount = 2)
        advanceUntilIdle()

        assertEquals(2, viewModel.syncState.value.conflictCount)
    }

    @Test
    fun `syncState emits updates when SyncManager state changes`() = runTest {
        viewModel.syncState.test {
            assertEquals(SyncState(), awaitItem())
            syncStateFlow.value = SyncState(isSyncing = true, pendingOperations = 3)
            val updated = awaitItem()
            assertTrue(updated.isSyncing)
            assertEquals(3, updated.pendingOperations)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `syncState error propagates from SyncManager`() = runTest {
        syncStateFlow.value = SyncState(error = "Network timeout")
        advanceUntilIdle()

        assertEquals("Network timeout", viewModel.syncState.value.error)
    }

    @Test
    fun `syncState lastSyncTime propagates from SyncManager`() = runTest {
        val ts = System.currentTimeMillis()
        syncStateFlow.value = SyncState(lastSyncTime = ts)
        advanceUntilIdle()

        assertEquals(ts, viewModel.syncState.value.lastSyncTime)
    }
}
