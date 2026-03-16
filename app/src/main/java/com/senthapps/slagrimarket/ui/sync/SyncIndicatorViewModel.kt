package com.senthapps.slagrimarket.ui.sync

import androidx.lifecycle.ViewModel
import com.senthapps.slagrimarket.data.sync.SyncManager
import com.senthapps.slagrimarket.data.sync.SyncState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class SyncIndicatorViewModel @Inject constructor(
    private val syncManager: SyncManager
) : ViewModel() {
    val syncState: StateFlow<SyncState> = syncManager.syncState
}
