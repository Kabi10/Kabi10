package com.senthapps.slagrimarket.data.repository

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
    private val messageDao: MessageDao
) {

    fun getConversationsForUser(userId: String): Flow<List<Conversation>> {
        return messageDao.getConversationsForUser(userId)
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
            val existingConversation = messageDao.getConversationBetweenUsers(user1Id, user2Id)
            if (existingConversation != null) {
                return Result.success(existingConversation)
            }

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
            Timber.d("Conversation created between $user1Name and $user2Name")
            Result.success(conversation)
        } catch (e: Exception) {
            Timber.e(e, "Error creating conversation")
            Result.failure(e)
        }
    }

    fun getMessagesForConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesForConversation(conversationId)
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
