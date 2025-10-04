package com.senthapps.slagrimarket.ui.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
fun HelpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToFAQ: () -> Unit,
    onNavigateToContact: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Help & Support"
                            "ta" -> "உதவி & ஆதரவு"
                            "si" -> "උදව් සහ සහාය"
                            else -> "Help & Support"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "How can we help you?"
                                "ta" -> "நாங்கள் உங்களுக்கு எவ்வாறு உதவ முடியும்?"
                                "si" -> "අපට ඔබට උදව් කළ හැක්කේ කෙසේද?"
                                else -> "How can we help you?"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Find answers to common questions or contact our support team"
                                "ta" -> "பொதுவான கேள்விகளுக்கான பதில்களைக் கண்டறியவும் அல்லது எங்கள் ஆதரவு குழுவைத் தொடர்பு கொள்ளவும்"
                                "si" -> "පොදු ප්‍රශ්නවලට පිළිතුරු සොයන්න හෝ අපගේ සහාය කණ්ඩායම අමතන්න"
                                else -> "Find answers to common questions or contact our support team"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            item {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Quick Links"
                        "ta" -> "விரைவு இணைப்புகள்"
                        "si" -> "ඉක්මන් සබැඳි"
                        else -> "Quick Links"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                HelpOptionCard(
                    icon = Icons.Default.Info,
                    title = when (currentLanguage) {
                        "en" -> "Frequently Asked Questions"
                        "ta" -> "அடிக்கடி கேட்கப்படும் கேள்விகள்"
                        "si" -> "නිතර අසන ප්‍රශ්න"
                        else -> "Frequently Asked Questions"
                    },
                    description = when (currentLanguage) {
                        "en" -> "Find answers to common questions"
                        "ta" -> "பொதுவான கேள்விகளுக்கான பதில்களைக் கண்டறியவும்"
                        "si" -> "පොදු ප්‍රශ්නවලට පිළිතුරු සොයන්න"
                        else -> "Find answers to common questions"
                    },
                    onClick = onNavigateToFAQ
                )
            }

            item {
                HelpOptionCard(
                    icon = Icons.Default.Email,
                    title = when (currentLanguage) {
                        "en" -> "Contact Support"
                        "ta" -> "ஆதரவைத் தொடர்பு கொள்ளவும்"
                        "si" -> "සහාය අමතන්න"
                        else -> "Contact Support"
                    },
                    description = when (currentLanguage) {
                        "en" -> "Get help from our support team"
                        "ta" -> "எங்கள் ஆதரவு குழுவிடமிருந்து உதவி பெறுங்கள்"
                        "si" -> "අපගේ සහාය කණ්ඩායමෙන් උදව් ලබා ගන්න"
                        else -> "Get help from our support team"
                    },
                    onClick = onNavigateToContact
                )
            }

            item {
                HelpOptionCard(
                    icon = Icons.Default.Info,
                    title = when (currentLanguage) {
                        "en" -> "Terms & Conditions"
                        "ta" -> "விதிமுறைகள் & நிபந்தனைகள்"
                        "si" -> "නියම සහ කොන්දේසි"
                        else -> "Terms & Conditions"
                    },
                    description = when (currentLanguage) {
                        "en" -> "Read our terms of service"
                        "ta" -> "எங்கள் சேவை விதிமுறைகளைப் படியுங்கள்"
                        "si" -> "අපගේ සේවා කොන්දේසි කියවන්න"
                        else -> "Read our terms of service"
                    },
                    onClick = onNavigateToTerms
                )
            }

            item {
                HelpOptionCard(
                    icon = Icons.Default.Lock,
                    title = when (currentLanguage) {
                        "en" -> "Privacy Policy"
                        "ta" -> "தனியுரிமைக் கொள்கை"
                        "si" -> "රහස්‍යතා ප්‍රතිපත්තිය"
                        else -> "Privacy Policy"
                    },
                    description = when (currentLanguage) {
                        "en" -> "Learn how we protect your data"
                        "ta" -> "உங்கள் தரவை நாங்கள் எவ்வாறு பாதுகாக்கிறோம் என்பதை அறியவும்"
                        "si" -> "අපි ඔබේ දත්ත ආරක්ෂා කරන ආකාරය ඉගෙන ගන්න"
                        else -> "Learn how we protect your data"
                    },
                    onClick = onNavigateToPrivacy
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (currentLanguage) {
                        "en" -> "App Information"
                        "ta" -> "பயன்பாட்டு தகவல்"
                        "si" -> "යෙදුම් තොරතුරු"
                        else -> "App Information"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        InfoRow(
                            label = when (currentLanguage) {
                                "en" -> "Version"
                                "ta" -> "பதிப்பு"
                                "si" -> "අනුවාදය"
                                else -> "Version"
                            },
                            value = "1.0.0"
                        )
                        Divider()
                        InfoRow(
                            label = when (currentLanguage) {
                                "en" -> "Email"
                                "ta" -> "மின்னஞ்சல்"
                                "si" -> "විද්‍යුත් තැපෑල"
                                else -> "Email"
                            },
                            value = "support@jaffnamarket.lk"
                        )
                        Divider()
                        InfoRow(
                            label = when (currentLanguage) {
                                "en" -> "Phone"
                                "ta" -> "தொலைபேசி"
                                "si" -> "දුරකථනය"
                                else -> "Phone"
                            },
                            value = "+94 21 222 3333"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HelpOptionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
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
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoRow(
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
