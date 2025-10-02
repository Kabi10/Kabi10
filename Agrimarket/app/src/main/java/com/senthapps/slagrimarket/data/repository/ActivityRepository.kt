package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.ActivityApiService
import com.senthapps.slagrimarket.data.api.CreateActivityRequest
import com.senthapps.slagrimarket.data.dao.ActivityDao
import com.senthapps.slagrimarket.data.model.Activity
import com.senthapps.slagrimarket.data.model.ActivityType
import com.senthapps.slagrimarket.data.model.ActivityStatus
import com.senthapps.slagrimarket.data.model.ActivityPriority
import com.senthapps.slagrimarket.data.model.EntityType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for activity operations with offline-first architecture
 * Handles user activities, notifications, and activity tracking
 */
@Singleton
class ActivityRepository @Inject constructor(
    private val activityApiService: ActivityApiService,
    private val activityDao: ActivityDao
) {
    
    // ============================================================================
    // OFFLINE-FIRST DATA ACCESS
    // ============================================================================
    
    /**
     * Get activities for a user - offline-first with background refresh
     */
    fun getActivitiesForUser(
        userId: String,
        forceRefresh: Boolean = false
    ): Flow<Resource<List<Activity>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Always emit cached data first
            val cachedActivities = activityDao.getActivitiesByUser(userId)
            if (cachedActivities.isNotEmpty()) {
                emit(Resource.Success(cachedActivities))
            }
            
            // Check if we need to refresh
            val shouldRefresh = forceRefresh || shouldRefreshActivities(userId)
            
            if (shouldRefresh) {
                try {
                    val response = activityApiService.getActivities(
                        userId = userId,
                        limit = 50,
                        sortBy = "timestamp",
                        sortOrder = "desc"
                    )
                    
                    if (response.isSuccessful) {
                        val networkActivities = response.body()?.activities ?: emptyList()
                        
                        // Update local database
                        activityDao.insertActivities(networkActivities)
                        
                        // Emit updated data
                        emit(Resource.Success(networkActivities))
                    } else {
                        if (cachedActivities.isEmpty()) {
                            emit(Resource.Error("Failed to load activities", null))
                        }
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to refresh activities from network")
                    if (cachedActivities.isEmpty()) {
                        emit(Resource.Error("No internet connection", e))
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting activities")
            emit(Resource.Error("Failed to load activities", e))
        }
    }
    
    /**
     * Get activities with reactive Flow
     */
    fun getActivitiesFlow(userId: String): Flow<List<Activity>> {
        return activityDao.getActivitiesByUserFlow(userId)
    }

    /**
     * Get unread activities
     */
    fun getUnreadActivities(userId: String): Flow<List<Activity>> {
        return activityDao.getUnreadActivitiesByUserFlow(userId)
    }

    /**
     * Get actionable activities
     */
    fun getActionableActivities(userId: String): Flow<List<Activity>> {
        return activityDao.getActionableActivitiesByUserFlow(userId)
    }

    /**
     * Get activities by type
     */
    fun getActivitiesByType(userId: String, activityType: ActivityType): Flow<List<Activity>> {
        return activityDao.getActivitiesByTypeFlow(activityType)
    }

    /**
     * Get activities by priority - using filtered activities
     */
    fun getActivitiesByPriority(userId: String, priority: ActivityPriority): Flow<List<Activity>> {
        return activityDao.getFilteredActivitiesFlow(userId, priority = priority)
    }
    
    /**
     * Get recent activities (last 24 hours)
     */
    fun getRecentActivities(userId: String): Flow<Resource<List<Activity>>> = flow {
        emit(Resource.Loading())
        
        try {
            // Get cached recent activities
            val cachedRecent = activityDao.getRecentActivitiesByUser(userId)
            if (cachedRecent.isNotEmpty()) {
                emit(Resource.Success(cachedRecent))
            }
            
            // Try to fetch fresh recent activities
            try {
                val response = activityApiService.getRecentActivities(userId, limit = 20)
                if (response.isSuccessful) {
                    val recentActivities = response.body()?.activities ?: emptyList()
                    
                    // Update cache
                    activityDao.insertActivities(recentActivities)
                    
                    emit(Resource.Success(recentActivities))
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch recent activities")
                if (cachedRecent.isEmpty()) {
                    emit(Resource.Error("Failed to load recent activities", e))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error("Error loading recent activities", e))
        }
    }
    
    /**
     * Get activity by ID
     */
    suspend fun getActivityById(activityId: String): Resource<Activity> {
        return try {
            // Try local first
            val localActivity = activityDao.getActivityById(activityId)
            if (localActivity != null) {
                Resource.Success(localActivity)
            } else {
                // Try network
                val response = activityApiService.getActivityById(activityId)
                if (response.isSuccessful) {
                    val networkActivity = response.body()
                    if (networkActivity != null) {
                        activityDao.insertActivity(networkActivity)
                        Resource.Success(networkActivity)
                    } else {
                        Resource.Error("Activity not found", null)
                    }
                } else {
                    Resource.Error("Failed to load activity", null)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting activity by ID")
            Resource.Error("Failed to load activity", e)
        }
    }
    
    /**
     * Get unread count
     */
    suspend fun getUnreadCount(userId: String): Resource<Int> {
        return try {
            // Try network first for accurate count
            val response = activityApiService.getUnreadCount(userId)
            if (response.isSuccessful) {
                val count = response.body()?.count ?: 0
                Resource.Success(count)
            } else {
                // Fallback to local count
                val localCount = activityDao.getUnreadCountByUser(userId)
                Resource.Success(localCount)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting unread count")
            // Fallback to local count
            try {
                val localCount = activityDao.getUnreadCountByUser(userId)
                Resource.Success(localCount)
            } catch (localE: Exception) {
                Resource.Error("Failed to get unread count", e)
            }
        }
    }
    
    /**
     * Get actionable count
     */
    suspend fun getActionableCount(userId: String): Resource<Int> {
        return try {
            // Try network first
            val response = activityApiService.getActionableCount(userId)
            if (response.isSuccessful) {
                val count = response.body()?.count ?: 0
                Resource.Success(count)
            } else {
                // Fallback to local count
                val localCount = activityDao.getActionableCountByUser(userId)
                Resource.Success(localCount)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting actionable count")
            try {
                val localCount = activityDao.getActionableCountByUser(userId)
                Resource.Success(localCount)
            } catch (localE: Exception) {
                Resource.Error("Failed to get actionable count", e)
            }
        }
    }
    
    // ============================================================================
    // WRITE OPERATIONS (OFFLINE-FIRST)
    // ============================================================================
    
    /**
     * Create new activity - save locally first, sync later
     */
    suspend fun createActivity(
        userId: String,
        activityType: ActivityType,
        title: String,
        titleTamil: String = "",
        titleSinhala: String = "",
        description: String,
        descriptionTamil: String = "",
        descriptionSinhala: String = "",
        relatedEntityType: EntityType? = null,
        relatedEntityId: String? = null,
        priority: ActivityPriority = ActivityPriority.NORMAL,
        isActionable: Boolean = false,
        expiresAt: String? = null,
        metadata: Map<String, String> = emptyMap()
    ): Resource<Activity> {
        return try {
            // Create activity locally first
            val activity = Activity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                activityType = activityType,
                title = title,
                titleTamil = titleTamil,
                titleSinhala = titleSinhala,
                description = description,
                descriptionTamil = descriptionTamil,
                descriptionSinhala = descriptionSinhala,
                relatedEntityType = relatedEntityType,
                relatedEntityId = relatedEntityId,
                priority = priority,
                status = ActivityStatus.ACTIVE,
                isRead = false,
                isActionable = isActionable,
                timestamp = Instant.now().toString(),
                expiresAt = expiresAt,
                metadata = metadata
            )
            
            // Save locally first (optimistic update)
            activityDao.insertActivity(activity)
            
            // Try to sync with server in background
            try {
                val createRequest = CreateActivityRequest(
                    userId = userId,
                    activityType = activityType,
                    title = title,
                    titleTamil = titleTamil,
                    titleSinhala = titleSinhala,
                    description = description,
                    descriptionTamil = descriptionTamil,
                    descriptionSinhala = descriptionSinhala,
                    relatedEntityType = relatedEntityType?.name,
                    relatedEntityId = relatedEntityId,
                    priority = priority,
                    isActionable = isActionable,
                    expiresAt = expiresAt,
                    metadata = metadata
                )
                
                val response = activityApiService.createActivity(createRequest)
                if (response.isSuccessful) {
                    val serverActivity = response.body()
                    if (serverActivity != null) {
                        // Update with server response
                        activityDao.insertActivity(serverActivity)
                        Resource.Success(serverActivity)
                    } else {
                        Resource.Success(activity)
                    }
                } else {
                    // Local save succeeded, server sync failed
                    Resource.Success(activity)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync activity creation with server")
                // Local save succeeded, server sync failed
                Resource.Success(activity)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error creating activity")
            Resource.Error("Failed to create activity", e)
        }
    }
    
    /**
     * Mark activity as read
     */
    suspend fun markAsRead(activityId: String): Resource<Unit> {
        return try {
            // Update locally first
            activityDao.markAsRead(activityId)
            
            // Try to sync with server
            try {
                val response = activityApiService.markAsRead(activityId)
                if (response.isSuccessful) {
                    Resource.Success(Unit)
                } else {
                    // Local update succeeded
                    Resource.Success(Unit)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync mark as read with server")
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error marking activity as read")
            Resource.Error("Failed to mark as read", e)
        }
    }
    
    /**
     * Mark all activities as read for a user
     */
    suspend fun markAllAsRead(userId: String): Resource<Int> {
        return try {
            // Update locally first
            activityDao.markAllAsReadForUser(userId)
            val markedCount = activityDao.getUnreadCountByUser(userId) // Get count before marking
            
            // Try to sync with server
            try {
                val response = activityApiService.markAllAsRead(userId)
                if (response.isSuccessful) {
                    val serverCount = response.body()?.markedCount ?: markedCount
                    Resource.Success(serverCount)
                } else {
                    Resource.Success(markedCount)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync mark all as read with server")
                Resource.Success(markedCount)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error marking all activities as read")
            Resource.Error("Failed to mark all as read", e)
        }
    }
    
    /**
     * Dismiss activity
     */
    suspend fun dismissActivity(activityId: String): Resource<Unit> {
        return try {
            // Update locally first
            activityDao.updateActivityStatus(activityId, ActivityStatus.DISMISSED)
            
            // Try to sync with server
            try {
                val response = activityApiService.dismissActivity(activityId)
                Resource.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync dismiss with server")
                Resource.Success(Unit)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error dismissing activity")
            Resource.Error("Failed to dismiss activity", e)
        }
    }
    
    // ============================================================================
    // CACHE MANAGEMENT
    // ============================================================================
    
    /**
     * Check if activities need refresh (older than 2 minutes)
     */
    private suspend fun shouldRefreshActivities(userId: String): Boolean {
        // For now, always refresh if forced or if no cached data
        // TODO: Implement proper timestamp checking when DAO method is available
        return true

    }
    
    /**
     * Manual refresh of activities
     */
    suspend fun refreshActivities(userId: String): Resource<Unit> {
        return try {
            val response = activityApiService.getActivities(
                userId = userId,
                limit = 50,
                sortBy = "timestamp",
                sortOrder = "desc"
            )
            
            if (response.isSuccessful) {
                val activities = response.body()?.activities ?: emptyList()
                activityDao.insertActivities(activities)
                Resource.Success(Unit)
            } else {
                Resource.Error("Failed to refresh activities", null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing activities")
            Resource.Error("Network error", e)
        }
    }
    
    /**
     * Clear old activities
     */
    suspend fun clearOldActivities(userId: String, daysToKeep: Int = 30) {
        try {
            // TODO: Implement when DAO method is available
            Timber.d("Clear old activities requested for user: $userId, days: $daysToKeep")
        } catch (e: Exception) {
            Timber.e(e, "Error clearing old activities")
        }
    }
}
