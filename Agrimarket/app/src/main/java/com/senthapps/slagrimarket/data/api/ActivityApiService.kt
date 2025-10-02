package com.senthapps.slagrimarket.data.api

import com.senthapps.slagrimarket.data.model.Activity
import com.senthapps.slagrimarket.data.model.ActivityType
import com.senthapps.slagrimarket.data.model.ActivityStatus
import com.senthapps.slagrimarket.data.model.ActivityPriority
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for activity operations
 * Handles user activities, notifications, and activity tracking
 */
interface ActivityApiService {
    
    /**
     * Get activities for a user with filtering options
     */
    @GET("v1/activities")
    suspend fun getActivities(
        @Query("userId") userId: String? = null,
        @Query("activityType") activityType: String? = null,
        @Query("status") status: String? = null,
        @Query("priority") priority: String? = null,
        @Query("isRead") isRead: Boolean? = null,
        @Query("isActionable") isActionable: Boolean? = null,
        @Query("fromDate") fromDate: String? = null,
        @Query("toDate") toDate: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("sortBy") sortBy: String = "timestamp",
        @Query("sortOrder") sortOrder: String = "desc",
        @Query("language") language: String = "en"
    ): Response<ActivitiesResponse>
    
    /**
     * Get activity by ID
     */
    @GET("v1/activities/{id}")
    suspend fun getActivityById(@Path("id") activityId: String): Response<Activity>
    
    /**
     * Get unread activities count for a user
     */
    @GET("v1/activities/unread-count")
    suspend fun getUnreadCount(
        @Query("userId") userId: String
    ): Response<UnreadCountResponse>
    
    /**
     * Get actionable activities count for a user
     */
    @GET("v1/activities/actionable-count")
    suspend fun getActionableCount(
        @Query("userId") userId: String
    ): Response<ActionableCountResponse>
    
    /**
     * Get activity summary for a user
     */
    @GET("v1/activities/summary")
    suspend fun getActivitySummary(
        @Query("userId") userId: String,
        @Query("timeframe") timeframe: String = "24h"
    ): Response<ActivitySummaryResponse>
    
    /**
     * Get recent activities for a user
     */
    @GET("v1/activities/recent")
    suspend fun getRecentActivities(
        @Query("userId") userId: String,
        @Query("limit") limit: Int = 10
    ): Response<ActivitiesResponse>
    
    /**
     * Create new activity
     */
    @POST("v1/activities")
    suspend fun createActivity(@Body activity: CreateActivityRequest): Response<Activity>
    
    /**
     * Mark activity as read
     */
    @PATCH("v1/activities/{id}/read")
    suspend fun markAsRead(@Path("id") activityId: String): Response<MarkReadResponse>
    
    /**
     * Mark all activities as read for a user
     */
    @PATCH("v1/activities/mark-all-read")
    suspend fun markAllAsRead(@Query("userId") userId: String): Response<MarkAllReadResponse>
    
    /**
     * Dismiss activity
     */
    @PATCH("v1/activities/{id}/dismiss")
    suspend fun dismissActivity(@Path("id") activityId: String): Response<DismissResponse>
    
    /**
     * Archive activity
     */
    @PATCH("v1/activities/{id}/archive")
    suspend fun archiveActivity(@Path("id") activityId: String): Response<ArchiveResponse>
    
    /**
     * Delete activity
     */
    @DELETE("v1/activities/{id}")
    suspend fun deleteActivity(@Path("id") activityId: String): Response<DeleteResponse>
}

// ============================================================================
// REQUEST/RESPONSE MODELS
// ============================================================================

/**
 * Response model for activities list
 */
@JsonClass(generateAdapter = true)
data class ActivitiesResponse(
    @Json(name = "activities")
    val activities: List<Activity>,
    
    @Json(name = "totalCount")
    val totalCount: Int,
    
    @Json(name = "page")
    val page: Int,
    
    @Json(name = "totalPages")
    val totalPages: Int,
    
    @Json(name = "hasNext")
    val hasNext: Boolean,
    
    @Json(name = "hasPrevious")
    val hasPrevious: Boolean,
    
    @Json(name = "lastUpdated")
    val lastUpdated: String
)

/**
 * Request model for creating activity
 */
@JsonClass(generateAdapter = true)
data class CreateActivityRequest(
    @Json(name = "userId")
    val userId: String,
    
    @Json(name = "activityType")
    val activityType: ActivityType,
    
    @Json(name = "title")
    val title: String,
    
    @Json(name = "titleTamil")
    val titleTamil: String = "",
    
    @Json(name = "titleSinhala")
    val titleSinhala: String = "",
    
    @Json(name = "description")
    val description: String,
    
    @Json(name = "descriptionTamil")
    val descriptionTamil: String = "",
    
    @Json(name = "descriptionSinhala")
    val descriptionSinhala: String = "",
    
    @Json(name = "relatedEntityType")
    val relatedEntityType: String? = null,
    
    @Json(name = "relatedEntityId")
    val relatedEntityId: String? = null,
    
    @Json(name = "priority")
    val priority: ActivityPriority = ActivityPriority.NORMAL,
    
    @Json(name = "isActionable")
    val isActionable: Boolean = false,
    
    @Json(name = "expiresAt")
    val expiresAt: String? = null,
    
    @Json(name = "metadata")
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Response model for unread count
 */
@JsonClass(generateAdapter = true)
data class UnreadCountResponse(
    @Json(name = "count")
    val count: Int
)

/**
 * Response model for actionable count
 */
@JsonClass(generateAdapter = true)
data class ActionableCountResponse(
    @Json(name = "count")
    val count: Int
)

/**
 * Response model for activity summary
 */
@JsonClass(generateAdapter = true)
data class ActivitySummaryResponse(
    @Json(name = "totalActivities")
    val totalActivities: Int,
    
    @Json(name = "unreadCount")
    val unreadCount: Int,
    
    @Json(name = "actionableCount")
    val actionableCount: Int,
    
    @Json(name = "typeDistribution")
    val typeDistribution: Map<String, Int>,
    
    @Json(name = "priorityDistribution")
    val priorityDistribution: Map<String, Int>,
    
    @Json(name = "recentActivities")
    val recentActivities: List<Activity>
)

/**
 * Response model for mark as read
 */
@JsonClass(generateAdapter = true)
data class MarkReadResponse(
    @Json(name = "success")
    val success: Boolean
)

/**
 * Response model for mark all as read
 */
@JsonClass(generateAdapter = true)
data class MarkAllReadResponse(
    @Json(name = "success")
    val success: Boolean,
    
    @Json(name = "markedCount")
    val markedCount: Int
)

/**
 * Response model for dismiss activity
 */
@JsonClass(generateAdapter = true)
data class DismissResponse(
    @Json(name = "success")
    val success: Boolean
)

/**
 * Response model for archive activity
 */
@JsonClass(generateAdapter = true)
data class ArchiveResponse(
    @Json(name = "success")
    val success: Boolean
)
