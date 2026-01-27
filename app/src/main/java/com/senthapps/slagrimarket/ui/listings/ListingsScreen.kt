package com.senthapps.slagrimarket.ui.listings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.Units
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.ui.components.EnhancedListingCard
import com.senthapps.slagrimarket.ui.components.EmptyListingsState
import com.senthapps.slagrimarket.ui.components.ErrorState
import com.senthapps.slagrimarket.ui.components.ListingCardSkeleton
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.util.TranslationUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onListingClick: (String) -> Unit = {},
    viewModel: ListingsViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Listings"
                                "ta" -> "பட்டியல்கள்"
                                "si" -> "ලැයිස්තු"
                                else -> "Listings"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (currentLanguage !in listOf("en", "ta", "si")) {
                            Text(
                                text = "Listings",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        val errorMessage = uiState.error
        when {
            // Error state
            errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    ErrorState(
                        errorMessage = errorMessage,
                        currentLanguage = currentLanguage,
                        onRetry = viewModel::refreshListings
                    )
                }
            }

            // Loading state with shimmer skeletons
            uiState.isLoading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(Spacing.Large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    items(5) {
                        ListingCardSkeleton()
                    }
                }
            }

            // Empty state
            uiState.listings.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyListingsState(
                        currentLanguage = currentLanguage,
                        onCreateListing = null // Can be connected to create listing action
                    )
                }
            }

            // Content state
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(Spacing.Large),
                    verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    items(uiState.listings) { listing ->
                        EnhancedListingCard(
                            listing = listing,
                            onClick = { onListingClick(listing.id) },
                            currentLanguage = currentLanguage,
                            showPriceTrend = false // Can be enabled when market prices are available
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ListingCard(
    listing: Listing,
    onClick: () -> Unit,
    currentLanguage: String = "en"
) {
    // Get translated values
    val cropName = CropTypes.getCropName(listing.cropType, currentLanguage)
    val locationName = TranslationUtil.getLocationName(listing.location, currentLanguage)
    val unitName = Units.getUnitName(listing.unit, currentLanguage)
    val qualityGrade = listing.quality.getDisplayString(currentLanguage)

    // Get labels in the selected language
    val quantityLabel = when (currentLanguage) {
        "en" -> "Quantity"
        "ta" -> "அளவு"
        "si" -> "ප්‍රමාණය"
        else -> "Quantity"
    }

    val gradeLabel = when (currentLanguage) {
        "en" -> "Grade"
        "ta" -> "தரம்"
        "si" -> "ශ්‍රේණිය"
        else -> "Grade"
    }

    val perLabel = when (currentLanguage) {
        "en" -> "per"
        "ta" -> "ஒரு"
        "si" -> "එක්"
        else -> "per"
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cropName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = locationName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LKR ${listing.pricePerUnit}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "$perLabel $unitName",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$quantityLabel: ${listing.quantity} $unitName",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$gradeLabel $qualityGrade",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
