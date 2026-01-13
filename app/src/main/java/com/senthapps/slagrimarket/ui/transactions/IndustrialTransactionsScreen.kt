package com.senthapps.slagrimarket.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing

// ============================================================================
// HUMAN INDUSTRIAL TRANSACTIONS SCREEN v1.0
// "Your Orders" - Clear, dignified, no confusion
// Earth header, Rice/Dust alternating rows, Gold prices
// ============================================================================

/**
 * Industrial transactions/orders screen
 *
 * @param onNavigateBack Callback to navigate back
 * @param onTransactionClick Callback when user taps a transaction
 * @param viewModel ViewModel for transactions
 */
@Composable
fun IndustrialTransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions = viewModel.getFilteredTransactions()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HumanIndustrial.Rice,
        topBar = {
            // Earth header with Rice text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(HumanIndustrial.Earth)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HumanIndustrial.Rice
                    )
                }

                Text(
                    text = "ORDERS",
                    style = HumanIndustrialType.screenTitle,
                    color = HumanIndustrial.Rice,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                // Loading state - text only, no spinner
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOADING ORDERS...",
                        style = HumanIndustrialType.emptyState,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            transactions.isEmpty() -> {
                // Empty state - text only
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No orders yet",
                        style = HumanIndustrialType.emptyState,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            else -> {
                // Content state - list of orders
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    itemsIndexed(transactions) { index, transaction ->
                        TransactionRow(
                            transaction = transaction,
                            useAlternateBackground = index % 2 == 1,
                            onClick = { onTransactionClick(transaction.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single row in the transactions list
 * Order ID + Status on left, Amount on right
 * Rice/Dust alternating backgrounds, Gold prices
 */
@Composable
private fun TransactionRow(
    transaction: Transaction,
    useAlternateBackground: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = Spacing.md.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Order ID + Status
        Column(
            verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
        ) {
            Text(
                text = "ORDER #${transaction.id.take(8).uppercase()}",
                style = HumanIndustrialType.priceRowProduct,
                color = HumanIndustrial.Ink
            )
            Text(
                text = getStatusText(transaction.status),
                style = HumanIndustrialType.unit,
                color = HumanIndustrial.Stone
            )
        }

        // Right side: Amount + Quantity
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
        ) {
            Text(
                text = "Rs. ${transaction.totalAmount.toInt()}",
                style = HumanIndustrialType.priceSmall,
                color = HumanIndustrial.Gold
            )
            Text(
                text = "${transaction.quantity.toInt()} ${transaction.unit.uppercase()}",
                style = HumanIndustrialType.unit,
                color = HumanIndustrial.Stone
            )
        }
    }
}

/**
 * Get display text for transaction status - ALL CAPS
 */
private fun getStatusText(status: TransactionStatus): String {
    return when (status) {
        TransactionStatus.PENDING -> "PENDING"
        TransactionStatus.CONFIRMED -> "CONFIRMED"
        TransactionStatus.IN_PROGRESS -> "IN PROGRESS"
        TransactionStatus.COMPLETED -> "COMPLETED"
        TransactionStatus.CANCELLED -> "CANCELLED"
    }
}
