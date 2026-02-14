package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.NotificationApiService
import com.senthapps.slagrimarket.data.dao.NotificationDao
import com.senthapps.slagrimarket.data.model.Notification
import com.senthapps.slagrimarket.data.model.NotificationType
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepository @Inject constructor(
    private val notificationDao: NotificationDao,
    private val notificationApiService: NotificationApiService
) {

    fun getNotificationsByUser(userId: String): Flow<List<Notification>> {
        return notificationDao.getNotificationsByUser(userId)
    }

    fun getUnreadNotifications(userId: String): Flow<List<Notification>> {
        return notificationDao.getUnreadNotifications(userId)
    }

    fun getUnreadCount(userId: String): Flow<Int> {
        return notificationDao.getUnreadCount(userId)
    }

    suspend fun refreshNotifications(userId: String) {
        try {
            val response = notificationApiService.getNotifications()
            if (response.isSuccessful && response.body()?.success == true) {
                val notifications = response.body()!!.notifications
                notifications.forEach { dto ->
                    val notification = Notification(
                        id = dto.id,
                        userId = dto.userId,
                        type = try { NotificationType.valueOf(dto.type) } catch (_: Exception) { NotificationType.SYSTEM },
                        title = dto.title,
                        message = dto.message ?: "",
                        relatedId = dto.relatedId,
                        isRead = dto.isRead,
                        createdAt = dto.createdAt
                    )
                    notificationDao.insertNotification(notification)
                }
                Timber.d("Refreshed ${notifications.size} notifications from API")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error refreshing notifications from API")
        }
    }

    suspend fun getUnreadCountFromApi(): Int {
        return try {
            val response = notificationApiService.getUnreadCount()
            if (response.isSuccessful) {
                response.body()?.count ?: 0
            } else {
                0
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting unread count from API")
            0
        }
    }

    suspend fun createNotification(
        userId: String,
        type: NotificationType,
        title: String,
        message: String,
        relatedId: String? = null
    ): Result<Notification> {
        return try {
            val notification = Notification(
                id = UUID.randomUUID().toString(),
                userId = userId,
                type = type,
                title = title,
                message = message,
                relatedId = relatedId,
                isRead = false,
                createdAt = Instant.now().toString()
            )

            notificationDao.insertNotification(notification)
            Timber.d("Notification created: $type for user $userId")
            Result.success(notification)
        } catch (e: Exception) {
            Timber.e(e, "Error creating notification")
            Result.failure(e)
        }
    }

    suspend fun markAsRead(notificationId: String) {
        try {
            notificationDao.markAsRead(notificationId)
            // Sync to API
            try {
                notificationApiService.markAsRead(notificationId)
            } catch (e: Exception) {
                Timber.w(e, "Failed to mark notification as read on API")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error marking notification as read")
        }
    }

    suspend fun markAllAsRead(userId: String) {
        try {
            notificationDao.markAllAsRead(userId)
            // Sync to API
            try {
                notificationApiService.markAllAsRead()
            } catch (e: Exception) {
                Timber.w(e, "Failed to mark all notifications as read on API")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error marking all notifications as read")
        }
    }

    suspend fun deleteNotification(notificationId: String) {
        try {
            notificationDao.deleteNotification(notificationId)
            // Sync to API
            try {
                notificationApiService.deleteNotification(notificationId)
            } catch (e: Exception) {
                Timber.w(e, "Failed to delete notification from API")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error deleting notification")
        }
    }

    suspend fun deleteAllForUser(userId: String) {
        try {
            notificationDao.deleteAllForUser(userId)
        } catch (e: Exception) {
            Timber.e(e, "Error deleting all notifications for user")
        }
    }

    suspend fun cleanupOldNotifications(daysOld: Int = 30) {
        try {
            val cutoffDate = Instant.now().minus(daysOld.toLong(), ChronoUnit.DAYS).toString()
            notificationDao.deleteOldNotifications(cutoffDate)
            Timber.d("Cleaned up notifications older than $daysOld days")
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up old notifications")
        }
    }

    // Helper methods for creating specific notification types
    suspend fun notifyOrderReceived(farmerId: String, transactionId: String, buyerName: String) {
        createNotification(
            userId = farmerId,
            type = NotificationType.ORDER_RECEIVED,
            title = "New Order Received",
            message = "$buyerName placed an order",
            relatedId = transactionId
        )
    }

    suspend fun notifyOrderConfirmed(buyerId: String, transactionId: String) {
        createNotification(
            userId = buyerId,
            type = NotificationType.ORDER_CONFIRMED,
            title = "Order Confirmed",
            message = "Your order has been confirmed by the farmer",
            relatedId = transactionId
        )
    }

    suspend fun notifyOrderReady(buyerId: String, transactionId: String) {
        createNotification(
            userId = buyerId,
            type = NotificationType.ORDER_READY,
            title = "Order Ready for Pickup",
            message = "Your order is ready for pickup",
            relatedId = transactionId
        )
    }

    suspend fun notifyOrderCompleted(farmerId: String, transactionId: String) {
        createNotification(
            userId = farmerId,
            type = NotificationType.ORDER_COMPLETED,
            title = "Order Completed",
            message = "Order has been completed successfully",
            relatedId = transactionId
        )
    }
}
