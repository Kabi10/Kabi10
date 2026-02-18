package com.senthapps.slagrimarket.ui.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.Notification
import com.senthapps.slagrimarket.data.model.NotificationType
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onNotificationClick: (String, String?) -> Unit, // type, relatedId
    viewModel: NotificationsViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Notifications"
                            "ta" -> "அறிவிப்புகள்"
                            "si" -> "දැනුම්දීම්"
                            else -> "Notifications"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (uiState.notifications.any { !it.isRead }) {
                        TextButton(onClick = { viewModel.markAllAsRead() }) {
                            Text(when (currentLanguage) {
                                "en" -> "Mark all read"
                                "ta" -> "அனைத்தையும் படித்ததாகக் குறி"
                                "si" -> "සියල්ල කියවූ ලෙස සලකුණු කරන්න"
                                else -> "Mark all read"
                            })
                        }
                    }
                }
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
            uiState.notifications.isEmpty() -> {
                EmptyState(
                    currentLanguage = currentLanguage,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.notifications) { notification ->
                        NotificationCard(
                            notification = notification,
                            currentLanguage = currentLanguage,
                            onClick = {
                                viewModel.markAsRead(notification.id)
                                onNotificationClick(notification.type.name, notification.relatedId)
                            },
                            onDelete = {
                                viewModel.deleteNotification(notification.id)
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationCard(
    notification: Notification,
    currentLanguage: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Determine if this notification type should show an action button
    val actionLabel: String? = when (notification.type) {
        NotificationType.ORDER_RECEIVED, NotificationType.ORDER_CONFIRMED,
        NotificationType.ORDER_READY, NotificationType.ORDER_COMPLETED,
        NotificationType.ORDER_CANCELLED -> when (currentLanguage) {
            "si" -> "ඇණවුම බලන්න →"
            "ta" -> "ஆர்டர் பார்க்க →"
            else -> "VIEW ORDER →"
        }
        NotificationType.NEW_MESSAGE -> when (currentLanguage) {
            "si" -> "පණිවිඩය බලන්න →"
            "ta" -> "செய்தி பார்க்க →"
            else -> "VIEW MESSAGE →"
        }
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (notification.isRead) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = getNotificationColor(notification.type).copy(alpha = 0.2f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getNotificationIcon(notification.type),
                        contentDescription = null,
                        tint = getNotificationColor(notification.type),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold
                    )
                    if (!notification.isRead) {
                        Surface(
                            modifier = Modifier.size(8.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary
                        ) {}
                    }
                }

                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = formatTime(notification.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Prominent action button for order/message notifications — easy for elderly farmers
                if (actionLabel != null) {
                    TextButton(
                        onClick = onClick,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = actionLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = getNotificationColor(notification.type),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Delete button
            IconButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Notification") },
            text = { Text("Are you sure you want to delete this notification?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun EmptyState(
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = when (currentLanguage) {
                    "en" -> "No notifications"
                    "ta" -> "அறிவிப்புகள் இல்லை"
                    "si" -> "දැනුම්දීම් නැත"
                    else -> "No notifications"
                },
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = when (currentLanguage) {
                    "en" -> "You're all caught up!"
                    "ta" -> "நீங்கள் அனைத்தையும் பார்த்துவிட்டீர்கள்!"
                    "si" -> "ඔබ සියල්ල දැක ඇත!"
                    else -> "You're all caught up!"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getNotificationIcon(type: NotificationType): ImageVector {
    return when (type) {
        NotificationType.ORDER_RECEIVED -> Icons.Default.ShoppingCart
        NotificationType.ORDER_CONFIRMED -> Icons.Default.Check
        NotificationType.ORDER_READY -> Icons.Default.Done
        NotificationType.ORDER_COMPLETED -> Icons.Default.CheckCircle
        NotificationType.ORDER_CANCELLED -> Icons.Default.Close
        NotificationType.LISTING_VIEWED -> Icons.Default.Star
        NotificationType.PRICE_ALERT -> Icons.Default.Info
        NotificationType.SYSTEM -> Icons.Default.Notifications
        NotificationType.NEW_MESSAGE -> Icons.Default.Email
        NotificationType.NEW_REVIEW -> Icons.Default.Star
    }
}

private fun getNotificationColor(type: NotificationType): Color {
    return when (type) {
        NotificationType.ORDER_RECEIVED -> Color(0xFF4CAF50)
        NotificationType.ORDER_CONFIRMED -> Color(0xFF2196F3)
        NotificationType.ORDER_READY -> Color(0xFF9C27B0)
        NotificationType.ORDER_COMPLETED -> Color(0xFF4CAF50)
        NotificationType.ORDER_CANCELLED -> Color(0xFFF44336)
        NotificationType.LISTING_VIEWED -> Color(0xFFFF9800)
        NotificationType.PRICE_ALERT -> Color(0xFFFF5722)
        NotificationType.SYSTEM -> Color(0xFF607D8B)
        NotificationType.NEW_MESSAGE -> Color(0xFF2196F3)
        NotificationType.NEW_REVIEW -> Color(0xFFFF9800)
    }
}

private fun formatTime(timestamp: String): String {
    return try {
        val instant = Instant.parse(timestamp)
        val now = Instant.now()
        val duration = java.time.Duration.between(instant, now)

        when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toMinutes() < 60 -> "${duration.toMinutes()}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            duration.toDays() < 7 -> "${duration.toDays()}d ago"
            else -> {
                val formatter = DateTimeFormatter.ofPattern("MMM dd")
                instant.atZone(ZoneId.systemDefault()).format(formatter)
            }
        }
    } catch (e: Exception) {
        timestamp
    }
}
