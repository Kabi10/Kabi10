package com.senthapps.slagrimarket.ui.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.FavoriteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favoriteRepository: FavoriteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState())
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                    return@launch
                }

                favoriteRepository.getFavoriteListingsForUser(currentUser.id).collect { listings ->
                    _uiState.value = _uiState.value.copy(
                        favorites = listings,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading favorites")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load favorites: ${e.message}"
                )
            }
        }
    }

    fun removeFavorite(listingId: String) {
        viewModelScope.launch {
            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(error = "User not found")
                    return@launch
                }

                favoriteRepository.toggleFavorite(currentUser.id, listingId)
                Timber.d("Removed favorite: $listingId")
            } catch (e: Exception) {
                Timber.e(e, "Error removing favorite")
                _uiState.value = _uiState.value.copy(
                    error = "Failed to remove favorite: ${e.message}"
                )
            }
        }
    }

    fun clearAllFavorites() {
        // This would require adding a method to FavoriteRepository
        // For now, just reload to show empty state
        loadFavorites()
    }
}

data class FavoritesUiState(
    val favorites: List<Listing> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
