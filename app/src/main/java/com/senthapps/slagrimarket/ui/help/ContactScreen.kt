package com.senthapps.slagrimarket.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactScreen(
    onNavigateBack: () -> Unit,
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Contact Us"
                            "ta" -> "எங்களை தொடர்பு கொள்ளுங்கள்"
                            "si" -> "අප හා සම්බන්ධ වන්න"
                            else -> "Contact Us"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = when (currentLanguage) {
                                "en" -> "Go back"
                                "ta" -> "பின் செல்"
                                "si" -> "ආපසු යන්න"
                                else -> "Go back"
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Get in Touch"
                                "ta" -> "தொடர்பில் இருங்கள்"
                                "si" -> "සම්බන්ධ වන්න"
                                else -> "Get in Touch"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "We're here to help! Reach out to us through any of these channels."
                                "ta" -> "நாங்கள் உதவ இங்கே இருக்கிறோம்! இந்த சேனல்கள் மூலம் எங்களை தொடர்பு கொள்ளுங்கள்."
                                "si" -> "අපි උදව් කිරීමට මෙහි සිටිමු! මෙම නාලිකා හරහා අප වෙත සම්බන්ධ වන්න."
                                else -> "We're here to help! Reach out to us through any of these channels."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                ContactItem(
                    icon = Icons.Default.Email,
                    title = when (currentLanguage) {
                        "en" -> "Email"
                        "ta" -> "மின்னஞ்சல்"
                        "si" -> "විද්‍යුත් තැපෑල"
                        else -> "Email"
                    },
                    value = "support@agrimarket.lk",
                    description = when (currentLanguage) {
                        "en" -> "Send us an email anytime"
                        "ta" -> "எந்த நேரத்திலும் மின்னஞ்சல் அனுப்புங்கள்"
                        "si" -> "ඕනෑම වේලාවක විද්‍යුත් තැපෑලක් එවන්න"
                        else -> "Send us an email anytime"
                    }
                )
            }

            item {
                ContactItem(
                    icon = Icons.Default.Phone,
                    title = when (currentLanguage) {
                        "en" -> "Phone"
                        "ta" -> "தொலைபேசி"
                        "si" -> "දුරකථනය"
                        else -> "Phone"
                    },
                    value = "+94 11 234 5678",
                    description = when (currentLanguage) {
                        "en" -> "Monday to Friday, 9 AM - 5 PM"
                        "ta" -> "திங்கள் முதல் வெள்ளி, காலை 9 - மாலை 5"
                        "si" -> "සඳුදා සිට සිකුරාදා, පෙ.ව. 9 - ප.ව. 5"
                        else -> "Monday to Friday, 9 AM - 5 PM"
                    }
                )
            }

            item {
                ContactItem(
                    icon = Icons.Default.LocationOn,
                    title = when (currentLanguage) {
                        "en" -> "Address"
                        "ta" -> "முகவரி"
                        "si" -> "ලිපිනය"
                        else -> "Address"
                    },
                    value = when (currentLanguage) {
                        "en" -> "Colombo, Sri Lanka"
                        "ta" -> "கொழும்பு, இலங்கை"
                        "si" -> "කොළඹ, ශ්‍රී ලංකාව"
                        else -> "Colombo, Sri Lanka"
                    },
                    description = when (currentLanguage) {
                        "en" -> "Visit our office"
                        "ta" -> "எங்கள் அலுவலகத்திற்கு வாருங்கள்"
                        "si" -> "අපගේ කාර්යාලයට පැමිණෙන්න"
                        else -> "Visit our office"
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = when (currentLanguage) {
                        "en" -> "We typically respond within 24 hours."
                        "ta" -> "நாங்கள் பொதுவாக 24 மணி நேரத்திற்குள் பதிலளிப்போம்."
                        "si" -> "අපි සාමාන්‍යයෙන් පැය 24ක් ඇතුළත ප්‍රතිචාර දක්වන්නෙමු."
                        else -> "We typically respond within 24 hours."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ContactItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
