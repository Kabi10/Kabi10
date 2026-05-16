package com.senthapps.slagrimarket.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.senthapps.slagrimarket.data.model.Message
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.MessageRepository
import com.senthapps.slagrimarket.data.sync.ChatRealtimeService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val authRepository: AuthRepository,
    private val chatRealtimeService: ChatRealtimeService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentConversationId: String = ""

    fun loadConversation(conversationId: String) {
        currentConversationId = conversationId

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

                // Refresh messages from API
                messageRepository.refreshMessages(conversationId, currentUser.id)

                // Subscribe to real-time updates
                chatRealtimeService.subscribeToConversation(conversationId)

                // Collect real-time messages in background
                launch {
                    chatRealtimeService.newMessages.collect { _ ->
                        // Room Flow will automatically update UI
                        Timber.d("Real-time message received, Room will update")
                    }
                }

                messageRepository.getMessagesForConversation(conversationId).collect { messages ->
                    _uiState.value = _uiState.value.copy(
                        messages = messages,
                        isLoading = false
                    )

                    messageRepository.markMessagesAsRead(conversationId, currentUser.id)
                }
            } catch (e: Exception) {
                Timber.e(e, "Error loading conversation")
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load conversation: ${e.message}"
                )
            }
        }
    }

    fun updateMessage(message: String) {
        _uiState.value = _uiState.value.copy(currentMessage = message)
    }

    fun sendMessage() {
        val message = _uiState.value.currentMessage.trim()
        if (message.isBlank() || _uiState.value.isSending) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)

            try {
                val currentUser = authRepository.getCurrentUser()
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = "User not found"
                    )
                    return@launch
                }

                val conversation = messageRepository.getConversationById(currentConversationId)
                if (conversation == null) {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error = "Conversation not found"
                    )
                    return@launch
                }

                val receiverId = if (conversation.participant1Id == currentUser.id) {
                    conversation.participant2Id
                } else {
                    conversation.participant1Id
                }

                val result = messageRepository.sendMessage(
                    conversationId = currentConversationId,
                    senderId = currentUser.id,
                    senderName = currentUser.name ?: "Anonymous",
                    receiverId = receiverId,
                    content = message
                )

                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(
                            currentMessage = "",
                            isSending = false
                        )
                        Timber.d("Message sent successfully")
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSending = false,
                            error = error.message ?: "Failed to send message"
                        )
                        Timber.e(error, "Failed to send message")
                    }
                )
            } catch (e: Exception) {
                Timber.e(e, "Error sending message")
                _uiState.value = _uiState.value.copy(
                    isSending = false,
                    error = "An unexpected error occurred: ${e.message}"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        chatRealtimeService.unsubscribe()
    }
}

data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val currentMessage: String = "",
    val currentUserId: String = "",
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val error: String? = null
)
