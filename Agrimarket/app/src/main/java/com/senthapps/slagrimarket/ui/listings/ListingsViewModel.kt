package com.senthapps.slagrimarket.ui.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListingsViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(ListingsUiState())
    val uiState: StateFlow<ListingsUiState> = _uiState.asStateFlow()
    
    init {
        loadListings()
    }
    
    private fun loadListings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            listingRepository.getAllActiveListingsFlow()
                .catch { error ->
                    Timber.e(error, "Error loading listings")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to load listings"
                    )
                }
                .collect { listings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listings = listings,
                        error = null
                    )
                }
        }
    }
    
    fun searchListings(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // For now, just filter by crop type
            // In a real implementation, this would use the search API
            listingRepository.getAllActiveListingsFlow()
                .catch { error ->
                    Timber.e(error, "Error searching listings")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to search listings"
                    )
                }
                .collect { listings ->
                    val filteredListings = if (query.isBlank()) {
                        listings
                    } else {
                        listings.filter { listing ->
                            listing.cropType.contains(query, ignoreCase = true) ||
                            listing.location.contains(query, ignoreCase = true)
                        }
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listings = filteredListings,
                        error = null
                    )
                }
        }
    }
    
    fun filterByCropType(cropType: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            listingRepository.searchListingsFlow(cropType = cropType)
                .catch { error ->
                    Timber.e(error, "Error filtering listings")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to filter listings"
                    )
                }
                .collect { listings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listings = listings,
                        error = null
                    )
                }
        }
    }
    
    fun filterByLocation(location: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            listingRepository.searchListingsFlow(location = location)
                .catch { error ->
                    Timber.e(error, "Error filtering listings")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Failed to filter listings"
                    )
                }
                .collect { listings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        listings = listings,
                        error = null
                    )
                }
        }
    }
    
    fun refreshListings() {
        loadListings()
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ListingsUiState(
    val isLoading: Boolean = false,
    val listings: List<Listing> = emptyList(),
    val error: String? = null
)
