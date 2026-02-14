package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.CreateConversationRequest
import com.senthapps.slagrimarket.data.api.MessageApiService
import com.senthapps.slagrimarket.data.api.SendMessageRequest
import com.senthapps.slagrimarket.data.dao.MessageDao
import com.senthapps.slagrimarket.data.model.Conversation
import com.senthapps.slagrimarket.data.model.Message
import com.senthapps.slagrimarket.data.model.MessageType
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val messageApiService: MessageApiService
) {

    fun getConversationsForUser(userId: String): Flow<List<Conversation>> {
        return messageDao.getConversationsForUser(userId)
    }

    suspend fun refreshConversations(userId: String) {
        try {
            val response = messageApiService.getConversations()
            if (response.isSuccessful && response.body()?.success == true) {
                val conversations = response.body()!!.conversations
                conversations.forEach { dto ->
                    val conversation = Conversation(
                        id = dto.id,
                        listingId = dto.listingId,
                        participant1Id = userId,
                        participant1Name = "",
                        participant2Id = dto.otherParticipant.id,
                        participant2Name = dto.otherParticipant.name ?: "",
                        lastMessage = dto.lastMessageText,
                        lastMessageTime = dto.lastMessageAt,
                        unreadCount = dto.unreadCount,
                        createdAt = dto.createdAt,
                        updatedAt = dto.updatedAt
                    )
                    messageDao.insertConversation(conversation)
                }
                Timber.d("Refreshed ${conversations.size} conversations from API")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing conversations from API")
        }
    }

    suspend fun getConversationById(conversationId: String): Conversation? {
        return try {
            messageDao.getConversationById(conversationId)
        } catch (e: Exception) {
            Timber.e(e, "Error getting conversation by ID")
            null
        }
    }

    suspend fun getOrCreateConversation(
        user1Id: String,
        user1Name: String,
        user2Id: String,
        user2Name: String,
        listingId: String? = null
    ): Result<Conversation> {
        return try {
            // Try to find existing conversation locally
            val existingConversation = messageDao.getConversationBetweenUsers(user1Id, user2Id)
            if (existingConversation != null) {
                return Result.success(existingConversation)
            }

            // Create via API
            try {
                val response = messageApiService.createConversation(
                    CreateConversationRequest(participantId = user2Id, listingId = listingId)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val data = response.body()!!.data
                    val conversation = Conversation(
                        id = data.id,
                        listingId = listingId,
                        participant1Id = user1Id,
                        participant1Name = user1Name,
                        participant2Id = user2Id,
                        participant2Name = user2Name,
                        lastMessage = null,
                        lastMessageTime = null,
                        unreadCount = 0,
                        createdAt = data.createdAt ?: Instant.now().toString(),
                        updatedAt = data.createdAt ?: Instant.now().toString()
                    )
                    messageDao.insertConversation(conversation)
                    Timber.d("Conversation created via API between $user1Name and $user2Name")
                    return Result.success(conversation)
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to create conversation via API, creating locally")
            }

            // Fallback: create locally
            val conversation = Conversation(
                id = UUID.randomUUID().toString(),
                listingId = listingId,
                participant1Id = user1Id,
                participant1Name = user1Name,
                participant2Id = user2Id,
                participant2Name = user2Name,
                lastMessage = null,
                lastMessageTime = null,
                unreadCount = 0,
                createdAt = Instant.now().toString(),
                updatedAt = Instant.now().toString()
            )

            messageDao.insertConversation(conversation)
            Timber.d("Conversation created locally between $user1Name and $user2Name")
            Result.success(conversation)
        } catch (e: Exception) {
            Timber.e(e, "Error creating conversation")
            Result.failure(e)
        }
    }

    fun getMessagesForConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    suspend fun refreshMessages(conversationId: String, currentUserId: String) {
        try {
            val response = messageApiService.getMessages(conversationId)
            if (response.isSuccessful && response.body()?.success == true) {
                val messages = response.body()!!.messages
                messages.forEach { dto ->
                    val message = Message(
                        id = dto.id,
                        conversationId = dto.conversationId,
                        senderId = dto.senderId,
                        senderName = dto.senderName ?: "",
                        receiverId = "", // API doesn't return receiverId directly
                        content = dto.content,
                        messageType = try { MessageType.valueOf(dto.messageType) } catch (_: Exception) { MessageType.TEXT },
                        isRead = dto.isRead,
                        createdAt = dto.createdAt
                    )
                    messageDao.insertMessage(message)
                }
                Timber.d("Refreshed ${messages.size} messages for conversation $conversationId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing messages from API")
        }
    }

    suspend fun sendMessage(
        conversationId: String,
        senderId: String,
        senderName: String,
        receiverId: String,
        content: String,
        messageType: MessageType = MessageType.TEXT
    ): Result<Message> {
        return try {
            val message = Message(
                id = UUID.randomUUID().toString(),
                conversationId = conversationId,
                senderId = senderId,
                senderName = senderName,
                receiverId = receiverId,
                content = content,
                messageType = messageType,
                isRead = false,
                createdAt = Instant.now().toString()
            )

            // Insert locally first for immediate UI update
            messageDao.insertMessage(message)

            val conversation = messageDao.getConversationById(conversationId)
            if (conversation != null) {
                val updatedConversation = conversation.copy(
                    lastMessage = content,
                    lastMessageTime = message.createdAt,
                    unreadCount = conversation.unreadCount + 1,
                    updatedAt = Instant.now().toString()
                )
                messageDao.updateConversation(updatedConversation)
            }

            // Send to API
            try {
                val response = messageApiService.sendMessage(
                    conversationId,
                    SendMessageRequest(content = content, messageType = messageType.name)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    Timber.d("Message sent via API in conversation $conversationId")
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to send message via API, saved locally")
            }

            Timber.d("Message sent in conversation $conversationId")
            Result.success(message)
        } catch (e: Exception) {
            Timber.e(e, "Error sending message")
            Result.failure(e)
        }
    }

    suspend fun markMessagesAsRead(conversationId: String, userId: String) {
        try {
            messageDao.markMessagesAsRead(conversationId, userId)

            val conversation = messageDao.getConversationById(conversationId)
            if (conversation != null) {
                val updatedConversation = conversation.copy(
                    unreadCount = 0,
                    updatedAt = Instant.now().toString()
                )
                messageDao.updateConversation(updatedConversation)
            }

            // Sync to API
            try {
                messageApiService.markConversationRead(conversationId)
            } catch (e: Exception) {
                Timber.w(e, "Failed to mark messages as read on API")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error marking messages as read")
        }
    }

    suspend fun deleteConversation(conversationId: String): Result<Unit> {
        return try {
            messageDao.deleteMessagesForConversation(conversationId)
            messageDao.deleteConversation(conversationId)
            Timber.d("Conversation deleted: $conversationId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting conversation")
            Result.failure(e)
        }
    }
}
