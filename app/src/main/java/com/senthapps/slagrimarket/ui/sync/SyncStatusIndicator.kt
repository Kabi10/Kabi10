package com.senthapps.slagrimarket.ui.sync

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * A 12dp persistent sync status indicator dot.
 *
 * States (in priority order):
 *  - Spinner     → sync in progress
 *  - Red dot     → failed operations exist
 *  - Orange dot  → pending operations queued
 *  - Green dot   → fully synced
 */
@Composable
fun SyncStatusDot(
    modifier: Modifier = Modifier,
    viewModel: SyncIndicatorViewModel = hiltViewModel()
) {
    val state by viewModel.syncState.collectAsState()
    SyncStatusDotContent(
        isSyncing = state.isSyncing,
        pendingOperations = state.pendingOperations,
        failedOperations = state.failedOperations,
        modifier = modifier
    )
}

@Composable
internal fun SyncStatusDotContent(
    isSyncing: Boolean,
    pendingOperations: Int,
    failedOperations: Int,
    modifier: Modifier = Modifier
) {
    val description = when {
        isSyncing -> "Syncing"
        failedOperations > 0 -> "Sync failed"
        pendingOperations > 0 -> "Sync pending"
        else -> "Synced"
    }

    if (isSyncing) {
        CircularProgressIndicator(
            modifier = modifier
                .size(12.dp)
                .semantics { contentDescription = description },
            strokeWidth = 2.dp,
            color = Color(0xFF4CAF50)
        )
    } else {
        val dotColor = when {
            failedOperations > 0 -> Color(0xFFF44336)  // red
            pendingOperations > 0 -> Color(0xFFFF9800) // orange
            else -> Color(0xFF4CAF50)                  // green
        }
        Box(
            modifier = modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(dotColor)
                .semantics { contentDescription = description }
        )
    }
}
