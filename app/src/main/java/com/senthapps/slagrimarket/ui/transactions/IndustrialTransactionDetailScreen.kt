package com.senthapps.slagrimarket.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.ui.components.IndustrialButton
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

// ============================================================================
// HUMAN INDUSTRIAL TRANSACTION DETAIL SCREEN v1.0
// "Your Order Receipt" - Clear, dignified, no confusion
// Earth headers, Rice backgrounds, Gold for prices
// ============================================================================

/**
 * Industrial transaction/order detail screen
 *
 * @param transactionId The ID of the transaction to display
 * @param onNavigateBack Callback to navigate back
 * @param onContactUser Callback when user wants to contact buyer/seller
 * @param viewModel ViewModel for transaction details
 */
@Composable
fun IndustrialTransactionDetailScreen(
    transactionId: String,
    onNavigateBack: () -> Unit,
    onContactUser: (String) -> Unit,
    viewModel: TransactionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState(initial = null)

    // Load transaction on screen entry
    LaunchedEffect(transactionId) {
        viewModel.loadTransaction(transactionId)
    }

    // Outer container with safe drawing insets
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Earth)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Main content with outer border
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = BorderWidth.Thick,
                    color = HumanIndustrial.Earth,
                    shape = RectangleShape
                )
                .background(HumanIndustrial.Rice)
        ) {
            // Header with back button
            IndustrialHeader(
                title = "ORDER DETAILS",
                onNavigateBack = onNavigateBack
            )

            // Content based on state
            when {
                uiState.isLoading -> {
                    LoadingState()
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = { viewModel.loadTransaction(transactionId) }
                    )
                }
                uiState.transaction != null -> {
                    TransactionContent(
                        transaction = uiState.transaction!!,
                        onContactUser = {
                            val contactId = if (currentUser?.userType?.name == "FARMER") {
                                uiState.transaction!!.buyerId
                            } else {
                                uiState.transaction!!.farmerId
                            }
                            onContactUser(contactId)
                        },
                        canUpdateStatus = uiState.canUpdateStatus,
                        nextStatus = uiState.nextStatus,
                        actionText = uiState.actionText,
                        onUpdateStatus = { newStatus ->
                            viewModel.updateTransactionStatus(transactionId, newStatus)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun IndustrialHeader(
    title: String,
    onNavigateBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(HumanIndustrial.Earth)
            .padding(horizontal = Spacing.md.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .clickable(
                    onClick = onNavigateBack,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
                .padding(end = Spacing.md.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "BACK",
                tint = HumanIndustrial.Rice
            )
        }
        Text(
            text = title,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Rice
        )
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "LOADING ORDER...",
            style = HumanIndustrialType.emptyState,
            color = HumanIndustrial.Stone
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "FAILED TO LOAD ORDER",
            style = HumanIndustrialType.productName,
            color = HumanIndustrial.Urgent
        )
        Spacer(modifier = Modifier.height(Spacing.md.dp))
        Text(
            text = error.uppercase(),
            style = HumanIndustrialType.unit,
            color = HumanIndustrial.Stone
        )
        Spacer(modifier = Modifier.height(Spacing.xl.dp))
        IndustrialButton(
            text = "RETRY",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TransactionContent(
    transaction: Transaction,
    onContactUser: () -> Unit,
    canUpdateStatus: Boolean,
    nextStatus: TransactionStatus?,
    actionText: String?,
    onUpdateStatus: (TransactionStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Order ID and Status header
        OrderHeader(transaction)

        // Order summary section
        SectionDivider()
        OrderSummary(transaction)

        // Pickup details section
        SectionDivider()
        PickupDetails(transaction)

        // Payment info section
        SectionDivider()
        PaymentInfo(transaction)

        // Notes (if any)
        if (!transaction.notes.isNullOrBlank()) {
            SectionDivider()
            NotesSection(transaction.notes!!)
        }

        // Action buttons
        SectionDivider()
        ActionButtons(
            onContactUser = onContactUser,
            canUpdateStatus = canUpdateStatus,
            nextStatus = nextStatus,
            actionText = actionText,
            onUpdateStatus = onUpdateStatus
        )

        Spacer(modifier = Modifier.height(Spacing.Double))
    }
}

@Composable
private fun OrderHeader(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(HumanIndustrial.Dust)
            .padding(Spacing.md.dp)
    ) {
        Text(
            text = "ORDER #${transaction.id.take(8).uppercase()}",
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink
        )
        Spacer(modifier = Modifier.height(Spacing.sm.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "STATUS",
                style = HumanIndustrialType.sectionLabel,
                color = HumanIndustrial.Stone
            )
            Text(
                text = getStatusText(transaction.status),
                style = HumanIndustrialType.productName,
                color = getStatusColor(transaction.status)
            )
        }
    }
}

@Composable
private fun SectionDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BorderWidth.Thin)
            .background(HumanIndustrial.Earth)
    )
}

@Composable
private fun OrderSummary(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md.dp)
    ) {
        Text(
            text = "ORDER SUMMARY",
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Ink
        )
        Spacer(modifier = Modifier.height(Spacing.md.dp))

        DetailRow("QUANTITY", "${transaction.quantity.toInt()} ${transaction.unit.uppercase()}")
        DetailRow("PRICE PER UNIT", "Rs. ${transaction.pricePerUnit.toInt()}")

        Spacer(modifier = Modifier.height(Spacing.md.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "TOTAL",
                style = HumanIndustrialType.productName,
                color = HumanIndustrial.Ink
            )
            Text(
                text = "Rs. ${transaction.totalAmount.toInt()}",
                style = HumanIndustrialType.price,
                color = HumanIndustrial.Gold
            )
        }
    }
}

