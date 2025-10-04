package com.senthapps.slagrimarket.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    @Json(name = "id")
    val id: String,
    
    @Json(name = "conversationId")
    val conversationId: String,
    
    @Json(name = "senderId")
    val senderId: String,
    
    @Json(name = "senderName")
    val senderName: String,
    
    @Json(name = "receiverId")
    val receiverId: String,
    
    @Json(name = "content")
    val content: String,
    
    @Json(name = "messageType")
    val messageType: MessageType = MessageType.TEXT,
    
    @Json(name = "isRead")
    val isRead: Boolean = false,
    
    @Json(name = "createdAt")
    val createdAt: String
)

@JsonClass(generateAdapter = true)
@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey
    @Json(name = "id")
    val id: String,
    
    @Json(name = "listingId")
    val listingId: String?,
    
    @Json(name = "participant1Id")
    val participant1Id: String,
    
    @Json(name = "participant1Name")
    val participant1Name: String,
    
    @Json(name = "participant2Id")
    val participant2Id: String,
    
    @Json(name = "participant2Name")
    val participant2Name: String,
    
    @Json(name = "lastMessage")
    val lastMessage: String?,
    
    @Json(name = "lastMessageTime")
    val lastMessageTime: String?,
    
    @Json(name = "unreadCount")
    val unreadCount: Int = 0,
    
    @Json(name = "createdAt")
    val createdAt: String,
    
    @Json(name = "updatedAt")
    val updatedAt: String
)

enum class MessageType {
    @Json(name = "TEXT")
    TEXT,
    
    @Json(name = "IMAGE")
    IMAGE,
    
    @Json(name = "SYSTEM")
    SYSTEM
}
