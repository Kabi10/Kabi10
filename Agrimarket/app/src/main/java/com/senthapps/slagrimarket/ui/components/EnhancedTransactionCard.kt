package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.PaymentStatus
import com.senthapps.slagrimarket.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Enhanced Transaction Card with Material Design 3 styling
 * Features status indicators, visual hierarchy, and trilingual support
 * Includes accessibility support for TalkBack
 */
@Composable
fun EnhancedTransactionCard(
    transaction: Transaction,
    currentLanguage: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onActionClick: (() -> Unit)? = null,
    actionText: String? = null
) {
    // Build accessibility description for TalkBack
    val statusLabel = transaction.status.getDisplayString(currentLanguage)
    val paymentLabel = transaction.paymentStatus.getDisplayString(currentLanguage)
    val cardDescription = when (currentLanguage) {
        "ta" -> "பரிவர்த்தனை ${transaction.id.take(8)}, ${transaction.quantity} ${transaction.unit}, LKR ${String.format("%.0f", transaction.totalAmount)}, நிலை: $statusLabel, கட்டணம்: $paymentLabel"
        "si" -> "ගනුදෙනුව ${transaction.id.take(8)}, ${transaction.quantity} ${transaction.unit}, LKR ${String.format("%.0f", transaction.totalAmount)}, තත්ත්වය: $statusLabel, ගෙවීම: $paymentLabel"
        else -> "Transaction ${transaction.id.take(8)}, ${transaction.quantity} ${transaction.unit}, LKR ${String.format("%.0f", transaction.totalAmount)}, Status: $statusLabel, Payment: $paymentLabel"
    }

    AppCard(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardDescription },
        onClick = onClick,
        elevation = 2.dp
    ) {
        // Status indicator strip at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(topStart = CornerRadius.Large, topEnd = CornerRadius.Large))
                .background(transaction.status.getColor())
        )
        
        Column(
            modifier = Modifier.padding(top = Spacing.Small),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            // Header row with transaction ID and status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = getTransactionLabel(currentLanguage, transaction.id),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatTransactionDate(transaction.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                EnhancedStatusChip(
                    status = transaction.status,
                    currentLanguage = currentLanguage
                )
            }
            
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            
            // Amount and quantity row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity info
                InfoColumn(
                    label = getQuantityLabel(currentLanguage),
                    value = "${transaction.quantity} ${transaction.unit}",
                    icon = Icons.Default.ShoppingCart
                )
                
                // Amount info with emphasis
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = getTotalLabel(currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "LKR ${String.format("%,.2f", transaction.totalAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Green600
                    )
                }
            }
            
            // Pickup info row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CornerRadius.Small))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(Spacing.Medium),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = getPickupLocation(transaction, currentLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatPickupDate(transaction.pickupDate, currentLanguage),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Days until pickup badge
                val daysUntil = transaction.getDaysUntilPickup()
                if (daysUntil >= 0 && transaction.status != TransactionStatus.COMPLETED && 
                    transaction.status != TransactionStatus.CANCELLED) {
                    DaysUntilBadge(days = daysUntil, currentLanguage = currentLanguage)
                }
            }
            
            // Payment status indicator
            PaymentStatusRow(
                paymentStatus = transaction.paymentStatus,
                paymentMethod = transaction.paymentMethod.getDisplayString(currentLanguage),
                currentLanguage = currentLanguage
            )
            
            // Action button if provided
            if (actionText != null && onActionClick != null) {
                PrimaryButton(
                    text = actionText,
                    onClick = onActionClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun InfoColumn(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(16.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun EnhancedStatusChip(
    status: TransactionStatus,
    currentLanguage: String
) {
    Surface(
        shape = RoundedCornerShape(CornerRadius.Full),
        color = status.getColor().copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.Medium, vertical = Spacing.ExtraSmall),
            horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(status.getColor())
            )
            Text(
                text = status.getDisplayString(currentLanguage),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = status.getColor()
            )
        }
    }
}

@Composable
private fun DaysUntilBadge(
    days: Int,
    currentLanguage: String
) {
    val backgroundColor = when {
        days == 0 -> Error600
        days <= 2 -> Warning500
        else -> Blue400
    }

    Surface(
        shape = RoundedCornerShape(CornerRadius.Small),
        color = backgroundColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = when {
                days == 0 -> when (currentLanguage) {
                    "ta" -> "இன்று"
                    "si" -> "අද"
                    else -> "Today"
                }
                days == 1 -> when (currentLanguage) {
                    "ta" -> "நாளை"
                    "si" -> "හෙට"
                    else -> "Tomorrow"
                }
                else -> when (currentLanguage) {
                    "ta" -> "$days நாட்கள்"
                    "si" -> "$days දින"
                    else -> "$days days"
                }
            },
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = backgroundColor,
            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = Spacing.ExtraSmall)
        )
    }
}

@Composable
private fun PaymentStatusRow(
    paymentStatus: PaymentStatus,
    paymentMethod: String,
    currentLanguage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (paymentStatus) {
                    PaymentStatus.PAID -> Icons.Default.Check
                    PaymentStatus.PENDING -> Icons.Default.Info
                    PaymentStatus.FAILED -> Icons.Default.Close
                    PaymentStatus.REFUNDED -> Icons.Default.Refresh
                },
                contentDescription = null,
                tint = when (paymentStatus) {
                    PaymentStatus.PAID -> Success600
                    PaymentStatus.PENDING -> Warning500
                    PaymentStatus.FAILED -> Error600
                    PaymentStatus.REFUNDED -> Blue400
                },
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = paymentStatus.getDisplayString(currentLanguage),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = paymentMethod,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Helper functions for trilingual labels
private fun getTransactionLabel(language: String, id: String): String = when (language) {
    "ta" -> "பரிவர்த்தனை #${id.take(8)}"
    "si" -> "ගනුදෙනුව #${id.take(8)}"
    else -> "Transaction #${id.take(8)}"
}

private fun getQuantityLabel(language: String): String = when (language) {
    "ta" -> "அளவு"
    "si" -> "ප්‍රමාණය"
    else -> "Quantity"
}

private fun getTotalLabel(language: String): String = when (language) {
    "ta" -> "மொத்தம்"
    "si" -> "එකතුව"
    else -> "Total"
}

private fun getPickupLocation(transaction: Transaction, language: String): String = when (language) {
    "ta" -> transaction.pickupLocationTamil.ifEmpty { transaction.pickupLocation }
    "si" -> transaction.pickupLocationSinhala.ifEmpty { transaction.pickupLocation }
    else -> transaction.pickupLocation
}

private fun formatTransactionDate(dateString: String): String {
    return try {
        val instant = java.time.Instant.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        instant.atZone(java.time.ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        dateString.take(10)
    }
}

private fun formatPickupDate(dateString: String, language: String): String {
    return try {
        val date = LocalDate.parse(dateString)
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM dd")
        val formattedDate = date.format(formatter)
        when (language) {
            "ta" -> "எடுப்பு: $formattedDate"
            "si" -> "ලබා ගැනීම: $formattedDate"
            else -> "Pickup: $formattedDate"
        }
    } catch (e: Exception) {
        dateString
    }
}

