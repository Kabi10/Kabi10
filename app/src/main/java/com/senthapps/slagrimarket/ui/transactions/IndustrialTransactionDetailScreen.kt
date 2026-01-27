package com.senthapps.slagrimarket.ui.transactions

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.ui.components.PrimaryButton
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.home.AppLanguage
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable

// ============================================================================
// HUMAN INDUSTRIAL TRANSACTION DETAIL SCREEN v2.0
// "Your Order Details" - Full details of a transaction/order
// 72dp Rice header, sections separated by 4dp Earth dividers
// ============================================================================

/**
 * Data class for transaction detail
 */
data class TransactionDetail(
    val id: String,
    val productName: String,
    val quantity: Double,
    val unit: String,
    val pricePerUnit: Double,
    val totalAmount: Double,
    val status: OrderStatus,
    val pickupDate: String? = null,
    val pickupLocation: String,
    val counterpartName: String,
    val counterpartPhone: String,
    val counterpartDistrict: String,
    val transactionDate: String,
    val isBuyer: Boolean // true if current user is buyer, false if seller
)

/**
 * Industrial transaction detail screen - full details of an order
 *
 * Header: 72dp Rice with back text and title
 * Sections: Item, Logistics, Counterpart, Status, Report Problem
 * Each section separated by 4dp Earth divider
 *
 * @param transaction Transaction detail data
 * @param language Current language for labels
 * @param onNavigateBack Callback to navigate back
 * @param onCallCounterpart Callback to call counterpart (or null to use default phone dialer)
 * @param onReportProblem Callback to report a problem
 * @param isLoading Loading state
 * @param isError Error state
 * @param onRetry Retry callback
 */
@Composable
fun IndustrialTransactionDetailScreen(
    transaction: TransactionDetail?,
    language: AppLanguage = AppLanguage.SINHALA,
    onNavigateBack: () -> Unit,
    onCallCounterpart: (() -> Unit)? = null,
    onReportProblem: () -> Unit = {},
    isLoading: Boolean = false,
    isError: Boolean = false,
    onRetry: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
    ) {
        // Header - 72dp
        TransactionDetailHeader(
            backText = when (language) {
                AppLanguage.SINHALA -> "ආපසු"
                AppLanguage.TAMIL -> "பின்"
                AppLanguage.ENGLISH -> "BACK"
            },
            title = when (language) {
                AppLanguage.SINHALA -> "ගනුදෙනුව"
                AppLanguage.TAMIL -> "ஆர்டர்"
                AppLanguage.ENGLISH -> "ORDER"
            },
            onBackClick = onNavigateBack
        )

        // Earth divider
        EarthDivider()

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
            isError || transaction == null -> {
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
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    // Item section
                    ItemSection(
                        transaction = transaction,
                        language = language
                    )

                    EarthDivider()

                    // Logistics section
                    LogisticsSection(
                        transaction = transaction,
                        language = language
                    )

                    EarthDivider()

                    // Counterpart section with Call button
                    CounterpartSection(
                        transaction = transaction,
                        language = language,
                        onCallCounterpart = onCallCounterpart ?: {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${transaction.counterpartPhone}")
                            }
                            context.startActivity(intent)
                        }
                    )

                    EarthDivider()

                    // Status section
                    StatusSection(
                        transaction = transaction,
                        language = language
                    )

                    EarthDivider()

                    // Report problem section (only for active transactions)
                    if (transaction.status == OrderStatus.ACTIVE) {
                        ReportProblemSection(
                            language = language,
                            onReportProblem = onReportProblem
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
private fun TransactionDetailHeader(
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
 * 4dp Earth divider
 */
@Composable
private fun EarthDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BorderWidth.Thick)
            .background(HumanIndustrial.Earth)
    )
}

/**
 * Item section - product, quantity, price
 */
@Composable
private fun ItemSection(
    transaction: TransactionDetail,
    language: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Section label
        Text(
            text = when (language) {
                AppLanguage.SINHALA -> "අයිතමය"
                AppLanguage.TAMIL -> "பொருள்"
                AppLanguage.ENGLISH -> "ITEM"
            },
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Stone
        )

        // Product name
        Text(
            text = transaction.productName,
            style = HumanIndustrialType.productName,
            color = HumanIndustrial.Ink
        )

        // Quantity
        Text(
            text = "${transaction.quantity.toInt()} ${transaction.unit}",
            style = HumanIndustrialType.body,
            color = HumanIndustrial.Ink
        )

        // Price per unit
        Text(
            text = "රු ${transaction.pricePerUnit.toInt()}/${transaction.unit}",
            style = HumanIndustrialType.body,
            color = HumanIndustrial.Stone
        )

        // Total amount
        Text(
            text = "රු ${transaction.totalAmount.toInt().toString().chunked(3).joinToString(",")}",
            style = HumanIndustrialType.price,
            color = HumanIndustrial.Gold
        )
    }
}

