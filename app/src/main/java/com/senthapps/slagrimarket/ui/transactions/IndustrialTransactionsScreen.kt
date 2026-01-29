package com.senthapps.slagrimarket.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.home.AppLanguage
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable
import com.senthapps.slagrimarket.util.RelativeTimeUtil

// ============================================================================
// HUMAN INDUSTRIAL TRANSACTIONS/ORDERS SCREEN v2.0
// "Your Orders" - Past and active transactions
// 72dp header, 96dp rows with Earth accent bar, status colors
// ============================================================================

/**
 * Transaction status enum with color mapping
 */
enum class OrderStatus {
    ACTIVE,
    COMPLETED,
    PROBLEM
}

/**
 * Order preview data class
 */
data class OrderPreview(
    val id: String,
    val counterpartName: String,
    val amount: Double,
    val status: OrderStatus,
    val timeText: String
)

/**
 * Industrial transactions/orders list screen
 *
 * Header: 72dp Rice with back text and title
 * Rows: 96dp with Earth accent bar, counterpart name, amount, status, time
 *
 * @param orders List of orders
 * @param language Current language for labels
 * @param onOrderClick Callback when order is clicked
 * @param onNavigateBack Callback to navigate back
 * @param isLoading Loading state
 * @param isError Error state
 * @param onRetry Retry callback
 */
