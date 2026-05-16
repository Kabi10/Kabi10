package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.Message
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

interface MessageApiService {

    @GET("v1/messages/conversations")
    suspend fun getConversations(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<ConversationsResponse>

    @GET("v1/messages/conversations/{id}")
    suspend fun getConversation(
        @Path("id") conversationId: String
    ): Response<ConversationDetailResponse>

    @POST("v1/messages/conversations")
    suspend fun createConversation(
        @Body request: CreateConversationRequest
    ): Response<CreateConversationResponse>

    @GET("v1/messages/conversations/{id}/messages")
    suspend fun getMessages(
        @Path("id") conversationId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<MessagesResponse>

    @POST("v1/messages/conversations/{id}/messages")
    suspend fun sendMessage(
        @Path("id") conversationId: String,
        @Body request: SendMessageRequest
    ): Response<SendMessageResponse>

    @PATCH("v1/messages/conversations/{id}/read")
    suspend fun markConversationRead(
        @Path("id") conversationId: String
    ): Response<MessageMarkReadResponse>

    @GET("v1/messages/unread-count")
    suspend fun getUnreadCount(): Response<MessageUnreadCountResponse>
}

// Request/Response models

@JsonClass(generateAdapter = true)
data class ConversationsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "conversations") val conversations: List<ConversationDto>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNext") val hasNext: Boolean,
    @Json(name = "hasPrevious") val hasPrevious: Boolean
)

@JsonClass(generateAdapter = true)
data class ConversationDto(
    @Json(name = "id") val id: String,
    @Json(name = "otherParticipant") val otherParticipant: ParticipantDto,
    @Json(name = "listingId") val listingId: String?,
    @Json(name = "listingCropType") val listingCropType: String?,
    @Json(name = "lastMessageText") val lastMessageText: String?,
    @Json(name = "lastMessageAt") val lastMessageAt: String?,
    @Json(name = "unreadCount") val unreadCount: Int,
    @Json(name = "createdAt") val createdAt: String,
    @Json(name = "updatedAt") val updatedAt: String
)

@JsonClass(generateAdapter = true)
data class ParticipantDto(
    @Json(name = "id") val id: String,
    @Json(name = "name") val name: String?,
    @Json(name = "phone") val phone: String?
)

@JsonClass(generateAdapter = true)
data class ConversationDetailResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: ConversationDto
)

@JsonClass(generateAdapter = true)
data class CreateConversationRequest(
    @Json(name = "participantId") val participantId: String,
    @Json(name = "listingId") val listingId: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateConversationResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: CreateConversationData
)

@JsonClass(generateAdapter = true)
data class CreateConversationData(
    @Json(name = "id") val id: String,
    @Json(name = "created") val created: Boolean,
    @Json(name = "createdAt") val createdAt: String? = null
)

@JsonClass(generateAdapter = true)
data class MessagesResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "messages") val messages: List<MessageDto>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNext") val hasNext: Boolean,
    @Json(name = "hasPrevious") val hasPrevious: Boolean
)

@JsonClass(generateAdapter = true)
data class MessageDto(
    @Json(name = "id") val id: String,
    @Json(name = "conversationId") val conversationId: String,
    @Json(name = "senderId") val senderId: String,
    @Json(name = "senderName") val senderName: String?,
    @Json(name = "content") val content: String,
    @Json(name = "messageType") val messageType: String,
    @Json(name = "isRead") val isRead: Boolean,
    @Json(name = "createdAt") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class SendMessageRequest(
    @Json(name = "content") val content: String,
    @Json(name = "messageType") val messageType: String = "TEXT"
)

@JsonClass(generateAdapter = true)
data class SendMessageResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "data") val data: MessageDto
)

@JsonClass(generateAdapter = true)
data class MessageMarkReadResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "markedCount") val markedCount: Int
)

@JsonClass(generateAdapter = true)
data class MessageUnreadCountResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "count") val count: Int
)
