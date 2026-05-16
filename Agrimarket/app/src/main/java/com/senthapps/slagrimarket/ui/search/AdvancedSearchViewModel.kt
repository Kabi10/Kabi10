package com.senthapps.slagrimarket.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.repository.ListingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AdvancedSearchViewModel @Inject constructor(
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdvancedSearchUiState())
    val uiState: StateFlow<AdvancedSearchUiState> = _uiState.asStateFlow()

    fun updateCropType(cropType: String) {
        _uiState.value = _uiState.value.copy(cropType = cropType)
    }

    fun updateQuality(quality: String) {
        _uiState.value = _uiState.value.copy(quality = quality)
    }

    fun updateMinPrice(price: String) {
        _uiState.value = _uiState.value.copy(minPrice = price)
    }

    fun updateMaxPrice(price: String) {
        _uiState.value = _uiState.value.copy(maxPrice = price)
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(location = location)
    }

    fun search() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, isInitial = false)

            try {
                val cropType = _uiState.value.cropType.takeIf { it.isNotBlank() }
                val location = _uiState.value.location.takeIf { it.isNotBlank() }

                listingRepository.searchListings(
                    cropType = cropType,
                    location = location,
                    forceRefresh = true
                ).collect { resource ->
                    when (resource) {
                        is com.senthapps.slagrimarket.data.repository.Resource.Success -> {
                            var results = resource.data ?: emptyList()

                            // Apply quality filter
                            val quality = _uiState.value.quality.takeIf { it.isNotBlank() }
                            if (quality != null) {
                                results = results.filter { it.quality.name == quality }
                            }

                            // Apply price range filter
                            val minPrice = _uiState.value.minPrice.toDoubleOrNull()
                            val maxPrice = _uiState.value.maxPrice.toDoubleOrNull()
                            if (minPrice != null) {
                                results = results.filter { it.pricePerUnit >= minPrice }
                            }
                            if (maxPrice != null) {
                                results = results.filter { it.pricePerUnit <= maxPrice }
                            }

                            _uiState.value = _uiState.value.copy(
                                results = results,
                                isLoading = false
                            )
                        }
                        is com.senthapps.slagrimarket.data.repository.Resource.Error -> {
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = resource.message ?: "Search failed"
                            )
                        }
                        is com.senthapps.slagrimarket.data.repository.Resource.Loading -> {
                            _uiState.value = _uiState.value.copy(isLoading = true)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching listings")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Search failed: ${e.message}"
                )
            }
        }
    }

    fun clearFilters() {
        _uiState.value = AdvancedSearchUiState()
    }
}

data class AdvancedSearchUiState(
    val cropType: String = "",
    val quality: String = "",
    val minPrice: String = "",
    val maxPrice: String = "",
    val location: String = "",
    val results: List<Listing> = emptyList(),
    val isLoading: Boolean = false,
    val isInitial: Boolean = true,
    val error: String? = null
)
