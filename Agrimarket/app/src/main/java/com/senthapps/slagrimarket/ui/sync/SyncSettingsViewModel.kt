package com.senthapps.slagrimarket.ui.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SyncSettingsViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SyncSettingsUiState())
    val uiState: StateFlow<SyncSettingsUiState> = _uiState.asStateFlow()
    
    init {
        observeSyncState()
    }
    
    private fun observeSyncState() {
        viewModelScope.launch {
            syncManager.syncState.collect { syncState ->
                _uiState.value = _uiState.value.copy(
                    isSyncing = syncState.isSyncing,
                    lastSyncTime = syncState.lastSyncTime,
                    pendingOperations = syncState.pendingOperations,
                    successfulOperations = syncState.successfulOperations,
                    failedOperations = syncState.failedOperations,
                    error = syncState.error
                )
            }
        }
    }
    
    fun toggleAutoSync(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(autoSyncEnabled = enabled)
        syncManager.enableAutoSync(enabled)
    }
    
    fun updateSyncInterval(intervalMs: Long) {
        _uiState.value = _uiState.value.copy(syncInterval = intervalMs)
        syncManager.setSyncInterval(intervalMs)
    }
    
    fun forceSyncNow() {
        viewModelScope.launch {
            try {
                syncManager.forceSyncNow().fold(
                    onSuccess = {
                        Timber.d("Manual sync completed successfully")
                    },
                    onFailure = { error ->
                        Timber.e(error, "Manual sync failed")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error during manual sync")
            }
        }
    }
    
    fun retryFailedOperations() {
        viewModelScope.launch {
            try {
                syncManager.retryFailedOperations().fold(
                    onSuccess = {
                        Timber.d("Retry failed operations completed")
                    },
                    onFailure = { error ->
                        Timber.e(error, "Retry failed operations failed")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error retrying failed operations")
            }
        }
    }
    
    fun clearSyncedOperations() {
        viewModelScope.launch {
            try {
                syncManager.clearSyncedOperations()
                Timber.d("Cleared synced operations")
            } catch (e: Exception) {
                Timber.e(e, "Error clearing synced operations")
            }
        }
    }
    
    fun refreshStatistics() {
        val stats = syncManager.getSyncStatistics()
        _uiState.value = _uiState.value.copy(
            pendingOperations = stats.pendingOperations,
            successfulOperations = stats.successfulOperations,
            failedOperations = stats.failedOperations,
            lastSyncTime = stats.lastSyncTime,
            isSyncing = stats.isSyncing
        )
    }
}

data class SyncSettingsUiState(
    val isSyncing: Boolean = false,
    val lastSyncTime: Long = 0L,
    val pendingOperations: Int = 0,
    val successfulOperations: Int = 0,
    val failedOperations: Int = 0,
    val autoSyncEnabled: Boolean = true,
    val syncInterval: Long = 30_000L, // 30 seconds
    val error: String? = null
)