@Composable
fun IndustrialTransactionsScreen(
    orders: List<OrderPreview>,
    language: AppLanguage = AppLanguage.SINHALA,
    onOrderClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    isError: Boolean = false,
    onRetry: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
    ) {
        // Header - 72dp
        OrdersHeader(
            backText = when (language) {
                AppLanguage.SINHALA -> "ආපසු"
                AppLanguage.TAMIL -> "பின்"
                AppLanguage.ENGLISH -> "BACK"
            },
            title = when (language) {
                AppLanguage.SINHALA -> "ගනුදෙනු"
                AppLanguage.TAMIL -> "ஆர்டர்கள்"
                AppLanguage.ENGLISH -> "ORDERS"
            },
            onBackClick = onNavigateBack
        )

        // 4dp Earth divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BorderWidth.Thick)
                .background(HumanIndustrial.Earth)
        )

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "රැඳී සිටින්න"
                            AppLanguage.TAMIL -> "காத்திருங்கள்"
                            AppLanguage.ENGLISH -> "WAIT"
                        },
                        style = HumanIndustrialType.body,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            isError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.SINHALA -> "සම්බන්ධ වීමට නොහැකි විය"
                                AppLanguage.TAMIL -> "இணைக்க முடியவில்லை"
                                AppLanguage.ENGLISH -> "COULD NOT CONNECT"
                            },
                            style = HumanIndustrialType.body,
                            color = HumanIndustrial.Stone
                        )
                        SecondaryButton(
                            text = when (language) {
                                AppLanguage.SINHALA -> "නැවත උත්සාහ කරන්න"
                                AppLanguage.TAMIL -> "மீண்டும் முயற்சிக்கவும்"
                                AppLanguage.ENGLISH -> "TRY AGAIN"
                            },
                            onClick = onRetry
                        )
                    }
                }
            }
            orders.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "මෙහි තවම කිසිවක් නැත"
                            AppLanguage.TAMIL -> "இங்கே இன்னும் எதுவும் இல்லை"
                            AppLanguage.ENGLISH -> "NOTHING HERE YET"
                        },
                        style = HumanIndustrialType.body,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(orders) { index, order ->
                        OrderRow(
                            order = order,
                            language = language,
                            useAlternateBackground = index % 2 == 1,
                            onClick = { onOrderClick(order.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Header component - 72dp Rice background
 */
@Composable
private fun OrdersHeader(
    backText: String,
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(HumanIndustrial.Rice)
            .padding(horizontal = Spacing.lg.dp, vertical = Spacing.md.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .height(48.dp)
                .industrialClickable(onClick = onBackClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = backText,
                style = HumanIndustrialType.sectionLabel,
                color = HumanIndustrial.Earth
            )
        }

        Text(
            text = title,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

/**
 * Order row - 96dp height per UI plan
 *
 * Layout:
 * ▌ Counterpart Name (20sp Bold Ink)
 * ▌ Amount (20sp Bold Gold)    Status (14sp Bold, status-colored)
 * ▌                             Time (14sp Regular Stone)
 */
@Composable
private fun OrderRow(
    order: OrderPreview,
    language: AppLanguage,
    useAlternateBackground: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice

    val statusText = when (order.status) {
        OrderStatus.ACTIVE -> when (language) {
            AppLanguage.SINHALA -> "සක්‍රීයයි"
            AppLanguage.TAMIL -> "செயலில்"
            AppLanguage.ENGLISH -> "ACTIVE"
        }
        OrderStatus.COMPLETED -> when (language) {
            AppLanguage.SINHALA -> "අවසන් විය"
            AppLanguage.TAMIL -> "முடிந்தது"
            AppLanguage.ENGLISH -> "DONE"
        }
        OrderStatus.PROBLEM -> when (language) {
            AppLanguage.SINHALA -> "ගැටලුවක්"
            AppLanguage.TAMIL -> "சிக்கல்"
            AppLanguage.ENGLISH -> "PROBLEM"
        }
    }

    val statusColor = when (order.status) {
        OrderStatus.ACTIVE -> HumanIndustrial.Green
        OrderStatus.COMPLETED -> HumanIndustrial.Stone
        OrderStatus.PROBLEM -> HumanIndustrial.Urgent
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(96.dp)
            .background(backgroundColor)
            .industrialClickable(onClick = onClick)
    ) {
        // 4dp Earth accent bar
        Box(
            modifier = Modifier
                .width(BorderWidth.Accent)
                .height(96.dp)
                .background(HumanIndustrial.Earth)
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = Spacing.lg.dp,
                    end = Spacing.lg.dp,
                    top = Spacing.md.dp,
                    bottom = Spacing.md.dp
                ),
            verticalArrangement = Arrangement.spacedBy(Spacing.sm.dp)
        ) {
            // Line 1: Counterpart name
            Text(
                text = order.counterpartName,
                style = HumanIndustrialType.productName,
                color = HumanIndustrial.Ink
            )

            // Line 2: Amount + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "රු ${order.amount.toInt().toString().chunked(3).joinToString(",")}",
                    style = HumanIndustrialType.productName,
                    color = HumanIndustrial.Gold
                )
                Text(
                    text = statusText,
                    style = HumanIndustrialType.sectionLabel,
                    color = statusColor
                )
            }

            // Line 3: Time (right-aligned)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = order.timeText,
                    style = HumanIndustrialType.timestamp,
                    color = HumanIndustrial.Stone
                )
            }
        }
    }
}

// ============================================================================
// BACKWARD COMPATIBILITY - Keep old function signature with ViewModel
// ============================================================================

/**
 * Legacy function signature for backward compatibility with ViewModel
 */
@Composable
fun IndustrialTransactionsScreen(
    onNavigateBack: () -> Unit,
    onTransactionClick: (String) -> Unit = {},
    viewModel: TransactionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions = viewModel.getFilteredTransactions()

    val convertedOrders = transactions.map { tx ->
        OrderPreview(
            id = tx.id,
            counterpartName = tx.buyerName.ifEmpty { tx.sellerName },
            amount = tx.totalAmount,
            status = when (tx.status) {
                com.senthapps.slagrimarket.data.model.TransactionStatus.PENDING,
                com.senthapps.slagrimarket.data.model.TransactionStatus.CONFIRMED,
                com.senthapps.slagrimarket.data.model.TransactionStatus.IN_PROGRESS -> OrderStatus.ACTIVE
                com.senthapps.slagrimarket.data.model.TransactionStatus.COMPLETED -> OrderStatus.COMPLETED
                com.senthapps.slagrimarket.data.model.TransactionStatus.CANCELLED -> OrderStatus.PROBLEM
            },
            timeText = RelativeTimeUtil.getRelativeTimeString(tx.createdAt, "en")
        )
    }

    IndustrialTransactionsScreen(
        orders = convertedOrders,
        language = AppLanguage.ENGLISH,
        onOrderClick = onTransactionClick,
        onNavigateBack = onNavigateBack,
        isLoading = uiState.isLoading
    )
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TransactionsPreview() {
    val sampleOrders = listOf(
        OrderPreview(
            id = "1",
            counterpartName = "සුනිල් මහතා",
            amount = 2800.0,
            status = OrderStatus.ACTIVE,
            timeText = "දැන්"
        ),
        OrderPreview(
            id = "2",
            counterpartName = "කමල් මහතා",
            amount = 4500.0,
            status = OrderStatus.COMPLETED,
            timeText = "ඊයේ"
        ),
        OrderPreview(
            id = "3",
            counterpartName = "නිමල් මහතා",
            amount = 1200.0,
            status = OrderStatus.PROBLEM,
            timeText = "දින 2කට පෙර"
        )
    )

    IndustrialTransactionsScreen(
        orders = sampleOrders,
        language = AppLanguage.SINHALA,
        onOrderClick = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TransactionsEnglishPreview() {
    val sampleOrders = listOf(
        OrderPreview(
            id = "1",
            counterpartName = "Mr. Sunil",
            amount = 2800.0,
            status = OrderStatus.ACTIVE,
            timeText = "Just now"
        ),
        OrderPreview(
            id = "2",
            counterpartName = "Mr. Kamal",
            amount = 4500.0,
            status = OrderStatus.COMPLETED,
            timeText = "Yesterday"
        )
    )

    IndustrialTransactionsScreen(
        orders = sampleOrders,
        language = AppLanguage.ENGLISH,
        onOrderClick = {},
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TransactionsEmptyPreview() {
    IndustrialTransactionsScreen(
        orders = emptyList(),
        language = AppLanguage.SINHALA,
        onOrderClick = {},
        onNavigateBack = {}
    )
}
