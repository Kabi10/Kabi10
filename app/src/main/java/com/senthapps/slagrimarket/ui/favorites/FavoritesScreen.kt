package com.senthapps.slagrimarket.ui.favorites

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
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onNavigateToDetail: (String) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "My Favorites"
                            "ta" -> "எனது விருப்பங்கள்"
                            "si" -> "මගේ ප්‍රියතම"
                            else -> "My Favorites"
                        },
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    if (uiState.favorites.isNotEmpty()) {
                        TextButton(onClick = { viewModel.clearAllFavorites() }) {
                            Text(
                                when (currentLanguage) {
                                    "en" -> "Clear All"
                                    "ta" -> "அனைத்தையும் அழி"
                                    "si" -> "සියල්ල ඉවත් කරන්න"
                                    else -> "Clear All"
                                }
                            )
                        }
                    }
                }
            )
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
                    onRetry = { viewModel.loadFavorites() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    currentLanguage = currentLanguage
                )
            }
            uiState.favorites.isEmpty() -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    currentLanguage = currentLanguage
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.favorites, key = { it.id }) { listing ->
                        FavoriteListingCard(
                            listing = listing,
                            onClick = { onNavigateToDetail(listing.id) },
                            onRemoveFavorite = { viewModel.removeFavorite(listing.id) },
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    currentLanguage: String
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (currentLanguage) {
                "en" -> "No Favorites Yet"
                "ta" -> "இன்னும் விருப்பங்கள் இல்லை"
                "si" -> "තවම ප්‍රියතම නැත"
                else -> "No Favorites Yet"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (currentLanguage) {
                "en" -> "Start adding listings to your favorites to see them here"
                "ta" -> "உங்கள் விருப்பங்களில் பட்டியல்களைச் சேர்க்கத் தொடங்குங்கள்"
                "si" -> "ඔබේ ප්‍රියතම වලට ලැයිස්තු එකතු කිරීම ආරම්භ කරන්න"
                else -> "Start adding listings to your favorites to see them here"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FavoriteListingCard(
    listing: Listing,
    onClick: () -> Unit,
    onRemoveFavorite: () -> Unit,
    currentLanguage: String
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = when (currentLanguage) {
                        "ta" -> listing.cropNameTamil
                        "si" -> listing.cropNameSinhala
                        else -> listing.cropNameEnglish
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Rs. ${listing.pricePerUnit}/${listing.unit}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "${listing.quantity} ${listing.unit} ${
                        when (currentLanguage) {
                            "en" -> "available"
                            "ta" -> "கிடைக்கிறது"
                            "si" -> "තිබේ"
                            else -> "available"
                        }
                    }",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onRemoveFavorite) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    currentLanguage: String
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
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(
                when (currentLanguage) {
                    "en" -> "Retry"
                    "ta" -> "மீண்டும் முயற்சிக்கவும்"
                    "si" -> "නැවත උත්සාහ කරන්න"
                    else -> "Retry"
                }
            )
        }
    }
}
