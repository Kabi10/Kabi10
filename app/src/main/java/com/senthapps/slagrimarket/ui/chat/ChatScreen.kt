package com.senthapps.slagrimarket.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.Message
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversationId: String,
    otherUserName: String,
    onNavigateBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(conversationId) {
        viewModel.loadConversation(conversationId)
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = otherUserName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        },
        bottomBar = {
            MessageInputBar(
                message = uiState.currentMessage,
                onMessageChange = viewModel::updateMessage,
                onSendMessage = {
                    viewModel.sendMessage()
                    focusManager.clearFocus()
                },
                isLoading = uiState.isSending,
                currentLanguage = currentLanguage
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                ErrorState(
                    error = uiState.error!!,
                    onRetry = { viewModel.loadConversation(conversationId) },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    currentLanguage = currentLanguage
                )
            }
            uiState.messages.isEmpty() -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    currentLanguage = currentLanguage
                )
            }
            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(
                            message = message,
                            isFromCurrentUser = message.senderId == uiState.currentUserId,
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    message: Message,
    isFromCurrentUser: Boolean,
    currentLanguage: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromCurrentUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isFromCurrentUser) 16.dp else 4.dp,
                bottomEnd = if (isFromCurrentUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromCurrentUser) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!isFromCurrentUser) {
                    Text(
                        text = message.senderName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isFromCurrentUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                
                Text(
                    text = formatMessageTime(message.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isFromCurrentUser) {
                        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
        }
    }
}

@Composable
private fun MessageInputBar(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean,
    currentLanguage: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(when (currentLanguage) {
                        "en" -> "Type a message..."
                        "ta" -> "செய்தி தட்டச்சு செய்யவும்..."
                        "si" -> "පණිවිඩයක් ටයිප් කරන්න..."
                        else -> "Type a message..."
                    })
                },
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = { onSendMessage() }
                ),
                maxLines = 4
            )
            
            IconButton(
                onClick = onSendMessage,
                enabled = message.isNotBlank() && !isLoading,
                modifier = Modifier.size(48.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier,
    currentLanguage: String
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Email,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = when (currentLanguage) {
                "en" -> "No messages yet"
                "ta" -> "இன்னும் செய்திகள் இல்லை"
                "si" -> "තවම පණිවිඩ නැත"
                else -> "No messages yet"
            },
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = when (currentLanguage) {
                "en" -> "Start the conversation by sending a message"
                "ta" -> "செய்தி அனுப்பி உரையாடலைத் தொடங்குங்கள்"
                "si" -> "පණිවිඩයක් යැවීමෙන් සංවාදය ආරම්භ කරන්න"
                else -> "Start the conversation by sending a message"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    currentLanguage: String
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(
                when (currentLanguage) {
                    "en" -> "Retry"
                    "ta" -> "மீண்டும் முயற்சிக்கவும்"
                    "si" -> "නැවත උත්සාහ කරන්න"
                    else -> "Retry"
                }
            )
        }
    }
}

private fun formatMessageTime(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val formatter = DateTimeFormatter.ofPattern("HH:mm")
        instant.atZone(ZoneId.systemDefault()).format(formatter)
    } catch (e: Exception) {
        timestamp
    }
}
