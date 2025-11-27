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
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.ui.components.EmptyTransactionsState
import com.senthapps.slagrimarket.ui.components.EnhancedTransactionCard
import com.senthapps.slagrimarket.ui.components.TransactionCardSkeleton
import com.senthapps.slagrimarket.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit = {},
    onContactUser: (String) -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Transactions"
                            "ta" -> "பரிவர்த்தனைகள்"
                            "si" -> "ගනුදෙනු"
                            else -> "Transactions"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = when (currentLanguage) {
                                "en" -> "Back"
                                "ta" -> "பின்செல்"
                                "si" -> "ආපසු"
                                else -> "Back"
                            }
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
                onStatusSelected = viewModel::filterTransactionsByStatus,
                currentLanguage = currentLanguage
            )
            
            HorizontalDivider()
            
            // Transactions list
            when {
                // Loading state with shimmer skeletons
                uiState.isLoading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.Large),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                    ) {
                        items(4) {
                            TransactionCardSkeleton()
                        }
                    }
                }

                // Empty state
                viewModel.getFilteredTransactions().isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        EmptyTransactionsState(
                            currentLanguage = currentLanguage,
                            onBrowseListings = null // Can be connected to browse action
                        )
                    }
                }

                // Content state with enhanced transaction cards
                else -> {
                    LazyColumn(
                        contentPadding = PaddingValues(Spacing.Large),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                    ) {
                        items(viewModel.getFilteredTransactions()) { transaction ->
                            EnhancedTransactionCard(
                                transaction = transaction,
                                currentLanguage = currentLanguage,
                                onClick = { onTransactionClick(transaction.id) },
                                onActionClick = if (viewModel.canUpdateStatus(
                                    transaction,
                                    currentUser?.userType ?: UserType.BUYER
                                )) {
                                    {
                                        viewModel.getNextStatusForUser(
                                            transaction,
                                            currentUser?.userType ?: UserType.BUYER
                                        )?.let { nextStatus ->
                                            viewModel.updateTransactionStatus(transaction.id, nextStatus)
                                        }
                                    }
                                } else null,
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
}

@Composable
private fun StatusFiltersSection(
    selectedStatus: TransactionStatus?,
    onStatusSelected: (TransactionStatus?) -> Unit,
    currentLanguage: String
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = when (currentLanguage) {
                "en" -> "Filter by Status"
                "ta" -> "நிலையின்படி வடிகட்டு"
                "si" -> "තත්ත්වය අනුව පෙරන්න"
                else -> "Filter by Status"
            },
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
                    label = {
                        Text(when (currentLanguage) {
                            "en" -> "All"
                            "ta" -> "அனைத்தும்"
                            "si" -> "සියල්ල"
                            else -> "All"
                        })
                    },
                    selected = selectedStatus == null
                )
            }

            items(TransactionStatus.values()) { status ->
                FilterChip(
                    onClick = { onStatusSelected(status) },
                    label = { Text(getStatusDisplayName(status, currentLanguage)) },
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
    onClick: () -> Unit,
    onStatusUpdate: (TransactionStatus) -> Unit,
    onContactUser: (String) -> Unit,
    canUpdateStatus: Boolean,
    nextStatus: TransactionStatus?,
    actionText: String?,
    currentLanguage: String
) {
    Card(
        onClick = onClick,
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
                        text = when (currentLanguage) {
                            "en" -> "Transaction #${transaction.id.take(8)}"
                            "ta" -> "பரிவர்த்தனை #${transaction.id.take(8)}"
                            "si" -> "ගනුදෙනුව #${transaction.id.take(8)}"
                            else -> "Transaction #${transaction.id.take(8)}"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = formatDate(transaction.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                StatusChip(status = transaction.status, currentLanguage = currentLanguage)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Transaction details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Quantity"
                            "ta" -> "அளவு"
                            "si" -> "ප්‍රමාණය"
                            else -> "Quantity"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "${transaction.quantity} units"
                            "ta" -> "${transaction.quantity} அலகுகள்"
                            "si" -> "${transaction.quantity} ඒකක"
                            else -> "${transaction.quantity} units"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Total Amount"
                            "ta" -> "மொத்த தொகை"
                            "si" -> "මුළු මුදල"
                            else -> "Total Amount"
                        },
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
                text = when (currentLanguage) {
                    "en" -> "Pickup: ${transaction.pickupLocation}"
                    "ta" -> "எடுத்துச் செல்லும் இடம்: ${transaction.pickupLocation}"
                    "si" -> "ලබා ගන්නා ස්ථානය: ${transaction.pickupLocation}"
                    else -> "Pickup: ${transaction.pickupLocation}"
                },
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = when (currentLanguage) {
                    "en" -> "Date: ${formatDate(transaction.pickupDate)}"
                    "ta" -> "தேதி: ${formatDate(transaction.pickupDate)}"
                    "si" -> "දිනය: ${formatDate(transaction.pickupDate)}"
                    else -> "Date: ${formatDate(transaction.pickupDate)}"
                },
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
                            contentDescription = when (currentLanguage) {
                                "en" -> "Contact"
                                "ta" -> "தொடர்பு கொள்ள"
                                "si" -> "සම්බන්ධ වන්න"
                                else -> "Contact"
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: TransactionStatus, currentLanguage: String) {
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
            text = getStatusDisplayName(status, currentLanguage),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun EmptyTransactionsState(currentLanguage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (currentLanguage) {
                "en" -> "No transactions yet"
                "ta" -> "பரிவர்த்தனைகள் இல்லை"
                "si" -> "තවම ගනුදෙනු නැත"
                else -> "No transactions yet"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = when (currentLanguage) {
                "en" -> "Your orders will appear here"
                "ta" -> "உங்கள் ஆர்டர்கள் இங்கே தோன்றும்"
                "si" -> "ඔබේ ඇණවුම් මෙහි දිස්වනු ඇත"
                else -> "Your orders will appear here"
            },
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

private fun getStatusDisplayName(status: TransactionStatus, currentLanguage: String): String {
    return when (status) {
        TransactionStatus.PENDING -> when (currentLanguage) {
            "en" -> "Pending"
            "ta" -> "நிலுவையில்"
            "si" -> "අපේක්ෂාවේ"
            else -> "Pending"
        }
        TransactionStatus.CONFIRMED -> when (currentLanguage) {
            "en" -> "Confirmed"
            "ta" -> "உறுதிப்படுத்தப்பட்டது"
            "si" -> "තහවුරු කළා"
            else -> "Confirmed"
        }
        TransactionStatus.IN_PROGRESS -> when (currentLanguage) {
            "en" -> "In Progress"
            "ta" -> "செயல்பாட்டில்"
            "si" -> "ක්‍රියාත්මකයි"
            else -> "In Progress"
        }
        TransactionStatus.COMPLETED -> when (currentLanguage) {
            "en" -> "Completed"
            "ta" -> "முடிந்தது"
            "si" -> "සම්පූර්ණයි"
            else -> "Completed"
        }
        TransactionStatus.CANCELLED -> when (currentLanguage) {
            "en" -> "Cancelled"
            "ta" -> "ரத்து செய்யப்பட்டது"
            "si" -> "අවලංගු කරන ලදී"
            else -> "Cancelled"
        }
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
