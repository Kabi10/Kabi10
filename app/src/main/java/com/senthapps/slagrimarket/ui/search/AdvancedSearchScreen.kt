package com.senthapps.slagrimarket.ui.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.data.model.QualityGrades
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import com.senthapps.slagrimarket.ui.listings.ListingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    onNavigateBack: () -> Unit,
    onListingClick: (String) -> Unit,
    viewModel: AdvancedSearchViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    var showFilters by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Advanced Search"
                            "ta" -> "மேம்பட்ட தேடல்"
                            "si" -> "උසස් සෙවීම"
                            else -> "Advanced Search"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            imageVector = if (showFilters) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Toggle Filters"
                        )
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
            if (showFilters) {
                FilterSection(
                    uiState = uiState,
                    currentLanguage = currentLanguage,
                    onCropTypeChange = viewModel::updateCropType,
                    onQualityChange = viewModel::updateQuality,
                    onMinPriceChange = viewModel::updateMinPrice,
                    onMaxPriceChange = viewModel::updateMaxPrice,
                    onLocationChange = viewModel::updateLocation,
                    onSearch = viewModel::search,
                    onClearFilters = viewModel::clearFilters
                )
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                uiState.error != null -> {
                    ErrorState(
                        error = uiState.error!!,
                        onRetry = viewModel::search
                    )
                }
                uiState.results.isEmpty() && !uiState.isInitial -> {
                    EmptyState(currentLanguage)
                }
                else -> {
                    ResultsList(
                        listings = uiState.results,
                        currentLanguage = currentLanguage,
                        onListingClick = onListingClick
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    uiState: AdvancedSearchUiState,
    currentLanguage: String,
    onCropTypeChange: (String) -> Unit,
    onQualityChange: (String) -> Unit,
    onMinPriceChange: (String) -> Unit,
    onMaxPriceChange: (String) -> Unit,
    onLocationChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClearFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Filters"
                    "ta" -> "வடிகட்டிகள்"
                    "si" -> "පෙරහන්"
                    else -> "Filters"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Crop type dropdown
            var expandedCrop by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedCrop,
                onExpandedChange = { expandedCrop = it }
            ) {
                OutlinedTextField(
                    value = uiState.cropType,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(when (currentLanguage) {
                            "en" -> "Crop Type"
                            "ta" -> "பயிர் வகை"
                            "si" -> "බෝග වර්ගය"
                            else -> "Crop Type"
                        })
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCrop) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedCrop,
                    onDismissRequest = { expandedCrop = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            onCropTypeChange("")
                            expandedCrop = false
                        }
                    )
                    CropTypes.ALL_CROPS.forEach { crop ->
                        DropdownMenuItem(
                            text = { Text(crop) },
                            onClick = {
                                onCropTypeChange(crop)
                                expandedCrop = false
                            }
                        )
                    }
                }
            }

            // Quality dropdown
            var expandedQuality by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expandedQuality,
                onExpandedChange = { expandedQuality = it }
            ) {
                OutlinedTextField(
                    value = uiState.quality,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(when (currentLanguage) {
                            "en" -> "Quality Grade"
                            "ta" -> "தர நிலை"
                            "si" -> "ගුණාත්මක ශ්‍රේණිය"
                            else -> "Quality Grade"
                        })
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedQuality) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expandedQuality,
                    onDismissRequest = { expandedQuality = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            onQualityChange("")
                            expandedQuality = false
                        }
                    )
                    QualityGrades.ALL_GRADES.forEach { grade ->
                        DropdownMenuItem(
                            text = { Text("Grade $grade") },
                            onClick = {
                                onQualityChange(grade)
                                expandedQuality = false
                            }
                        )
                    }
                }
            }

            // Price range
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.minPrice,
                    onValueChange = onMinPriceChange,
                    label = {
                        Text(when (currentLanguage) {
                            "en" -> "Min Price"
                            "ta" -> "குறைந்த விலை"
                            "si" -> "අවම මිල"
                            else -> "Min Price"
                        })
                    },
                    prefix = { Text("LKR") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.maxPrice,
                    onValueChange = onMaxPriceChange,
                    label = {
                        Text(when (currentLanguage) {
                            "en" -> "Max Price"
                            "ta" -> "அதிக விலை"
                            "si" -> "උපරිම මිල"
                            else -> "Max Price"
                        })
                    },
                    prefix = { Text("LKR") },
                    modifier = Modifier.weight(1f)
                )
            }

            // Location
            OutlinedTextField(
                value = uiState.location,
                onValueChange = onLocationChange,
                label = {
                    Text(when (currentLanguage) {
                        "en" -> "Location"
                        "ta" -> "இடம்"
                        "si" -> "ස්ථානය"
                        else -> "Location"
                    })
                },
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, null)
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(when (currentLanguage) {
                        "en" -> "Clear"
                        "ta" -> "அழி"
                        "si" -> "මකන්න"
                        else -> "Clear"
                    })
                }
                Button(
                    onClick = onSearch,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Search, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(when (currentLanguage) {
                        "en" -> "Search"
                        "ta" -> "தேடு"
                        "si" -> "සොයන්න"
                        else -> "Search"
                    })
                }
            }
        }
    }
}

@Composable
private fun ResultsList(
    listings: List<com.senthapps.slagrimarket.data.model.Listing>,
    currentLanguage: String,
    onListingClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = when (currentLanguage) {
                    "en" -> "${listings.size} results found"
                    "ta" -> "${listings.size} முடிவுகள் கிடைத்தன"
                    "si" -> "ප්‍රතිඵල ${listings.size} ක් හමු විය"
                    else -> "${listings.size} results found"
                },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(listings) { listing ->
            ListingCard(
                listing = listing,
                onClick = { onListingClick(listing.id) },
                currentLanguage = currentLanguage
            )
        }
    }
}

@Composable
private fun EmptyState(currentLanguage: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when (currentLanguage) {
                    "en" -> "No results found"
                    "ta" -> "முடிவுகள் இல்லை"
                    "si" -> "ප්‍රතිඵල හමු නොවීය"
                    else -> "No results found"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = when (currentLanguage) {
                    "en" -> "Try adjusting your filters"
                    "ta" -> "உங்கள் வடிகட்டிகளை சரிசெய்யவும்"
                    "si" -> "ඔබේ පෙරහන් සකස් කරන්න"
                    else -> "Try adjusting your filters"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(text = error)
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
