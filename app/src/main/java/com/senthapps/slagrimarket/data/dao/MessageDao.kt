package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.Conversation
import com.senthapps.slagrimarket.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    // Conversations
    @Query("SELECT * FROM conversations WHERE participant1Id = :userId OR participant2Id = :userId ORDER BY updatedAt DESC")
    fun getConversationsForUser(userId: String): Flow<List<Conversation>>
    
    @Query("SELECT * FROM conversations WHERE id = :conversationId")
    suspend fun getConversationById(conversationId: String): Conversation?
    
    @Query("SELECT * FROM conversations WHERE (participant1Id = :user1 AND participant2Id = :user2) OR (participant1Id = :user2 AND participant2Id = :user1) LIMIT 1")
    suspend fun getConversationBetweenUsers(user1: String, user2: String): Conversation?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)
    
    @Update
    suspend fun updateConversation(conversation: Conversation)
    
    @Query("DELETE FROM conversations WHERE id = :conversationId")
    suspend fun deleteConversation(conversationId: String)
    
    // Messages
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>
    
    @Query("SELECT * FROM messages WHERE id = :messageId")
    suspend fun getMessageById(messageId: String): Message?
    
    @Query("SELECT COUNT(*) FROM messages WHERE conversationId = :conversationId AND receiverId = :userId AND isRead = 0")
    suspend fun getUnreadMessageCount(conversationId: String, userId: String): Int
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)
    
    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :conversationId AND receiverId = :userId")
    suspend fun markMessagesAsRead(conversationId: String, userId: String)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
    
    @Query("DELETE FROM messages WHERE conversationId = :conversationId")
    suspend fun deleteMessagesForConversation(conversationId: String)
}
