package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.util.TranslationUtil

/**
 * Search and Filter Bar Component
 * Combines search input with category and location dropdown filters
 * Matches the HTML mockup design with Material Design 3 styling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String?,
    onCategoryChange: (String?) -> Unit,
    selectedLocation: String?,
    onLocationChange: (String?) -> Unit,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier,
    onSearchClick: () -> Unit = {}
) {
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showLocationDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search input field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Search crops, farmers, locations..."
                        "ta" -> "பயிர்கள், விவசாயிகள், இடங்களைத் தேடுங்கள்..."
                        "si" -> "බෝග, ගොවීන්, ස්ථාන සොයන්න..."
                        else -> "Search crops, farmers, locations..."
                    }
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search"
                        )
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Filter chips row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Category filter
            FilterChip(
                selected = selectedCategory != null,
                onClick = { showCategoryDropdown = true },
                label = {
                    Text(
                        text = if (selectedCategory != null) {
                            CropTypes.getCropName(selectedCategory, currentLanguage)
                        } else {
                            when (currentLanguage) {
                                "en" -> "Category"
                                "ta" -> "வகை"
                                "si" -> "කාණ්ඩය"
                                else -> "Category"
                            }
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            // Location filter
            FilterChip(
                selected = selectedLocation != null,
                onClick = { showLocationDropdown = true },
                label = {
                    Text(
                        text = if (selectedLocation != null) {
                            TranslationUtil.getLocationName(selectedLocation, currentLanguage)
                        } else {
                            when (currentLanguage) {
                                "en" -> "Location"
                                "ta" -> "இடம்"
                                "si" -> "ස්ථානය"
                                else -> "Location"
                            }
                        }
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )

            // Clear filters button (if any filter is active)
            if (selectedCategory != null || selectedLocation != null) {
                FilterChip(
                    selected = false,
                    onClick = {
                        onCategoryChange(null)
                        onLocationChange(null)
                    },
                    label = {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Clear"
                                "ta" -> "அழி"
                                "si" -> "ඉවත් කරන්න"
                                else -> "Clear"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        // Category dropdown menu
        DropdownMenu(
            expanded = showCategoryDropdown,
            onDismissRequest = { showCategoryDropdown = false }
        ) {
            // All categories option
            DropdownMenuItem(
                text = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "All Categories"
                            "ta" -> "அனைத்து வகைகள்"
                            "si" -> "සියලුම කාණ්ඩ"
                            else -> "All Categories"
                        },
                        fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = {
                    onCategoryChange(null)
                    showCategoryDropdown = false
                }
            )

            Divider()

            // Individual crop categories
            CropTypes.ALL_CROPS.forEach { cropType ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = CropTypes.getCropEmoji(cropType))
                            Text(
                                text = CropTypes.getCropName(cropType, currentLanguage),
                                fontWeight = if (selectedCategory == cropType) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    },
                    onClick = {
                        onCategoryChange(cropType)
                        showCategoryDropdown = false
                    }
                )
            }
        }

        // Location dropdown menu
        DropdownMenu(
            expanded = showLocationDropdown,
            onDismissRequest = { showLocationDropdown = false }
        ) {
            // All locations option
            DropdownMenuItem(
                text = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "All Locations"
                            "ta" -> "அனைத்து இடங்கள்"
                            "si" -> "සියලුම ස්ථාන"
                            else -> "All Locations"
                        },
                        fontWeight = if (selectedLocation == null) FontWeight.Bold else FontWeight.Normal
                    )
                },
                onClick = {
                    onLocationChange(null)
                    showLocationDropdown = false
                }
            )

            Divider()

            // Common locations in Jaffna region
            val locations = listOf(
                "Jaffna", "Chavakachcheri", "Point Pedro", "Nallur", "Chunnakam",
                "Manipay", "Kopay", "Tellippalai", "Chankanai", "Karainagar"
            )

            locations.forEach { location ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = TranslationUtil.getLocationName(location, currentLanguage),
                            fontWeight = if (selectedLocation == location) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onLocationChange(location)
                        showLocationDropdown = false
                    }
                )
            }
        }
    }
}

/**
 * Compact version of search filter bar for smaller spaces
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompactSearchFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier,
    hasActiveFilters: Boolean = false
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Search..."
                    "ta" -> "தேடுங்கள்..."
                    "si" -> "සොයන්න..."
                    else -> "Search..."
                }
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        trailingIcon = {
            Row {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
                Badge(
                    containerColor = if (hasActiveFilters) MaterialTheme.colorScheme.primary else Color.Transparent
                ) {
                    IconButton(onClick = onFilterClick) {
                        Icon(
                            imageVector = Icons.Default.List,
                            contentDescription = "Filters"
                        )
                    }
                }
            }
        },
        shape = RoundedCornerShape(16.dp),
        singleLine = true
    )
}