@Composable
private fun PickupDetails(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md.dp)
    ) {
        Text(
            text = "PICKUP DETAILS",
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Ink
        )
        Spacer(modifier = Modifier.height(Spacing.md.dp))

        DetailRow("LOCATION", transaction.pickupLocation.uppercase())
        DetailRow("DATE", formatDate(transaction.pickupDate))
    }
}

@Composable
private fun PaymentInfo(transaction: Transaction) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md.dp)
    ) {
        Text(
            text = "PAYMENT",
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Ink
        )
        Spacer(modifier = Modifier.height(Spacing.md.dp))

        DetailRow("METHOD", transaction.paymentMethod.name.uppercase())
        DetailRow("ORDER DATE", formatDate(transaction.createdAt))
    }
}

@Composable
private fun NotesSection(notes: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md.dp)
    ) {
        Text(
            text = "NOTES",
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Ink
        )
        Spacer(modifier = Modifier.height(Spacing.sm.dp))
        Text(
            text = notes.uppercase(),
            style = HumanIndustrialType.unit,
            color = HumanIndustrial.Stone
        )
    }
}

@Composable
private fun ActionButtons(
    onContactUser: () -> Unit,
    canUpdateStatus: Boolean,
    nextStatus: TransactionStatus?,
    actionText: String?,
    onUpdateStatus: (TransactionStatus) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.md.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Contact button (always visible)
        IndustrialButton(
            text = "CONTACT",
            onClick = onContactUser,
            isPrimary = false,
            modifier = Modifier.fillMaxWidth()
        )

        // Status update button (if available)
        if (canUpdateStatus && nextStatus != null && actionText != null) {
            IndustrialButton(
                text = actionText.uppercase(),
                onClick = { onUpdateStatus(nextStatus) },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = HumanIndustrialType.unit,
            color = HumanIndustrial.Stone
        )
        Text(
            text = value,
            style = HumanIndustrialType.body.copy(fontWeight = FontWeight.Bold),
            color = HumanIndustrial.Ink
        )
    }
}

private fun getStatusText(status: TransactionStatus): String {
    return when (status) {
        TransactionStatus.PENDING -> "PENDING"
        TransactionStatus.CONFIRMED -> "CONFIRMED"
        TransactionStatus.IN_PROGRESS -> "IN PROGRESS"
        TransactionStatus.COMPLETED -> "COMPLETED"
        TransactionStatus.CANCELLED -> "CANCELLED"
    }
}

private fun getStatusColor(status: TransactionStatus): androidx.compose.ui.graphics.Color {
    return when (status) {
        TransactionStatus.PENDING -> HumanIndustrial.Stone
        TransactionStatus.CONFIRMED -> HumanIndustrial.Gold
        TransactionStatus.IN_PROGRESS -> HumanIndustrial.Gold
        TransactionStatus.COMPLETED -> HumanIndustrial.Green
        TransactionStatus.CANCELLED -> HumanIndustrial.Urgent
    }
}

private fun formatDate(dateString: String): String {
    return try {
        val instant = Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        instant.atZone(ZoneId.systemDefault()).format(formatter).uppercase()
    } catch (e: Exception) {
        dateString.uppercase()
    }
}
