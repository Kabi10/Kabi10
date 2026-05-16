package com.senthapps.slagrimarket.data.dao

import androidx.room.*
import com.senthapps.slagrimarket.data.model.Activity
import com.senthapps.slagrimarket.data.model.ActivityType
import com.senthapps.slagrimarket.data.model.ActivityStatus
import com.senthapps.slagrimarket.data.model.ActivityPriority
import com.senthapps.slagrimarket.data.model.EntityType
import kotlinx.coroutines.flow.Flow

/**
 * Enhanced ActivityDao with reactive Flow-based queries for activity tracking
 */
@Dao
interface ActivityDao {
    
    // ============================================================================
    // BASIC CRUD OPERATIONS
    // ============================================================================
    
    @Query("SELECT * FROM activities WHERE id = :activityId")
    suspend fun getActivityById(activityId: String): Activity?
    
    @Query("SELECT * FROM activities WHERE id = :activityId")
    fun getActivityByIdFlow(activityId: String): Flow<Activity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<Activity>)
    
    @Update
    suspend fun updateActivity(activity: Activity)
    
    @Delete
    suspend fun deleteActivity(activity: Activity)
    
    @Query("DELETE FROM activities WHERE id = :activityId")
    suspend fun deleteActivityById(activityId: String)
    
    @Query("DELETE FROM activities")
    suspend fun deleteAllActivities()
    
    // ============================================================================
    // USER ACTIVITY QUERIES
    // ============================================================================
    
    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getActivitiesByUser(userId: String): List<Activity>
    
    @Query("SELECT * FROM activities WHERE userId = :userId ORDER BY timestamp DESC")
    fun getActivitiesByUserFlow(userId: String): Flow<List<Activity>>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND status = 'ACTIVE'
        ORDER BY 
            CASE priority 
                WHEN 'URGENT' THEN 4
                WHEN 'HIGH' THEN 3
                WHEN 'NORMAL' THEN 2
                WHEN 'LOW' THEN 1
            END DESC,
            timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getActiveActivitiesByUser(
        userId: String,
        limit: Int = 20,
        offset: Int = 0
    ): List<Activity>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND status = 'ACTIVE'
        ORDER BY 
            CASE priority 
                WHEN 'URGENT' THEN 4
                WHEN 'HIGH' THEN 3
                WHEN 'NORMAL' THEN 2
                WHEN 'LOW' THEN 1
            END DESC,
            timestamp DESC
    """)
    fun getActiveActivitiesByUserFlow(userId: String): Flow<List<Activity>>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND isRead = 0 
        AND status = 'ACTIVE'
        ORDER BY timestamp DESC
    """)
    suspend fun getUnreadActivitiesByUser(userId: String): List<Activity>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND isRead = 0 
        AND status = 'ACTIVE'
        ORDER BY timestamp DESC
    """)
    fun getUnreadActivitiesByUserFlow(userId: String): Flow<List<Activity>>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND isActionable = 1 
        AND isRead = 0 
        AND status = 'ACTIVE'
        AND (expiresAt IS NULL OR datetime(expiresAt) > datetime('now'))
        ORDER BY 
            CASE priority 
                WHEN 'URGENT' THEN 4
                WHEN 'HIGH' THEN 3
                WHEN 'NORMAL' THEN 2
                WHEN 'LOW' THEN 1
            END DESC,
            timestamp DESC
    """)
    suspend fun getActionableActivitiesByUser(userId: String): List<Activity>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND isActionable = 1 
        AND isRead = 0 
        AND status = 'ACTIVE'
        AND (expiresAt IS NULL OR datetime(expiresAt) > datetime('now'))
        ORDER BY 
            CASE priority 
                WHEN 'URGENT' THEN 4
                WHEN 'HIGH' THEN 3
                WHEN 'NORMAL' THEN 2
                WHEN 'LOW' THEN 1
            END DESC,
            timestamp DESC
    """)
    fun getActionableActivitiesByUserFlow(userId: String): Flow<List<Activity>>
    
    // ============================================================================
    // FILTERING AND SEARCH QUERIES
    // ============================================================================
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId
        AND (:activityType IS NULL OR activityType = :activityType)
        AND (:status IS NULL OR status = :status)
        AND (:priority IS NULL OR priority = :priority)
        AND (:entityType IS NULL OR relatedEntityType = :entityType)
        AND (:isRead IS NULL OR isRead = :isRead)
        AND (:isActionable IS NULL OR isActionable = :isActionable)
        ORDER BY timestamp DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getFilteredActivities(
        userId: String,
        activityType: ActivityType? = null,
        status: ActivityStatus? = null,
        priority: ActivityPriority? = null,
        entityType: EntityType? = null,
        isRead: Boolean? = null,
        isActionable: Boolean? = null,
        limit: Int = 20,
        offset: Int = 0
    ): List<Activity>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId
        AND (:activityType IS NULL OR activityType = :activityType)
        AND (:status IS NULL OR status = :status)
        AND (:priority IS NULL OR priority = :priority)
        ORDER BY timestamp DESC
    """)
    fun getFilteredActivitiesFlow(
        userId: String,
        activityType: ActivityType? = null,
        status: ActivityStatus? = null,
        priority: ActivityPriority? = null
    ): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE activityType = :activityType ORDER BY timestamp DESC")
    suspend fun getActivitiesByType(activityType: ActivityType): List<Activity>
    
    @Query("SELECT * FROM activities WHERE activityType = :activityType ORDER BY timestamp DESC")
    fun getActivitiesByTypeFlow(activityType: ActivityType): Flow<List<Activity>>
    
    @Query("SELECT * FROM activities WHERE relatedEntityId = :entityId AND relatedEntityType = :entityType ORDER BY timestamp DESC")
    suspend fun getActivitiesForEntity(entityId: String, entityType: EntityType): List<Activity>
    
    @Query("SELECT * FROM activities WHERE relatedEntityId = :entityId AND relatedEntityType = :entityType ORDER BY timestamp DESC")
    fun getActivitiesForEntityFlow(entityId: String, entityType: EntityType): Flow<List<Activity>>
    
    // ============================================================================
    // RECENT AND TIME-BASED QUERIES
    // ============================================================================
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND datetime(timestamp) > datetime('now', '-24 hours')
        AND status = 'ACTIVE'
        ORDER BY timestamp DESC
    """)
    suspend fun getRecentActivitiesByUser(userId: String): List<Activity>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND datetime(timestamp) > datetime('now', '-24 hours')
        AND status = 'ACTIVE'
        ORDER BY timestamp DESC
    """)
    fun getRecentActivitiesByUserFlow(userId: String): Flow<List<Activity>>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND datetime(timestamp) BETWEEN datetime('now', 'start of day') AND datetime('now', 'start of day', '+1 day')
        AND status = 'ACTIVE'
        ORDER BY timestamp DESC
    """)
    suspend fun getTodayActivitiesByUser(userId: String): List<Activity>
    
    @Query("""
        SELECT * FROM activities 
        WHERE userId = :userId 
        AND datetime(timestamp) BETWEEN datetime('now', 'start of day') AND datetime('now', 'start of day', '+1 day')
        AND status = 'ACTIVE'
        ORDER BY timestamp DESC
    """)
    fun getTodayActivitiesByUserFlow(userId: String): Flow<List<Activity>>
    
    // ============================================================================
    // UPDATE OPERATIONS
    // ============================================================================
    
    @Query("UPDATE activities SET isRead = 1 WHERE id = :activityId")
    suspend fun markAsRead(activityId: String)
    
    @Query("UPDATE activities SET isRead = 1 WHERE userId = :userId AND isRead = 0")
    suspend fun markAllAsReadForUser(userId: String)
    
    @Query("UPDATE activities SET status = :status WHERE id = :activityId")
    suspend fun updateActivityStatus(activityId: String, status: ActivityStatus)
    
    @Query("UPDATE activities SET status = 'ARCHIVED' WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun archiveAllActivitiesForUser(userId: String)
    
    @Query("UPDATE activities SET status = 'DISMISSED' WHERE id = :activityId")
    suspend fun dismissActivity(activityId: String)
    
    // ============================================================================
    // STATISTICS AND ANALYTICS QUERIES
    // ============================================================================
    
    @Query("SELECT COUNT(*) FROM activities WHERE userId = :userId AND isRead = 0 AND status = 'ACTIVE'")
    suspend fun getUnreadCountByUser(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM activities WHERE userId = :userId AND isRead = 0 AND status = 'ACTIVE'")
    fun getUnreadCountByUserFlow(userId: String): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM activities WHERE userId = :userId AND isActionable = 1 AND isRead = 0 AND status = 'ACTIVE'")
    suspend fun getActionableCountByUser(userId: String): Int
    
    @Query("SELECT COUNT(*) FROM activities WHERE userId = :userId AND isActionable = 1 AND isRead = 0 AND status = 'ACTIVE'")
    fun getActionableCountByUserFlow(userId: String): Flow<Int>
    
    @Query("""
        SELECT 
            activityType,
            COUNT(*) as count,
            COUNT(CASE WHEN isRead = 0 THEN 1 END) as unreadCount
        FROM activities 
        WHERE userId = :userId AND status = 'ACTIVE'
        GROUP BY activityType
        ORDER BY count DESC
    """)
    suspend fun getActivityTypeStatsByUser(userId: String): List<ActivityTypeStats>
    
    @Query("""
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN isRead = 0 THEN 1 END) as unread,
            COUNT(CASE WHEN isActionable = 1 AND isRead = 0 THEN 1 END) as actionable,
            COUNT(CASE WHEN priority = 'URGENT' THEN 1 END) as urgent,
            COUNT(CASE WHEN priority = 'HIGH' THEN 1 END) as high
        FROM activities 
        WHERE userId = :userId AND status = 'ACTIVE'
    """)
    suspend fun getActivitySummaryByUser(userId: String): ActivitySummary
    
    @Query("""
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN isRead = 0 THEN 1 END) as unread,
            COUNT(CASE WHEN isActionable = 1 AND isRead = 0 THEN 1 END) as actionable,
            COUNT(CASE WHEN priority = 'URGENT' THEN 1 END) as urgent,
            COUNT(CASE WHEN priority = 'HIGH' THEN 1 END) as high
        FROM activities 
        WHERE userId = :userId AND status = 'ACTIVE'
    """)
    fun getActivitySummaryByUserFlow(userId: String): Flow<ActivitySummary>
    
    // ============================================================================
    // CLEANUP QUERIES
    // ============================================================================
    
    @Query("DELETE FROM activities WHERE status = 'DISMISSED' AND datetime(timestamp) < datetime('now', '-' || :retentionDays || ' days')")
    suspend fun cleanupDismissedActivities(retentionDays: Int = 30)

    @Query("DELETE FROM activities WHERE status = 'ARCHIVED' AND datetime(timestamp) < datetime('now', '-' || :retentionDays || ' days')")
    suspend fun cleanupArchivedActivities(retentionDays: Int = 90)
    
    @Query("UPDATE activities SET status = 'ARCHIVED' WHERE datetime(expiresAt) < datetime('now') AND status = 'ACTIVE'")
    suspend fun archiveExpiredActivities()
    
    @Query("SELECT COUNT(*) FROM activities WHERE datetime(timestamp) > datetime('now', '-1 hour')")
    suspend fun getRecentActivityCount(): Int

    @Query("SELECT MAX(datetime(timestamp)) FROM activities WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun getLastUpdateTimeForUser(userId: String): String?
}

/**
 * Data class for activity type statistics
 */
data class ActivityTypeStats(
    val activityType: ActivityType,
    val count: Int,
    val unreadCount: Int
)

/**
 * Data class for activity summary
 */
data class ActivitySummary(
    val total: Int,
    val unread: Int,
    val actionable: Int,
    val urgent: Int,
    val high: Int
)
