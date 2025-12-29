package com.senthapps.slagrimarket.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val isLoggedIn = authRepository.isLoggedIn
    val currentUser = authRepository.currentUser

    fun sendOtp(phoneNumber: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                otpSent = false
            )

            authRepository.sendOtp(phoneNumber).fold(
                onSuccess = { result ->
                    Timber.d("OTP sent successfully. OTP ID: ${result.otpId}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        otpSent = true,
                        otpId = result.otpId,
                        phoneNumber = phoneNumber
                    )
                    
                    // AUTO-VERIFY if OTP is returned (development/bypass mode)
                    result.otp?.let { otpCode ->
                        Timber.d("🚀 Bypassing manual OTP entry. Auto-verifying: $otpCode")
                        verifyOtp(phoneNumber, otpCode, result.otpId)
                    }
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to send OTP")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: context.getString(com.senthapps.slagrimarket.R.string.error_send_otp_failed)
                    )
                }
            )
        }
    }

    fun verifyOtp(phoneNumber: String, otp: String, otpId: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            authRepository.verifyOtp(phoneNumber, otp, otpId).fold(
                onSuccess = { user ->
                    Timber.d("OTP verified successfully. User: ${user.name}")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        user = user
                    )
                },
                onFailure = { error ->
                    Timber.e(error, "Failed to verify OTP")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: context.getString(com.senthapps.slagrimarket.R.string.error_invalid_otp)
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetOtpState() {
        _uiState.value = _uiState.value.copy(
            otpSent = false,
            otpId = null,
            error = null
        )
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
            Timber.d("User logged out")
        }
    }
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val otpSent: Boolean = false,
    val otpId: String? = null,
    val phoneNumber: String? = null,
    val isAuthenticated: Boolean = false,
    val user: User? = null
)
