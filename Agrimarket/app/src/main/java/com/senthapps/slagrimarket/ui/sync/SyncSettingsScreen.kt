package com.senthapps.slagrimarket.ui.sync

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.R
import java.text.SimpleDateFormat
import java.util.*

@Composable
private fun ConflictBanner(conflictCount: Int, onDismiss: () -> Unit) {
    if (conflictCount > 0) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.sync_conflict_message, conflictCount),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_dismiss))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SyncSettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.sync_settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = viewModel::forceSyncNow,
                        enabled = !uiState.isSyncing
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.sync_action_now)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Conflict banner — shown immediately when conflicts exist
            ConflictBanner(
                conflictCount = uiState.conflictCount,
                onDismiss = viewModel::dismissConflicts
            )

            // Sync Status Card
            SyncStatusCard(
                isSyncing = uiState.isSyncing,
                lastSyncTime = uiState.lastSyncTime,
                pendingOperations = uiState.pendingOperations,
                failedOperations = uiState.failedOperations,
                error = uiState.error
            )

            // Sync Settings Card
            SyncSettingsCard(
                autoSyncEnabled = uiState.autoSyncEnabled,
                onAutoSyncToggle = viewModel::toggleAutoSync,
                syncInterval = uiState.syncInterval,
                onSyncIntervalChange = viewModel::updateSyncInterval
            )

            // Sync Actions Card
            SyncActionsCard(
                onForceSyncNow = viewModel::forceSyncNow,
                onRetryFailedOps = viewModel::retryFailedOperations,
                onClearSyncedOps = viewModel::clearSyncedOperations,
                isSyncing = uiState.isSyncing,
                hasFailedOps = uiState.failedOperations > 0
            )

            // Sync Statistics Card
            SyncStatisticsCard(
                pendingOperations = uiState.pendingOperations,
                successfulOperations = uiState.successfulOperations,
                failedOperations = uiState.failedOperations,
                conflictCount = uiState.conflictCount
            )
        }
    }
}

@Composable
private fun SyncStatusCard(
    isSyncing: Boolean,
    lastSyncTime: Long,
    pendingOperations: Int,
    failedOperations: Int,
    error: String?
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.sync_status_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.sync_current_status))
                if (isSyncing) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.sync_syncing), color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Text(
                        text = if (pendingOperations > 0 || failedOperations > 0) stringResource(R.string.sync_status_pending) else stringResource(R.string.sync_status_up_to_date),
                        color = if (pendingOperations > 0 || failedOperations > 0)
                            MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.sync_last_sync))
                Text(
                    text = if (lastSyncTime > 0) {
                        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
                            .format(Date(lastSyncTime))
                    } else stringResource(R.string.sync_never),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (error != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = stringResource(R.string.sync_error),
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SyncSettingsCard(
    autoSyncEnabled: Boolean,
    onAutoSyncToggle: (Boolean) -> Unit,
    syncInterval: Long,
    onSyncIntervalChange: (Long) -> Unit
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.sync_auto_label))
                    Text(
                        text = stringResource(R.string.sync_auto_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = autoSyncEnabled,
                    onCheckedChange = onAutoSyncToggle
                )
            }
            
            if (autoSyncEnabled) {
                Column {
                    Text(stringResource(R.string.sync_interval_label))
                    Text(
                        text = stringResource(R.string.sync_interval_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "30s" to 30_000L,
                            "1m" to 60_000L,
                            "5m" to 300_000L,
                            "15m" to 900_000L
                        ).forEach { (label, interval) ->
                            FilterChip(
                                onClick = { onSyncIntervalChange(interval) },
                                label = { Text(label) },
                                selected = syncInterval == interval
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SyncActionsCard(
    onForceSyncNow: () -> Unit,
    onRetryFailedOps: () -> Unit,
    onClearSyncedOps: () -> Unit,
    isSyncing: Boolean,
    hasFailedOps: Boolean
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.sync_actions_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Button(
                onClick = onForceSyncNow,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sync_action_now))
            }

            if (hasFailedOps) {
                OutlinedButton(
                    onClick = onRetryFailedOps,
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.sync_retry_failed))
                }
            }

            OutlinedButton(
                onClick = onClearSyncedOps,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.sync_clear_synced))
            }
        }
    }
}

@Composable
private fun SyncStatisticsCard(
    pendingOperations: Int,
    successfulOperations: Int,
    failedOperations: Int,
    conflictCount: Int = 0
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.sync_statistics_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.sync_pending_ops_label))
                Text(
                    text = pendingOperations.toString(),
                    color = if (pendingOperations > 0) MaterialTheme.colorScheme.secondary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.sync_successful_ops_label))
                Text(
                    text = successfulOperations.toString(),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.sync_failed_ops_label))
                Text(
                    text = failedOperations.toString(),
                    color = if (failedOperations > 0) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(stringResource(R.string.sync_conflicts_label))
                Text(
                    text = conflictCount.toString(),
                    color = if (conflictCount > 0) MaterialTheme.colorScheme.error
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
