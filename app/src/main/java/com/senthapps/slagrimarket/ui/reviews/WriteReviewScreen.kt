package com.senthapps.slagrimarket.ui.reviews

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    transactionId: String,
    revieweeId: String,
    revieweeName: String,
    onNavigateBack: () -> Unit,
    viewModel: WriteReviewViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Write Review"
                            "ta" -> "மதிப்பாய்வு எழுது"
                            "si" -> "සමාලෝචනය ලියන්න"
                            else -> "Write Review"
                        },
                        style = MaterialTheme.typography.titleLarge,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Reviewee info
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = MaterialTheme.shapes.medium,
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
                            text = when (currentLanguage) {
                                "en" -> "Rate your experience with"
                                "ta" -> "உங்கள் அனுபவத்தை மதிப்பிடுங்கள்"
                                "si" -> "ඔබේ අත්දැකීම ශ්‍රේණිගත කරන්න"
                                else -> "Rate your experience with"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = revieweeName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Rating stars
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Your Rating"
                        "ta" -> "உங்கள் மதிப்பீடு"
                        "si" -> "ඔබේ ශ්‍රේණිගත කිරීම"
                        else -> "Your Rating"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { viewModel.updateRating(star) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star stars",
                                tint = if (star <= uiState.rating) {
                                    Color(0xFFFFC107)
                                } else {
                                    Color(0xFFE0E0E0)
                                },
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                }

                if (uiState.rating > 0) {
                    Text(
                        text = getRatingText(uiState.rating, currentLanguage),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Comment
            OutlinedTextField(
                value = uiState.comment,
                onValueChange = viewModel::updateComment,
                label = {
                    Text(when (currentLanguage) {
                        "en" -> "Your Review (Optional)"
                        "ta" -> "உங்கள் மதிப்பாய்வு (விருப்பம்)"
                        "si" -> "ඔබේ සමාලෝචනය (විකල්ප)"
                        else -> "Your Review (Optional)"
                    })
                },
                placeholder = {
                    Text(when (currentLanguage) {
                        "en" -> "Share your experience..."
                        "ta" -> "உங்கள் அனுபவத்தைப் பகிரவும்..."
                        "si" -> "ඔබේ අත්දැකීම් බෙදා ගන්න..."
                        else -> "Share your experience..."
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 5
            )

            // Error message
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Submit button
            Button(
                onClick = {
                    viewModel.submitReview(transactionId, revieweeId, revieweeName)
                },
                enabled = !uiState.isLoading && uiState.rating > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(when (currentLanguage) {
                        "en" -> "Submit Review"
                        "ta" -> "மதிப்பாய்வை சமர்ப்பிக்கவும்"
                        "si" -> "සමාලෝචනය ඉදිරිපත් කරන්න"
                        else -> "Submit Review"
                    })
                }
            }
        }
    }
}

private fun getRatingText(rating: Int, language: String): String {
    return when (rating) {
        5 -> when (language) {
            "ta" -> "சிறந்தது!"
            "si" -> "විශිෂ්ටයි!"
            else -> "Excellent!"
        }
        4 -> when (language) {
            "ta" -> "நல்லது"
            "si" -> "හොඳයි"
            else -> "Good"
        }
        3 -> when (language) {
            "ta" -> "சராசரி"
            "si" -> "සාමාන්‍යයි"
            else -> "Average"
        }
        2 -> when (language) {
            "ta" -> "மோசமானது"
            "si" -> "දුර්වලයි"
            else -> "Poor"
        }
        1 -> when (language) {
            "ta" -> "மிகவும் மோசம்"
            "si" -> "ඉතා දුර්වලයි"
            else -> "Very Poor"
        }
        else -> ""
    }
}
