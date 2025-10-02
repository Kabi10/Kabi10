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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.PickupLocations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateBack: () -> Unit,
    onListingClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
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
                        placeholder = { Text("Search crops, locations...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { 
                                    searchQuery = ""
                                    viewModel.clearSearch()
                                }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
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
                            contentDescription = "Back"
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
            // Filters section
            FiltersSection(
                selectedCropType = uiState.selectedCropType,
                selectedLocation = uiState.selectedLocation,
                onCropTypeSelected = viewModel::selectCropType,
                onLocationSelected = viewModel::selectLocation,
                onClearFilters = viewModel::clearFilters
            )
            
            Divider()
            
            // Results section
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.searchResults.isEmpty() && searchQuery.isNotEmpty()) {
                EmptySearchResults(searchQuery = searchQuery)
            } else if (uiState.searchResults.isEmpty()) {
                SearchSuggestions(
                    onCropTypeClick = { cropType ->
                        viewModel.selectCropType(cropType)
                        viewModel.performSearch()
                    }
                )
            } else {
                SearchResults(
                    results = uiState.searchResults,
                    onListingClick = onListingClick
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
    onClearFilters: () -> Unit
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
                text = "வடிகட்டிகள் / Filters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            if (selectedCropType != null || selectedLocation != null) {
                TextButton(onClick = onClearFilters) {
                    Text("Clear All")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Crop type filters
        Text(
            text = "Crop Types",
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
                    label = { Text(cropType.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) },
                    selected = selectedCropType == cropType
                )
            }
        }
        
        // Location filters
        Text(
            text = "Locations",
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
                    label = { Text(location) },
                    selected = selectedLocation == location
                )
            }
        }
    }
}

@Composable
private fun SearchResults(
    results: List<Listing>,
    onListingClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "${results.size} results found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        items(results) { listing ->
            ListingCard(
                listing = listing,
                onClick = { onListingClick(listing.id) }
            )
        }
    }
}

@Composable
private fun EmptySearchResults(searchQuery: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "தேடல் முடிவுகள் இல்லை",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "No results found",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Try searching for different keywords or check your spelling",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchSuggestions(
    onCropTypeClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "பிரபலமான தேடல்கள் / Popular Searches",
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
                    label = { Text(cropType.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }) }
                )
            }
        }
    }
}

@Composable
private fun ListingCard(
    listing: Listing,
    onClick: () -> Unit
) {
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
                        text = listing.cropType.replace("_", " ").replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = listing.location,
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
                        text = "per ${listing.unit}",
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
                    text = "Quantity: ${listing.quantity} ${listing.unit}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Grade ${listing.quality}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
