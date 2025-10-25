---
name: "🌟 Good First Issue: Add Search Filters to Listings Screen"
about: Implement filtering by crop type, price range, and location (Hard - Feature development!)
title: "[Hard] Add search filters to listings screen"
labels: good first issue, enhancement, feature, ui
assignees: ''
---

## 🎯 Issue Description

The listings screen currently shows all active listings without any filtering options. Users need the ability to filter listings by crop type, price range, and location to find what they're looking for more easily.

**Your task**: Add a filter UI and implement filtering logic in the listings screen.

## 📚 What You'll Learn

- ✅ Jetpack Compose UI development
- ✅ Material Design 3 components
- ✅ State management in Compose
- ✅ ViewModel logic and data transformation
- ✅ Kotlin collection operations (filter, map, etc.)
- ✅ User experience (UX) design
- ✅ Trilingual UI implementation

## 🔍 Current Behavior

1. User opens Listings screen
2. All active listings are displayed
3. No way to filter or narrow down results ❌

## ✨ Expected Behavior

1. User opens Listings screen
2. User taps "Filter" button
3. Filter bottom sheet appears with options:
   - **Crop Type**: Dropdown with all crop types
   - **Price Range**: Min/Max price inputs
   - **Location**: Dropdown with locations
4. User selects filters and taps "Apply"
5. Listings are filtered based on criteria ✅
6. User can clear filters to see all listings again

## 📂 Relevant Files

### Files to Modify:

1. **`app/src/main/java/com/senthapps/slagrimarket/ui/listings/ListingsScreen.kt`**
   - Add filter button to top bar
   - Add filter bottom sheet UI
   - Display active filters

2. **`app/src/main/java/com/senthapps/slagrimarket/ui/listings/ListingsViewModel.kt`**
   - Add filter state
   - Implement filter logic
   - Add `applyFilters()` and `clearFilters()` functions

### Files to Reference:

3. **`app/src/main/java/com/senthapps/slagrimarket/data/model/Listing.kt`**
   - Understand Listing data structure

4. **`app/src/main/java/com/senthapps/slagrimarket/ui/search/AdvancedSearchScreen.kt`**
   - Reference for filter UI patterns

## 💡 Implementation Hints

### Step 1: Add Filter State to ViewModel

```kotlin
data class ListingFilters(
    val cropType: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val location: String? = null
)

data class ListingsUiState(
    val listings: List<Listing> = emptyList(),
    val filteredListings: List<Listing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val activeFilters: ListingFilters = ListingFilters()
)

class ListingsViewModel @Inject constructor(
    private val repository: ListingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListingsUiState())
    val uiState: StateFlow<ListingsUiState> = _uiState.asStateFlow()
    
    fun applyFilters(filters: ListingFilters) {
        _uiState.update { currentState ->
            val filtered = currentState.listings.filter { listing ->
                // TODO: Implement filter logic
                val matchesCrop = filters.cropType == null || 
                    listing.cropName.equals(filters.cropType, ignoreCase = true)
                
                val matchesPrice = (filters.minPrice == null || listing.pricePerUnit >= filters.minPrice) &&
                    (filters.maxPrice == null || listing.pricePerUnit <= filters.maxPrice)
                
                val matchesLocation = filters.location == null || 
                    listing.location.contains(filters.location, ignoreCase = true)
                
                matchesCrop && matchesPrice && matchesLocation
            }
            
            currentState.copy(
                filteredListings = filtered,
                activeFilters = filters
            )
        }
    }
    
    fun clearFilters() {
        _uiState.update { it.copy(
            filteredListings = it.listings,
            activeFilters = ListingFilters()
        )}
    }
}
```

### Step 2: Create Filter Bottom Sheet UI

```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilters: ListingFilters,
    availableCrops: List<String>,
    availableLocations: List<String>,
    onApplyFilters: (ListingFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCrop by remember { mutableStateOf(currentFilters.cropType) }
    var minPrice by remember { mutableStateOf(currentFilters.minPrice?.toString() ?: "") }
    var maxPrice by remember { mutableStateOf(currentFilters.maxPrice?.toString() ?: "") }
    var selectedLocation by remember { mutableStateOf(currentFilters.location) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.filter_listings),
                style = MaterialTheme.typography.headlineSmall
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Crop Type Dropdown
            // TODO: Implement dropdown
            
            // Price Range Inputs
            // TODO: Implement min/max price fields
            
            // Location Dropdown
            // TODO: Implement dropdown
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Apply and Clear Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onApplyFilters(ListingFilters())
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.clear_filters))
                }
                
                Button(
                    onClick = {
                        onApplyFilters(
                            ListingFilters(
                                cropType = selectedCrop,
                                minPrice = minPrice.toDoubleOrNull(),
                                maxPrice = maxPrice.toDoubleOrNull(),
                                location = selectedLocation
                            )
                        )
                        onDismiss()
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(R.string.apply_filters))
                }
            }
        }
    }
}
```

