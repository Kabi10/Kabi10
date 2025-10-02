package com.senthapps.slagrimarket.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _searchQuery = MutableStateFlow("")
    
    init {
        // Auto-search when query changes with debounce
        _searchQuery
            .debounce(300)
            .distinctUntilChanged()
            .onEach { query ->
                if (query.isNotBlank()) {
                    performSearchInternal(query)
                }
            }
            .launchIn(viewModelScope)
    }
    
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }
    
    fun performSearch() {
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            performSearchInternal(query)
        } else {
            searchWithFilters()
        }
    }
    
    private fun performSearchInternal(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                listingRepository.getAllActiveListingsFlow()
                    .catch { error ->
                        Timber.e(error, "Error performing search")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Search failed"
                        )
                    }
                    .collect { allListings ->
                        val filteredResults = filterListings(allListings, query)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            searchResults = filteredResults,
                            error = null
                        )
                    }
            } catch (e: Exception) {
                Timber.e(e, "Error performing search")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Search failed"
                )
            }
        }
    }
    
    private fun filterListings(listings: List<Listing>, query: String): List<Listing> {
        val state = _uiState.value
        
        return listings.filter { listing ->
            // Text search
            val matchesQuery = query.isBlank() || 
                listing.cropType.contains(query, ignoreCase = true) ||
                listing.location.contains(query, ignoreCase = true)
            
            // Crop type filter
            val matchesCropType = state.selectedCropType == null || 
                listing.cropType == state.selectedCropType
            
            // Location filter
            val matchesLocation = state.selectedLocation == null || 
                listing.location.contains(state.selectedLocation, ignoreCase = true)
            
            matchesQuery && matchesCropType && matchesLocation
        }
    }
    
    fun selectCropType(cropType: String?) {
        _uiState.value = _uiState.value.copy(selectedCropType = cropType)
        searchWithFilters()
    }
    
    fun selectLocation(location: String?) {
        _uiState.value = _uiState.value.copy(selectedLocation = location)
        searchWithFilters()
    }
    
    private fun searchWithFilters() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                listingRepository.searchListingsFlow(
                    cropType = _uiState.value.selectedCropType,
                    location = _uiState.value.selectedLocation
                )
                .catch { error ->
                    Timber.e(error, "Error filtering listings")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Filter failed"
                    )
                }
                .collect { filteredListings ->
                    val query = _uiState.value.searchQuery
                    val finalResults = if (query.isNotBlank()) {
                        filterListings(filteredListings, query)
                    } else {
                        filteredListings
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        searchResults = finalResults,
                        error = null
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error filtering listings")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Filter failed"
                )
            }
        }
    }
    
    fun clearSearch() {
        _searchQuery.value = ""
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            searchResults = emptyList()
        )
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            selectedCropType = null,
            selectedLocation = null
        )
        
        val query = _uiState.value.searchQuery
        if (query.isNotBlank()) {
            performSearchInternal(query)
        } else {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class SearchUiState(
    val searchQuery: String = "",
    val selectedCropType: String? = null,
    val selectedLocation: String? = null,
    val searchResults: List<Listing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