/**
 * Logistics section - pickup date and location
 */
@Composable
private fun LogisticsSection(
    transaction: TransactionDetail,
    language: AppLanguage
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Section label
        Text(
            text = when (language) {
                AppLanguage.SINHALA -> "ප්‍රවාහනය"
                AppLanguage.TAMIL -> "போக்குவரத்து"
                AppLanguage.ENGLISH -> "LOGISTICS"
            },
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Stone
        )

        // Pickup date
        transaction.pickupDate?.let { date ->
            LabelValuePair(
                label = when (language) {
                    AppLanguage.SINHALA -> "රැගෙන යන දිනය"
                    AppLanguage.TAMIL -> "எடுக்கும் தேதி"
                    AppLanguage.ENGLISH -> "PICKUP DATE"
                },
                value = date
            )
        }

        // Pickup location
        LabelValuePair(
            label = when (language) {
                AppLanguage.SINHALA -> "රැගෙන යන ස්ථානය"
                AppLanguage.TAMIL -> "எடுக்கும் இடம்"
                AppLanguage.ENGLISH -> "PICKUP LOCATION"
            },
            value = transaction.pickupLocation
        )
    }
}

/**
 * Counterpart section - buyer/seller info with call button
 */
@Composable
private fun CounterpartSection(
    transaction: TransactionDetail,
    language: AppLanguage,
    onCallCounterpart: () -> Unit
) {
    val phoneAvailable = transaction.counterpartPhone.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Section label - changes based on user role
        Text(
            text = if (transaction.isBuyer) {
                when (language) {
                    AppLanguage.SINHALA -> "විකුණන්නා"
                    AppLanguage.TAMIL -> "விற்பவர்"
                    AppLanguage.ENGLISH -> "SELLER"
                }
            } else {
                when (language) {
                    AppLanguage.SINHALA -> "ගැනුම්කරු"
                    AppLanguage.TAMIL -> "வாங்குபவர்"
                    AppLanguage.ENGLISH -> "BUYER"
                }
            },
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Stone
        )

        // Counterpart name
        Text(
            text = transaction.counterpartName.ifEmpty {
                when (language) {
                    AppLanguage.SINHALA -> "නම නොදනී"
                    AppLanguage.TAMIL -> "பெயர் தெரியாது"
                    AppLanguage.ENGLISH -> "NAME UNAVAILABLE"
                }
            },
            style = HumanIndustrialType.productName,
            color = HumanIndustrial.Ink
        )

        // District
        Text(
            text = transaction.counterpartDistrict,
            style = HumanIndustrialType.body,
            color = HumanIndustrial.Ink
        )

        // Call button - disabled if phone not available
        if (phoneAvailable) {
            PrimaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "ඇමතුමක් ගන්න"
                    AppLanguage.TAMIL -> "அழைக்கவும்"
                    AppLanguage.ENGLISH -> "CALL"
                },
                onClick = onCallCounterpart,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Show unavailable message when phone is missing
            SecondaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "දුරකතන අංකය නොමැත"
                    AppLanguage.TAMIL -> "தொலைபேசி இல்லை"
                    AppLanguage.ENGLISH -> "PHONE UNAVAILABLE"
                },
                onClick = { /* No action - informational only */ },
                enabled = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Status section - current order status
 */
