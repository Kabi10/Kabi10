package com.senthapps.slagrimarket.ui.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.ReviewType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WriteReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WriteReviewUiState())
    val uiState: StateFlow<WriteReviewUiState> = _uiState.asStateFlow()

    fun updateRating(rating: Int) {
        _uiState.value = _uiState.value.copy(rating = rating)
    }

    fun updateComment(comment: String) {
        _uiState.value = _uiState.value.copy(comment = comment)
    }

    fun submitReview(transactionId: String, revieweeId: String, revieweeName: String) {
        if (_uiState.value.rating == 0) {
            _uiState.value = _uiState.value.copy(error = "Please select a rating")
            return
        }

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

                // Determine review type based on user type
                val reviewType = if (currentUser.userType == com.senthapps.slagrimarket.data.model.UserType.FARMER) {
                    ReviewType.BUYER // Farmer reviewing buyer
                } else {
                    ReviewType.FARMER // Buyer reviewing farmer
                }

                val result = reviewRepository.createReview(
                    transactionId = transactionId,
                    reviewerId = currentUser.id,
                    reviewerName = currentUser.name,
                    revieweeId = revieweeId,
                    rating = _uiState.value.rating,
                    comment = _uiState.value.comment,
                    reviewType = reviewType
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                        Timber.d("Review submitted successfully")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to submit review"
                        )
                        Timber.e(error, "Failed to submit review")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error submitting review")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }
}

data class WriteReviewUiState(
    val rating: Int = 0,
    val comment: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
