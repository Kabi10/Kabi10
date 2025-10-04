package com.senthapps.slagrimarket.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val user = authRepository.getCurrentUser()
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        name = user.name,
                        phoneNumber = user.phone,
                        location = "", // User model doesn't have location field yet
                        userType = user.userType,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading user profile")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load profile: ${e.message}"
                )
            }
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }

    fun updateLocation(location: String) {
        _uiState.value = _uiState.value.copy(
            location = location,
            locationError = null
        )
    }

    fun saveProfile() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val user = authRepository.getCurrentUser()
                if (user == null) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "User not found"
                    )
                    return@launch
                }

                val result = authRepository.updateUserProfile(
                    userId = user.id,
                    name = _uiState.value.name,
                    location = _uiState.value.location
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                        Timber.d("Profile updated successfully")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to update profile"
                        )
                        Timber.e(error, "Failed to update profile")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error updating profile")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    fun isFormValid(): Boolean {
        val state = _uiState.value
        return state.name.isNotBlank() &&
                state.location.isNotBlank() &&
                state.nameError == null &&
                state.locationError == null
    }

    private fun validateForm(): Boolean {
        val state = _uiState.value
        var isValid = true

        if (state.name.isBlank()) {
            _uiState.value = _uiState.value.copy(nameError = "Name is required")
            isValid = false
        } else if (state.name.length < 2) {
            _uiState.value = _uiState.value.copy(nameError = "Name must be at least 2 characters")
            isValid = false
        }

        if (state.location.isBlank()) {
            _uiState.value = _uiState.value.copy(locationError = "Location is required")
            isValid = false
        }

        return isValid
    }
}

data class EditProfileUiState(
    val name: String = "",
    val phoneNumber: String = "",
    val location: String = "",
    val userType: UserType? = null,
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val locationError: String? = null
)
