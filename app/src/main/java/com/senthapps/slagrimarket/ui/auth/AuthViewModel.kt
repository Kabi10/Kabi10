package com.senthapps.slagrimarket.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    // MVP: Simplified ViewModel without OTP functionality
    val isLoggedIn = authRepository.isLoggedIn
    val currentUser = authRepository.currentUser

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            Timber.d("User logged out")
        }
    }
}
