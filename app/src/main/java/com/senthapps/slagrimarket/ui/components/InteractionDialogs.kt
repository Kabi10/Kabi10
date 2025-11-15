package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.getCropName
import com.senthapps.slagrimarket.ui.theme.Green600
import com.senthapps.slagrimarket.ui.theme.Blue400
import java.util.Locale

/**
 * Contact Farmer Dialog
 * Modal dialog for contacting a farmer about a listing
 */
@Composable
fun ContactFarmerDialog(
    listing: Listing,
    farmerName: String,
    onDismiss: () -> Unit,
    onSendMessage: (String) -> Unit,
    currentLanguage: String = "en"
) {
    var message by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Contact Farmer"
                            "ta" -> "விவசாயியைத் தொடர்பு கொள்ளுங்கள்"
                            "si" -> "ගොවියා සම්බන්ධ කරන්න"
                            else -> "Contact Farmer"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Divider()

                // Farmer info
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column {
                        Text(
                            text = farmerName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = listing.getCropName(currentLanguage),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Message input
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Type your message here..."
                                "ta" -> "உங்கள் செய்தியை இங்கே தட்டச்சு செய்யவும்..."
                                "si" -> "ඔබේ පණිවිඩය මෙහි ටයිප් කරන්න..."
                                else -> "Type your message here..."
                            }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 4
                )

                // Quick message templates
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Quick messages:"
                        "ta" -> "விரைவு செய்திகள்:"
                        "si" -> "ඉක්මන් පණිවිඩ:"
                        else -> "Quick messages:"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val quickMessages = when (currentLanguage) {
                        "en" -> listOf("Interested", "Price?", "Available?")
                        "ta" -> listOf("ஆர்வம்", "விலை?", "கிடைக்குமா?")
                        "si" -> listOf("උනන්දුයි", "මිල?", "තිබේද?")
                        else -> listOf("Interested", "Price?", "Available?")
                    }

                    quickMessages.forEach { quickMsg ->
                        AssistChip(
                            onClick = { message = quickMsg },
                            label = { Text(quickMsg) }
                        )
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Cancel"
                                "ta" -> "ரத்து செய்"
                                "si" -> "අවලංගු කරන්න"
                                else -> "Cancel"
                            }
                        )
                    }
                    Button(
                        onClick = {
                            if (message.isNotBlank()) {
                                onSendMessage(message)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = message.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Send"
                                "ta" -> "அனுப்பு"
                                "si" -> "යවන්න"
                                else -> "Send"
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Make Offer Dialog
 * Modal dialog for making a price offer on a listing
 */
@Composable
fun MakeOfferDialog(
    listing: Listing,
    onDismiss: () -> Unit,
    onSubmitOffer: (Double, Double) -> Unit,
    currentLanguage: String = "en"
) {
    var offerPrice by remember { mutableStateOf("") }
    var offerQuantity by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Make an Offer"
                            "ta" -> "விலை முன்மொழிவு"
                            "si" -> "මිල යෝජනාවක් කරන්න"
                            else -> "Make an Offer"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }

                Divider()

                // Listing info
                Text(
                    text = listing.getCropName(currentLanguage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Listed price: LKR ${listing.pricePerUnit} per ${listing.unit}"
                        "ta" -> "பட்டியல் விலை: LKR ${listing.pricePerUnit} ஒரு ${listing.unit}"
                        "si" -> "ලැයිස්තුගත මිල: LKR ${listing.pricePerUnit} එක් ${listing.unit}"
                        else -> "Listed price: LKR ${listing.pricePerUnit} per ${listing.unit}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Offer price input
                OutlinedTextField(
                    value = offerPrice,
                    onValueChange = { offerPrice = it.filter { char -> char.isDigit() || char == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Your offer price (LKR per ${listing.unit})"
                                "ta" -> "உங்கள் விலை (LKR ஒரு ${listing.unit})"
                                "si" -> "ඔබේ මිල (LKR එක් ${listing.unit})"
                                else -> "Your offer price"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ShoppingCart,
                            contentDescription = null
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Quantity input
                OutlinedTextField(
                    value = offerQuantity,
                    onValueChange = { offerQuantity = it.filter { char -> char.isDigit() || char == '.' } },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Quantity (${listing.unit})"
                                "ta" -> "அளவு (${listing.unit})"
                                "si" -> "ප්‍රමාණය (${listing.unit})"
                                else -> "Quantity"
                            }
                        )
                    },
                    supportingText = {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Available: ${listing.quantity} ${listing.unit}"
                                "ta" -> "கிடைக்கும்: ${listing.quantity} ${listing.unit}"
                                "si" -> "තිබේ: ${listing.quantity} ${listing.unit}"
                                else -> "Available: ${listing.quantity}"
                            }
                        )
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Total calculation
                val totalAmount = (offerPrice.toDoubleOrNull() ?: 0.0) * (offerQuantity.toDoubleOrNull() ?: 0.0)
                if (totalAmount > 0) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    "en" -> "Total:"
                                    "ta" -> "மொத்தம்:"
                                    "si" -> "එකතුව:"
                                    else -> "Total:"
                                },
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "LKR ${String.format(Locale.US, "%.2f", totalAmount)}",
                                fontWeight = FontWeight.Bold,
                                color = Green600
                            )
                        }
                    }
                }

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Cancel"
                                "ta" -> "ரத்து செய்"
                                "si" -> "අවලංගු කරන්න"
                                else -> "Cancel"
                            }
                        )
                    }
                    Button(
                        onClick = {
                            val price = offerPrice.toDoubleOrNull()
                            val quantity = offerQuantity.toDoubleOrNull()
                            if (price != null && quantity != null && price > 0 && quantity > 0 && quantity <= listing.quantity) {
                                onSubmitOffer(price, quantity)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = offerPrice.toDoubleOrNull() != null &&
                                 offerQuantity.toDoubleOrNull() != null &&
                                 (offerPrice.toDoubleOrNull() ?: 0.0) > 0 &&
                                 (offerQuantity.toDoubleOrNull() ?: 0.0) > 0 &&
                                 (offerQuantity.toDoubleOrNull() ?: 0.0) <= listing.quantity
                    ) {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Submit Offer"
                                "ta" -> "முன்மொழிவை சமர்ப்பிக்கவும்"
                                "si" -> "යෝජනාව ඉදිරිපත් කරන්න"
                                else -> "Submit Offer"
                            }
                        )
                    }
                }
            }
        }
    }
}

