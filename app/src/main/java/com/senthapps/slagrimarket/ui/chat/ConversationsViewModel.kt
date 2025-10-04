package com.senthapps.slagrimarket.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Conversation
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.MessageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConversationsViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConversationsUiState())
    val uiState: StateFlow<ConversationsUiState> = _uiState.asStateFlow()

    fun loadConversations() {
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

                _uiState.value = _uiState.value.copy(currentUserId = currentUser.id)

                messageRepository.getConversationsForUser(currentUser.id).collect { conversations ->
                    _uiState.value = _uiState.value.copy(
                        conversations = conversations,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading conversations")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load conversations: ${e.message}"
                )
            }
        }
    }
}

data class ConversationsUiState(
    val conversations: List<Conversation> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
