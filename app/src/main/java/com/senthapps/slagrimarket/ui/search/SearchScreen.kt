package com.senthapps.slagrimarket.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.PickupLocations
import com.senthapps.slagrimarket.util.TranslationUtil
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.ui.components.ErrorBanner

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onListingClick: (String) -> Unit,
    onNavigateToAdvancedSearch: () -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    var searchQuery by remember { mutableStateOf("") }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            viewModel.updateSearchQuery(it)
                        },
                        placeholder = {
                            Text(when (currentLanguage) {
                                "en" -> "Search crops, locations..."
                                "ta" -> "பயிர்கள், இடங்களைத் தேடுங்கள்..."
                                "si" -> "බෝග, ස්ථාන සොයන්න..."
                                else -> "Search crops, locations..."
                            })
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = when (currentLanguage) {
                                "en" -> "Search"
                                "ta" -> "தேடு"
                                "si" -> "සොයන්න"
                                else -> "Search"
                            })
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    viewModel.clearSearch()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = when (currentLanguage) {
                                        "en" -> "Clear"
                                        "ta" -> "அழி"
                                        "si" -> "මකන්න"
                                        else -> "Clear"
                                    })
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = {
                                keyboardController?.hide()
                                viewModel.performSearch()
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = when (currentLanguage) {
                                "en" -> "Back"
                                "ta" -> "பின்செல்"
                                "si" -> "ආපසු"
                                else -> "Back"
                            }
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToAdvancedSearch) {
                        Icon(Icons.Default.Search, "Advanced Search")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters section
            FiltersSection(
                selectedCropType = uiState.selectedCropType,
                selectedLocation = uiState.selectedLocation,
                onCropTypeSelected = viewModel::selectCropType,
                onLocationSelected = viewModel::selectLocation,
                onClearFilters = viewModel::clearFilters,
                currentLanguage = currentLanguage
            )

            Divider()

            // Error banner
            val errorMessage = uiState.error
            if (errorMessage != null) {
                ErrorBanner(
                    errorMessage = errorMessage,
                    onRetry = viewModel::performSearch,
                    onDismiss = viewModel::clearError,
                    modifier = Modifier.padding(16.dp)
                )
            }

            // Results section
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                EmptySearchResults(searchQuery = searchQuery, currentLanguage = currentLanguage)
            } else if (uiState.searchResults.isEmpty()) {
                SearchSuggestions(
                    onCropTypeClick = { cropType ->
                        viewModel.selectCropType(cropType)
                        viewModel.performSearch()
                    },
                    currentLanguage = currentLanguage
                )
            } else {
                SearchResults(
                    results = uiState.searchResults,
                    onListingClick = onListingClick,
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

@Composable
private fun FiltersSection(
    selectedCropType: String?,
    selectedLocation: String?,
    onCropTypeSelected: (String?) -> Unit,
    onLocationSelected: (String?) -> Unit,
    onClearFilters: () -> Unit,
    currentLanguage: String
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Filters"
                    "ta" -> "வடிகட்டிகள்"
                    "si" -> "පෙරහන්"
                    else -> "Filters"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (selectedCropType != null || selectedLocation != null) {
                TextButton(onClick = onClearFilters) {
                    Text(when (currentLanguage) {
                        "en" -> "Clear All"
                        "ta" -> "அனைத்தையும் அழி"
                        "si" -> "සියල්ල මකන්න"
                        else -> "Clear All"
                    })
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Crop type filters
        Text(
            text = when (currentLanguage) {
                "en" -> "Crop Types"
                "ta" -> "பயிர் வகைகள்"
                "si" -> "බෝග වර්ග"
                else -> "Crop Types"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(CropTypes.ALL_CROPS) { cropType ->
                FilterChip(
                    onClick = {
                        onCropTypeSelected(if (selectedCropType == cropType) null else cropType)
                    },
                    label = { Text(CropTypes.getCropName(cropType, currentLanguage)) },
                    selected = selectedCropType == cropType
                )
            }
        }
        
        // Location filters
        Text(
            text = when (currentLanguage) {
                "en" -> "Locations"
                "ta" -> "இடங்கள்"
                "si" -> "ස්ථාන"
                else -> "Locations"
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            items(PickupLocations.ALL_LOCATIONS.take(5)) { location ->
                FilterChip(
                    onClick = {
                        onLocationSelected(if (selectedLocation == location) null else location)
                    },
                    label = { Text(TranslationUtil.getLocationName(location, currentLanguage)) },
                    selected = selectedLocation == location
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<Listing>,
    onListingClick: (String) -> Unit,
    currentLanguage: String
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = when (currentLanguage) {
                    "en" -> "${results.size} results found"
                    "ta" -> "${results.size} முடிவுகள் கண்டறியப்பட்டன"
                    "si" -> "ප්‍රතිඵල ${results.size} ක් හමු විය"
                    else -> "${results.size} results found"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(results) { listing ->
            ListingCard(
                listing = listing,
                onClick = { onListingClick(listing.id) },
                currentLanguage = currentLanguage
            )
        }
    }
}

@Composable
private fun EmptySearchResults(searchQuery: String, currentLanguage: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = when (currentLanguage) {
                "en" -> "No results found"
                "ta" -> "தேடல் முடிவுகள் இல்லை"
                "si" -> "ප්‍රතිඵල හමු නොවීය"
                else -> "No results found"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (currentLanguage) {
                "en" -> "Try searching for different keywords or check your spelling"
                "ta" -> "வெவ்வேறு முக்கிய வார்த்தைகளைத் தேட முயற்சிக்கவும் அல்லது உங்கள் எழுத்துப்பிழையைச் சரிபார்க்கவும்"
                "si" -> "විවිධ මූල පද සෙවීමට උත්සාහ කරන්න හෝ ඔබේ අක්ෂර වින්‍යාසය පරීක්ෂා කරන්න"
                else -> "Try searching for different keywords or check your spelling"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchSuggestions(
    onCropTypeClick: (String) -> Unit,
    currentLanguage: String
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = when (currentLanguage) {
                "en" -> "Popular Searches"
                "ta" -> "பிரபலமான தேடல்கள்"
                "si" -> "ජනප්‍රිය සෙවීම්"
                else -> "Popular Searches"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CropTypes.ALL_CROPS.take(6)) { cropType ->
                SuggestionChip(
                    onClick = { onCropTypeClick(cropType) },
                    label = { Text(CropTypes.getCropName(cropType, currentLanguage)) }
                )
            }
        }
    }
}

@Composable
private fun ListingCard(
    listing: Listing,
    onClick: () -> Unit,
    currentLanguage: String
) {
    // Safe fallbacks for all displayed fields to prevent crashes
    val unknownText = when (currentLanguage) {
        "en" -> "Unknown"
        "ta" -> "தெரியவில்லை"
        "si" -> "නොදනී"
        else -> "Unknown"
    }

    val cropName = try {
        CropTypes.getCropName(listing.cropType, currentLanguage).takeIf { it.isNotBlank() } ?: unknownText
    } catch (e: Exception) {
        unknownText
    }

    val locationName = try {
        TranslationUtil.getLocationName(listing.location, currentLanguage).takeIf { it.isNotBlank() } ?: unknownText
    } catch (e: Exception) {
        unknownText
    }

    val unitName = try {
        com.senthapps.slagrimarket.data.model.Units.getUnitName(listing.unit, currentLanguage).takeIf { it.isNotBlank() } ?: "unit"
    } catch (e: Exception) {
        "unit"
    }

    val qualityGrade = try {
        listing.quality.getDisplayString(currentLanguage).takeIf { it.isNotBlank() } ?: unknownText
    } catch (e: Exception) {
        unknownText
    }

    val pricePerUnit = try {
        if (listing.pricePerUnit >= 0) listing.pricePerUnit else 0.0
    } catch (e: Exception) {
        0.0
    }

    val quantity = try {
        if (listing.quantity >= 0) listing.quantity else 0.0
    } catch (e: Exception) {
        0.0
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
                        text = "LKR ${String.format("%.2f", pricePerUnit)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "per $unitName"
                            "ta" -> "$unitName ஒன்றுக்கு"
                            "si" -> "$unitName කට"
                            else -> "per $unitName"
                        },
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
                    text = when (currentLanguage) {
                        "en" -> "Quantity: ${String.format("%.1f", quantity)} $unitName"
                        "ta" -> "அளவு: ${String.format("%.1f", quantity)} $unitName"
                        "si" -> "ප්‍රමාණය: ${String.format("%.1f", quantity)} $unitName"
                        else -> "Quantity: ${String.format("%.1f", quantity)} $unitName"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Grade $qualityGrade"
                        "ta" -> "தரம் $qualityGrade"
                        "si" -> "ශ්‍රේණිය $qualityGrade"
                        else -> "Grade $qualityGrade"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
