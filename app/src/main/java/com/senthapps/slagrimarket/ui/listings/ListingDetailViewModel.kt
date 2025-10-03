package com.senthapps.slagrimarket.ui.listings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ListingRepository
import com.senthapps.slagrimarket.data.repository.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ListingDetailViewModel @Inject constructor(
    private val listingRepository: ListingRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ListingDetailUiState())
    val uiState: StateFlow<ListingDetailUiState> = _uiState.asStateFlow()

    val currentUser: Flow<User?> = authRepository.currentUser

    fun loadListing(listingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            try {
                // First try to get from local database
                listingRepository.getListingById(listingId).collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            _uiState.update {
                                it.copy(
                                    listing = resource.data,
                                    isLoading = false,
                                    error = null
                                )
                            }
                            
                            // Increment view count in background
                            incrementViewCount(listingId)
                        }
                        is Resource.Error -> {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = resource.message ?: "Failed to load listing"
                                )
                            }
                            Timber.e("Error loading listing: ${resource.message}")
                        }
                        is Resource.Loading -> {
                            _uiState.update { it.copy(isLoading = true) }
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load listing: ${e.message}"
                    )
                }
                Timber.e(e, "Exception loading listing")
            }
        }
    }

    private fun incrementViewCount(listingId: String) {
        viewModelScope.launch {
            try {
                // This will be synced to backend when online
                listingRepository.incrementViewCount(listingId)
            } catch (e: Exception) {
                Timber.e(e, "Failed to increment view count")
                // Don't show error to user, this is a background operation
            }
        }
    }

    fun incrementInquiryCount(listingId: String) {
        viewModelScope.launch {
            try {
                listingRepository.incrementInquiryCount(listingId)
            } catch (e: Exception) {
                Timber.e(e, "Failed to increment inquiry count")
            }
        }
    }
}

data class ListingDetailUiState(
    val listing: Listing? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
