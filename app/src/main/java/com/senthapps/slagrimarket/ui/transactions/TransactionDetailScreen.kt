package com.senthapps.slagrimarket.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onContactUser: (String) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)

    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Order Details"
                            "ta" -> "ஆர்டர் விவரங்கள்"
                            "si" -> "ඇණවුම් විස්තර"
                            else -> "Order Details"
                        },
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
                    IconButton(onClick = { /* TODO: Share transaction */ }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (uiState.transaction != null && uiState.canUpdateStatus) {
                BottomActionBar(
                    transaction = uiState.transaction!!,
                    currentUserType = currentUser?.userType ?: UserType.BUYER,
                    onUpdateStatus = { newStatus ->
                        viewModel.updateTransactionStatus(transactionId, newStatus)
                    },
                    onContactUser = {
                        val contactId = if (currentUser?.userType == UserType.FARMER) {
                            uiState.transaction!!.buyerId
                        } else {
                            uiState.transaction!!.farmerId
                        }
                        onContactUser(contactId)
                    },
                    nextStatus = uiState.nextStatus,
                    actionText = uiState.actionText,
                    currentLanguage = currentLanguage
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadTransaction(transactionId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            uiState.transaction != null -> {
                TransactionDetailContent(
                    transaction = uiState.transaction!!,
                    currentLanguage = currentLanguage,
                    currentUserType = currentUser?.userType,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailContent(
    transaction: Transaction,
    currentLanguage: String,
    currentUserType: UserType?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status timeline card
        StatusTimelineCard(
            transaction = transaction,
            currentLanguage = currentLanguage
        )

        // Order summary card
        OrderSummaryCard(
            transaction = transaction,
            currentLanguage = currentLanguage
        )

        // Pickup details card
        PickupDetailsCard(
            transaction = transaction,
            currentLanguage = currentLanguage
        )

        // Parties involved card
        PartiesInvolvedCard(
            transaction = transaction,
            currentUserType = currentUserType,
            currentLanguage = currentLanguage
        )

        // Payment information card
        PaymentInfoCard(
            transaction = transaction,
            currentLanguage = currentLanguage
        )

        // Notes if available
        if (!transaction.notes.isNullOrBlank()) {
            NotesCard(
                notes = transaction.notes,
                currentLanguage = currentLanguage
            )
        }

        // Spacer for bottom bar
        if (currentUserType != null) {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun StatusTimelineCard(
    transaction: Transaction,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = getStatusColor(transaction.status).copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Order Status"
                        "ta" -> "ஆர்டர் நிலை"
                        "si" -> "ඇණවුම් තත්ත්වය"
                        else -> "Order Status"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getStatusColor(transaction.status)
                ) {
                    Text(
                        text = getStatusText(transaction.status, currentLanguage),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Status timeline
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TimelineItem(
                    status = TransactionStatus.PENDING,
                    isCompleted = transaction.status != TransactionStatus.PENDING,
                    isCurrent = transaction.status == TransactionStatus.PENDING,
                    currentLanguage = currentLanguage
                )
                TimelineItem(
                    status = TransactionStatus.CONFIRMED,
                    isCompleted = transaction.status in listOf(TransactionStatus.IN_PROGRESS, TransactionStatus.COMPLETED),
                    isCurrent = transaction.status == TransactionStatus.CONFIRMED,
                    currentLanguage = currentLanguage
                )
                TimelineItem(
                    status = TransactionStatus.IN_PROGRESS,
                    isCompleted = transaction.status == TransactionStatus.COMPLETED,
                    isCurrent = transaction.status == TransactionStatus.IN_PROGRESS,
                    currentLanguage = currentLanguage
                )
                TimelineItem(
                    status = TransactionStatus.COMPLETED,
                    isCompleted = transaction.status == TransactionStatus.COMPLETED,
                    isCurrent = false,
                    currentLanguage = currentLanguage
                )
            }

            if (transaction.status == TransactionStatus.CANCELLED) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Order Cancelled"
                            "ta" -> "ஆர்டர் ரத்து செய்யப்பட்டது"
                            "si" -> "ඇණවුම අවලංගු කරන ලදී"
                            else -> "Order Cancelled"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun TimelineItem(
    status: TransactionStatus,
    isCompleted: Boolean,
    isCurrent: Boolean,
    currentLanguage: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status indicator
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isCompleted -> Color(0xFF4CAF50)
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Text(
            text = getStatusText(status, currentLanguage),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isCompleted -> Color(0xFF4CAF50)
                isCurrent -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

@Composable
private fun OrderSummaryCard(
    transaction: Transaction,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Order Summary"
                    "ta" -> "ஆர்டர் சுருக்கம்"
                    "si" -> "ඇණවුම් සාරාංශය"
                    else -> "Order Summary"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DetailRow(
                label = when (currentLanguage) {
                    "en" -> "Order ID"
                    "ta" -> "ஆர்டர் எண்"
                    "si" -> "ඇණවුම් අංකය"
                    else -> "Order ID"
                },
                value = "#${transaction.id.take(8)}"
            )

            DetailRow(
                label = when (currentLanguage) {
                    "en" -> "Quantity"
                    "ta" -> "அளவு"
                    "si" -> "ප්‍රමාණය"
                    else -> "Quantity"
                },
                value = "${transaction.quantity} ${transaction.unit}"
            )

            DetailRow(
                label = when (currentLanguage) {
                    "en" -> "Price per Unit"
                    "ta" -> "அலகுக்கான விலை"
                    "si" -> "ඒකක මිල"
                    else -> "Price per Unit"
                },
                value = "LKR ${String.format("%.2f", transaction.pricePerUnit)}"
            )

            Divider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Total Amount"
                        "ta" -> "மொத்த தொகை"
                        "si" -> "මුළු මුදල"
                        else -> "Total Amount"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "LKR ${String.format("%.2f", transaction.totalAmount)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun PickupDetailsCard(
    transaction: Transaction,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Pickup Details"
                    "ta" -> "எடுத்துச் செல்லும் விவரங்கள்"
                    "si" -> "ලබා ගැනීමේ විස්තර"
                    else -> "Pickup Details"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Location"
                            "ta" -> "இடம்"
                            "si" -> "ස්ථානය"
                            else -> "Location"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = transaction.pickupLocation,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Date"
                            "ta" -> "தேதி"
                            "si" -> "දිනය"
                            else -> "Date"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDate(transaction.pickupDate),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun PartiesInvolvedCard(
    transaction: Transaction,
    currentUserType: UserType?,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Parties Involved"
                    "ta" -> "சம்பந்தப்பட்டவர்கள்"
                    "si" -> "සම්බන්ධ පාර්ශ්ව"
                    else -> "Parties Involved"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Farmer
            PartyRow(
                label = when (currentLanguage) {
                    "en" -> "Farmer"
                    "ta" -> "விவசாயி"
                    "si" -> "ගොවියා"
                    else -> "Farmer"
                },
                isCurrentUser = currentUserType == UserType.FARMER
            )

            // Buyer
            PartyRow(
                label = when (currentLanguage) {
                    "en" -> "Buyer"
                    "ta" -> "வாங்குபவர்"
                    "si" -> "ගැනුම්කරු"
                    else -> "Buyer"
                },
                isCurrentUser = currentUserType == UserType.BUYER
            )
        }
    }
}

@Composable
private fun PartyRow(
    label: String,
    isCurrentUser: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }

        if (isCurrentUser) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "You",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun PaymentInfoCard(
    transaction: Transaction,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Payment Information"
                    "ta" -> "கட்டண தகவல்"
                    "si" -> "ගෙවීම් තොරතුරු"
                    else -> "Payment Information"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DetailRow(
                label = when (currentLanguage) {
                    "en" -> "Payment Method"
                    "ta" -> "கட்டண முறை"
                    "si" -> "ගෙවීම් ක්‍රමය"
                    else -> "Payment Method"
                },
                value = transaction.paymentMethod.name
            )

            DetailRow(
                label = when (currentLanguage) {
                    "en" -> "Order Date"
                    "ta" -> "ஆர்டர் தேதி"
                    "si" -> "ඇණවුම් දිනය"
                    else -> "Order Date"
                },
                value = formatDate(transaction.createdAt)
            )
        }
    }
}

@Composable
private fun NotesCard(
    notes: String,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Additional Notes"
                    "ta" -> "கூடுதல் குறிப்புகள்"
                    "si" -> "අමතර සටහන්"
                    else -> "Additional Notes"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = notes,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun BottomActionBar(
    transaction: Transaction,
    currentUserType: UserType,
    onUpdateStatus: (TransactionStatus) -> Unit,
    onContactUser: () -> Unit,
    nextStatus: TransactionStatus?,
    actionText: String?,
    currentLanguage: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onContactUser,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Contact"
                        "ta" -> "தொடர்பு"
                        "si" -> "සම්බන්ධ"
                        else -> "Contact"
                    }
                )
            }

            if (nextStatus != null && actionText != null) {
                Button(
                    onClick = { onUpdateStatus(nextStatus) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}

private fun getStatusColor(status: TransactionStatus): Color {
    return when (status) {
        TransactionStatus.PENDING -> Color(0xFFFF9800)
        TransactionStatus.CONFIRMED -> Color(0xFF2196F3)
        TransactionStatus.IN_PROGRESS -> Color(0xFF9C27B0)
        TransactionStatus.COMPLETED -> Color(0xFF4CAF50)
        TransactionStatus.CANCELLED -> Color(0xFFF44336)
    }
}

private fun getStatusText(status: TransactionStatus, language: String): String {
    return when (status) {
        TransactionStatus.PENDING -> when (language) {
            "ta" -> "நிலுவையில்"
            "si" -> "අපේක්ෂාවේ"
            else -> "Pending"
        }
        TransactionStatus.CONFIRMED -> when (language) {
            "ta" -> "உறுதிப்படுத்தப்பட்டது"
            "si" -> "තහවුරු කළා"
            else -> "Confirmed"
        }
        TransactionStatus.IN_PROGRESS -> when (language) {
            "ta" -> "செயல்பாட்டில்"
            "si" -> "ක්‍රියාත්මකයි"
            else -> "In Progress"
        }
        TransactionStatus.COMPLETED -> when (language) {
            "ta" -> "முடிந்தது"
            "si" -> "සම්පූර්ණයි"
            else -> "Completed"
        }
        TransactionStatus.CANCELLED -> when (language) {
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
