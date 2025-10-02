package com.senthapps.slagrimarket.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.UserType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    onContactUser: (String) -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "பரிவர்த்தனைகள்",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Transactions",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
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
        ) {
            // Status filters
            StatusFiltersSection(
                selectedStatus = uiState.selectedStatus,
                onStatusSelected = viewModel::filterTransactionsByStatus
            )
            
            Divider()
            
            // Transactions list
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.getFilteredTransactions().isEmpty()) {
                EmptyTransactionsState()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.getFilteredTransactions()) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            currentUserType = currentUser?.userType ?: UserType.BUYER,
                            onStatusUpdate = { newStatus ->
                                viewModel.updateTransactionStatus(transaction.id, newStatus)
                            },
                            onContactUser = onContactUser,
                            canUpdateStatus = viewModel.canUpdateStatus(
                                transaction, 
                                currentUser?.userType ?: UserType.BUYER
                            ),
                            nextStatus = viewModel.getNextStatusForUser(
                                transaction,
                                currentUser?.userType ?: UserType.BUYER
                            ),
                            actionText = viewModel.getStatusActionText(
                                transaction,
                                currentUser?.userType ?: UserType.BUYER
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusFiltersSection(
    selectedStatus: TransactionStatus?,
    onStatusSelected: (TransactionStatus?) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Filter by Status",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    onClick = { onStatusSelected(null) },
                    label = { Text("All") },
                    selected = selectedStatus == null
                )
            }
            
            items(TransactionStatus.values()) { status ->
                FilterChip(
                    onClick = { onStatusSelected(status) },
                    label = { Text(getStatusDisplayName(status)) },
                    selected = selectedStatus == status
                )
            }
        }
    }
}

@Composable
private fun TransactionCard(
    transaction: Transaction,
    currentUserType: UserType,
    onStatusUpdate: (TransactionStatus) -> Unit,
    onContactUser: (String) -> Unit,
    canUpdateStatus: Boolean,
    nextStatus: TransactionStatus?,
    actionText: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Transaction #${transaction.id.take(8)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatDate(transaction.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                StatusChip(status = transaction.status)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Transaction details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Quantity",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${transaction.quantity} units",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Total Amount",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "LKR ${transaction.totalAmount}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Pickup details
            Text(
                text = "Pickup: ${transaction.pickupLocation}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Date: ${formatDate(transaction.pickupDate)}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            // Action buttons
            if (canUpdateStatus && nextStatus != null && actionText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onStatusUpdate(nextStatus) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(actionText)
                    }
                    
                    OutlinedButton(
                        onClick = { 
                            // Contact the other party
                            val contactId = if (currentUserType == UserType.FARMER) {
                                transaction.buyerId
                            } else {
                                transaction.farmerId
                            }
                            onContactUser(contactId)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Contact",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: TransactionStatus) {
    val (backgroundColor, contentColor) = when (status) {
        TransactionStatus.PENDING -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        TransactionStatus.CONFIRMED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        TransactionStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        TransactionStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        TransactionStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
    }
    
    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = getStatusDisplayName(status),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyTransactionsState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "பரிவர்த்தனைகள் இல்லை",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

private fun getStatusDisplayName(status: TransactionStatus): String {
    return when (status) {
        TransactionStatus.PENDING -> "Pending"
        TransactionStatus.CONFIRMED -> "Confirmed"
        TransactionStatus.IN_PROGRESS -> "In Progress"
        TransactionStatus.COMPLETED -> "Completed"
        TransactionStatus.CANCELLED -> "Cancelled"
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        dateString
    }
}
