package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.Notification
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

interface NotificationApiService {

    @GET("v1/notifications")
    suspend fun getNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<NotificationsResponse>

    @GET("v1/notifications/unread-count")
    suspend fun getUnreadCount(): Response<NotificationUnreadCountResponse>

    @PATCH("v1/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String
    ): Response<NotificationMarkReadResponse>

    @PATCH("v1/notifications/read-all")
    suspend fun markAllAsRead(): Response<NotificationMarkAllReadResponse>

    @DELETE("v1/notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: String
    ): Response<NotificationDeleteResponse>
}

// Request/Response models

@JsonClass(generateAdapter = true)
data class NotificationsResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "notifications") val notifications: List<NotificationDto>,
    @Json(name = "totalCount") val totalCount: Int,
    @Json(name = "page") val page: Int,
    @Json(name = "totalPages") val totalPages: Int,
    @Json(name = "hasNext") val hasNext: Boolean,
    @Json(name = "hasPrevious") val hasPrevious: Boolean
)

@JsonClass(generateAdapter = true)
data class NotificationDto(
    @Json(name = "id") val id: String,
    @Json(name = "userId") val userId: String,
    @Json(name = "type") val type: String,
    @Json(name = "title") val title: String,
    @Json(name = "message") val message: String?,
    @Json(name = "relatedId") val relatedId: String?,
    @Json(name = "isRead") val isRead: Boolean,
    @Json(name = "createdAt") val createdAt: String
)

@JsonClass(generateAdapter = true)
data class NotificationMarkReadResponse(
    @Json(name = "success") val success: Boolean
)

@JsonClass(generateAdapter = true)
data class NotificationMarkAllReadResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "markedCount") val markedCount: Int
)

@JsonClass(generateAdapter = true)
data class NotificationUnreadCountResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "count") val count: Int
)

@JsonClass(generateAdapter = true)
data class NotificationDeleteResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "message") val message: String?
)