### Step 3: Add Filter Button to ListingsScreen

```kotlin
@Composable
fun ListingsScreen(
    viewModel: ListingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onListingClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.all_listings)) },
                navigationIcon = { /* Back button */ },
                actions = {
                    // Filter button
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = stringResource(R.string.filter)
                        )
                    }
                }
            )
        }
    ) { padding ->
        // Display filtered listings
        val displayListings = if (uiState.activeFilters != ListingFilters()) {
            uiState.filteredListings
        } else {
            uiState.listings
        }
        
        // Show active filters chip
        if (uiState.activeFilters != ListingFilters()) {
            ActiveFiltersChip(
                filters = uiState.activeFilters,
                onClearFilters = { viewModel.clearFilters() }
            )
        }
        
        // Listings list
        LazyColumn {
            items(displayListings) { listing ->
                ListingCard(
                    listing = listing,
                    onClick = { onListingClick(listing.id) }
                )
            }
        }
    }
    
    // Filter bottom sheet
    if (showFilterSheet) {
        FilterBottomSheet(
            currentFilters = uiState.activeFilters,
            availableCrops = /* TODO: Get from listings */,
            availableLocations = /* TODO: Get from listings */,
            onApplyFilters = { filters ->
                viewModel.applyFilters(filters)
            },
            onDismiss = { showFilterSheet = false }
        )
    }
}
```

### Step 4: Add String Resources

Add to `values/strings.xml`, `values-ta/strings.xml`, `values-si/strings.xml`:

```xml
<string name="filter_listings">Filter Listings</string>
<string name="filter">Filter</string>
<string name="crop_type">Crop Type</string>
<string name="price_range">Price Range</string>
<string name="min_price">Minimum Price</string>
<string name="max_price">Maximum Price</string>
<string name="location">Location</string>
<string name="apply_filters">Apply Filters</string>
<string name="clear_filters">Clear Filters</string>
<string name="active_filters">Active Filters</string>
```

## ✅ Acceptance Criteria

- [ ] Filter button added to ListingsScreen top bar
- [ ] Filter bottom sheet appears when button is clicked
- [ ] Filter options include:
  - [ ] Crop type dropdown
  - [ ] Min/Max price inputs
  - [ ] Location dropdown
- [ ] "Apply Filters" button filters listings correctly
- [ ] "Clear Filters" button resets to show all listings
- [ ] Active filters are displayed as chips above listings
- [ ] Filtered count shown (e.g., "Showing 5 of 20 listings")
- [ ] All strings are externalized and translated (EN/TA/SI)
- [ ] UI follows Material Design 3 guidelines
- [ ] No crashes or errors
- [ ] Smooth animations and transitions

## 🧪 Testing Instructions

1. Build and run the app
2. Navigate to Listings screen
3. Tap filter button
4. Select crop type (e.g., "Tomatoes")
5. Verify only tomato listings are shown
6. Add price range filter (e.g., 50-100)
7. Verify listings are further filtered
8. Clear filters
9. Verify all listings are shown again
10. Test with different combinations
11. Test in all three languages

## 📖 Helpful Resources

- [Material 3 Bottom Sheets](https://developer.android.com/jetpack/compose/components/bottom-sheets)
- [Kotlin Collection Operations](https://kotlinlang.org/docs/collection-filtering.html)
- [Compose State Management](https://developer.android.com/jetpack/compose/state)
- [Material 3 Dropdowns](https://developer.android.com/jetpack/compose/components/menus)
- See `ARCHITECTURE.md` for state management patterns
- See `CONTRIBUTING.md` for UI guidelines

## 🎓 Difficulty Level

**Hard** - Estimated time: 6-10 hours

This is a challenging issue because:
- ⚠️ Multiple UI components to implement
- ⚠️ Complex state management
- ⚠️ Trilingual support required
- ⚠️ UX considerations (empty states, loading, etc.)
- ✅ Great for learning full feature development
- ✅ Visible impact on user experience

## 💡 Tips for Success

1. **Break it down**: Implement one filter at a time
2. **Test frequently**: Run the app after each addition
3. **Reference existing code**: Look at `AdvancedSearchScreen.kt`
4. **Start with UI**: Get the bottom sheet working first
5. **Then add logic**: Implement filtering after UI is done
6. **Handle edge cases**: Empty results, invalid inputs, etc.

## 🔄 Bonus Challenges

- [ ] Add "Sort by" options (price, date, distance)
- [ ] Save filter preferences
- [ ] Add filter presets (e.g., "Cheap vegetables", "Nearby")
- [ ] Animate filter count changes
- [ ] Add filter analytics tracking

## 💬 Need Help?

- Look at `AdvancedSearchScreen.kt` for similar patterns
- Check Material 3 documentation for bottom sheets
- Ask questions in the comments below
- Break the task into smaller PRs if needed

Good luck! 🚀✨