@Composable
private fun StatusSection(
    transaction: TransactionDetail,
    language: AppLanguage
) {
    val statusText = when (transaction.status) {
        OrderStatus.ACTIVE -> when (language) {
            AppLanguage.SINHALA -> "සක්‍රීයයි"
            AppLanguage.TAMIL -> "செயலில்"
            AppLanguage.ENGLISH -> "ACTIVE"
        }
        OrderStatus.COMPLETED -> when (language) {
            AppLanguage.SINHALA -> "අවසන් විය"
            AppLanguage.TAMIL -> "முடிந்தது"
            AppLanguage.ENGLISH -> "COMPLETED"
        }
        OrderStatus.PROBLEM -> when (language) {
            AppLanguage.SINHALA -> "ගැටලුවක්"
            AppLanguage.TAMIL -> "சிக்கல்"
            AppLanguage.ENGLISH -> "PROBLEM"
        }
    }

    val statusColor = when (transaction.status) {
        OrderStatus.ACTIVE -> HumanIndustrial.Green
        OrderStatus.COMPLETED -> HumanIndustrial.Stone
        OrderStatus.PROBLEM -> HumanIndustrial.Urgent
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Section label
        Text(
            text = when (language) {
                AppLanguage.SINHALA -> "තත්ත්වය"
                AppLanguage.TAMIL -> "நிலை"
                AppLanguage.ENGLISH -> "STATUS"
            },
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Stone
        )

        // Status value
        Text(
            text = statusText,
            style = HumanIndustrialType.productName,
            color = statusColor
        )

        // Transaction date
        Text(
            text = transaction.transactionDate,
            style = HumanIndustrialType.body,
            color = HumanIndustrial.Stone
        )
    }
}

/**
 * Report problem section - urgent button
 */
@Composable
private fun ReportProblemSection(
    language: AppLanguage,
    onReportProblem: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Urgent button - Rice background, Urgent border and text
        UrgentButton(
            text = when (language) {
                AppLanguage.SINHALA -> "ගැටලුවක් වාර්තා කරන්න"
                AppLanguage.TAMIL -> "சிக்கலைப் புகாரளிக்கவும்"
                AppLanguage.ENGLISH -> "REPORT PROBLEM"
            },
            onClick = onReportProblem,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Urgent button - Rice background with Urgent border
 */
@Composable
private fun UrgentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(HumanIndustrial.Rice)
            .border(
                width = BorderWidth.Standard,
                color = HumanIndustrial.Urgent,
                shape = RectangleShape
            )
            .industrialClickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = HumanIndustrialType.button,
            color = HumanIndustrial.Urgent
        )
    }
}

/**
 * Label-value pair component
 */
@Composable
private fun LabelValuePair(
    label: String,
    value: String
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
    ) {
        Text(
            text = label,
            style = HumanIndustrialType.sectionLabel,
            color = HumanIndustrial.Stone
        )
        Text(
            text = value,
            style = HumanIndustrialType.productName,
            color = HumanIndustrial.Ink
        )
    }
}

// ============================================================================
// BACKWARD COMPATIBILITY - Keep old function signature with ViewModel
// ============================================================================

/**
 * Legacy function signature for backward compatibility with ViewModel
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

    val transaction = uiState.transaction
    val convertedTransaction = transaction?.let { tx ->
        val isBuyer = currentUser?.userType?.name != "FARMER"
        TransactionDetail(
            id = tx.id,
            productName = "", // Not available in Transaction model
            quantity = tx.quantity,
            unit = tx.unit,
            pricePerUnit = tx.pricePerUnit,
            totalAmount = tx.totalAmount,
            status = when (tx.status) {
                TransactionStatus.PENDING,
                TransactionStatus.CONFIRMED,
                TransactionStatus.IN_PROGRESS -> OrderStatus.ACTIVE
                TransactionStatus.COMPLETED -> OrderStatus.COMPLETED
                TransactionStatus.CANCELLED -> OrderStatus.PROBLEM
            },
            pickupDate = tx.pickupDate,
            pickupLocation = tx.pickupLocation,
            counterpartName = if (isBuyer) tx.sellerName else tx.buyerName,
            counterpartPhone = if (isBuyer) tx.sellerPhone else tx.buyerPhone,
            counterpartDistrict = tx.pickupLocation, // Using location as district
            transactionDate = tx.createdAt,
            isBuyer = isBuyer
        )
    }

    IndustrialTransactionDetailScreen(
        transaction = convertedTransaction,
        language = AppLanguage.ENGLISH,
        onNavigateBack = onNavigateBack,
        onCallCounterpart = {
            val contactId = if (currentUser?.userType?.name == "FARMER") {
                transaction?.buyerId ?: ""
            } else {
                transaction?.farmerId ?: ""
            }
            onContactUser(contactId)
        },
        isLoading = uiState.isLoading,
        isError = uiState.error != null,
        onRetry = { viewModel.loadTransaction(transactionId) }
    )
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TransactionDetailPreview() {
    val sampleTransaction = TransactionDetail(
        id = "1",
        productName = "තක්කාලි",
        quantity = 10.0,
        unit = "kg",
        pricePerUnit = 280.0,
        totalAmount = 2800.0,
        status = OrderStatus.ACTIVE,
        pickupDate = "ජනවාරි 15",
        pickupLocation = "නුවර පොළ අසල",
        counterpartName = "සුනිල් මහතා",
        counterpartPhone = "+94771234567",
        counterpartDistrict = "නුවර",
        transactionDate = "ජනවාරි 12",
        isBuyer = true
    )

    IndustrialTransactionDetailScreen(
        transaction = sampleTransaction,
        language = AppLanguage.SINHALA,
        onNavigateBack = {},
        onReportProblem = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TransactionDetailEnglishPreview() {
    val sampleTransaction = TransactionDetail(
        id = "1",
        productName = "Tomatoes",
        quantity = 10.0,
        unit = "kg",
        pricePerUnit = 280.0,
        totalAmount = 2800.0,
        status = OrderStatus.ACTIVE,
        pickupDate = "January 15",
        pickupLocation = "Near Kandy Market",
        counterpartName = "Mr. Sunil",
        counterpartPhone = "+94771234567",
        counterpartDistrict = "Kandy",
        transactionDate = "January 12",
        isBuyer = true
    )

    IndustrialTransactionDetailScreen(
        transaction = sampleTransaction,
        language = AppLanguage.ENGLISH,
        onNavigateBack = {},
        onReportProblem = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TransactionDetailCompletedPreview() {
    val sampleTransaction = TransactionDetail(
        id = "2",
        productName = "බෝංචි",
        quantity = 15.0,
        unit = "kg",
        pricePerUnit = 320.0,
        totalAmount = 4800.0,
        status = OrderStatus.COMPLETED,
        pickupDate = "ජනවාරි 10",
        pickupLocation = "කොළඹ පොළ",
        counterpartName = "කමල් මහතා",
        counterpartPhone = "+94779876543",
        counterpartDistrict = "කොළඹ",
        transactionDate = "ජනවාරි 8",
        isBuyer = false
    )

    IndustrialTransactionDetailScreen(
        transaction = sampleTransaction,
        language = AppLanguage.SINHALA,
        onNavigateBack = {},
        onReportProblem = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun TransactionDetailLoadingPreview() {
    IndustrialTransactionDetailScreen(
        transaction = null,
        language = AppLanguage.ENGLISH,
        onNavigateBack = {},
        isLoading = true
    )
}
