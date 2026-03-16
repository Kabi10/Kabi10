package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.NotificationApiService
import com.senthapps.slagrimarket.data.dao.NotificationDao
import com.senthapps.slagrimarket.data.model.Notification
import com.senthapps.slagrimarket.data.model.NotificationType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationRepositoryTest {

    private lateinit var notificationDao: NotificationDao
    private lateinit var notificationApiService: NotificationApiService
    private lateinit var repository: NotificationRepository

    private val mockNotifications = listOf(
        Notification(
            id = "notif1",
            userId = "user1",
            type = NotificationType.ORDER_RECEIVED,
            title = "New Order",
            message = "You have a new order",
            isRead = false,
            createdAt = "2025-11-20T10:00:00Z"
        ),
        Notification(
            id = "notif2",
            userId = "user1",
            type = NotificationType.SYSTEM,
            title = "System Update",
            message = "App updated",
            isRead = true,
            createdAt = "2025-11-19T10:00:00Z"
        )
    )

    @Before
    fun setup() {
        notificationDao = mockk(relaxed = true)
        notificationApiService = mockk(relaxed = true)
        repository = NotificationRepository(notificationDao, notificationApiService)
    }

    @Test
    fun `createNotification should insert into DAO and return success`() = runTest {
        coEvery { notificationDao.insertNotification(any()) } just Runs

        val result = repository.createNotification(
            userId = "user1",
            type = NotificationType.ORDER_RECEIVED,
            title = "New Order",
            message = "Buyer placed an order",
            relatedId = "tx1"
        )

        assertTrue(result.isSuccess)
        val notif = result.getOrNull()
        assertNotNull(notif)
        assertEquals("user1", notif?.userId)
        assertEquals(NotificationType.ORDER_RECEIVED, notif?.type)
        assertEquals("New Order", notif?.title)
        assertFalse(notif?.isRead ?: true)
        coVerify(exactly = 1) { notificationDao.insertNotification(any()) }
    }

    @Test
    fun `createNotification should return failure when DAO throws`() = runTest {
        coEvery { notificationDao.insertNotification(any()) } throws RuntimeException("DB error")

        val result = repository.createNotification(
            userId = "user1",
            type = NotificationType.SYSTEM,
            title = "Test",
            message = "Test message"
        )

        assertTrue(result.isFailure)
    }

    @Test
    fun `markAsRead should update DAO and attempt API sync`() = runTest {
        coEvery { notificationDao.markAsRead("notif1") } just Runs
        coEvery { notificationApiService.markAsRead("notif1") } returns mockk(relaxed = true)

        repository.markAsRead("notif1")

        coVerify { notificationDao.markAsRead("notif1") }
        coVerify { notificationApiService.markAsRead("notif1") }
    }

    @Test
    fun `markAsRead should still succeed when API throws`() = runTest {
        coEvery { notificationDao.markAsRead("notif1") } just Runs
        coEvery { notificationApiService.markAsRead("notif1") } throws Exception("Network error")

        // Should not throw
        repository.markAsRead("notif1")

        coVerify { notificationDao.markAsRead("notif1") }
    }

    @Test
    fun `markAllAsRead should update DAO with userId`() = runTest {
        coEvery { notificationDao.markAllAsRead("user1") } just Runs

        repository.markAllAsRead("user1")

        coVerify { notificationDao.markAllAsRead("user1") }
    }

    @Test
    fun `deleteNotification should remove from DAO`() = runTest {
        coEvery { notificationDao.deleteNotification("notif1") } just Runs

        repository.deleteNotification("notif1")

        coVerify { notificationDao.deleteNotification("notif1") }
    }

    @Test
    fun `deleteNotification should silently handle API exception`() = runTest {
        coEvery { notificationDao.deleteNotification("notif1") } just Runs
        coEvery { notificationApiService.deleteNotification("notif1") } throws Exception("Network error")

        // Should not throw
        repository.deleteNotification("notif1")

        coVerify { notificationDao.deleteNotification("notif1") }
    }

    @Test
    fun `getNotificationsByUser should delegate to DAO`() = runTest {
        coEvery { notificationDao.getNotificationsByUser("user1") } returns flowOf(mockNotifications)

        val result = repository.getNotificationsByUser("user1").first()

        assertEquals(2, result.size)
        assertEquals("notif1", result[0].id)
    }

    @Test
    fun `getUnreadCount should delegate to DAO`() = runTest {
        coEvery { notificationDao.getUnreadCount("user1") } returns flowOf(3)

        val count = repository.getUnreadCount("user1").first()

        assertEquals(3, count)
    }

    @Test
    fun `notifyOrderReceived should create ORDER_RECEIVED notification for farmer`() = runTest {
        val capturedNotif = slot<Notification>()
        coEvery { notificationDao.insertNotification(capture(capturedNotif)) } just Runs

        repository.notifyOrderReceived("farmer1", "tx1", "John Buyer")

        assertEquals("farmer1", capturedNotif.captured.userId)
        assertEquals(NotificationType.ORDER_RECEIVED, capturedNotif.captured.type)
        assertTrue(capturedNotif.captured.message.contains("John Buyer"))
        assertEquals("tx1", capturedNotif.captured.relatedId)
    }

    @Test
    fun `notifyOrderConfirmed should create ORDER_CONFIRMED notification for buyer`() = runTest {
        val capturedNotif = slot<Notification>()
        coEvery { notificationDao.insertNotification(capture(capturedNotif)) } just Runs

        repository.notifyOrderConfirmed("buyer1", "tx1")

        assertEquals("buyer1", capturedNotif.captured.userId)
        assertEquals(NotificationType.ORDER_CONFIRMED, capturedNotif.captured.type)
        assertEquals("tx1", capturedNotif.captured.relatedId)
    }

    @Test
    fun `cleanupOldNotifications should call DAO with cutoff date`() = runTest {
        coEvery { notificationDao.deleteOldNotifications(any()) } just Runs

        repository.cleanupOldNotifications(daysOld = 30)

        coVerify { notificationDao.deleteOldNotifications(any()) }
    }
}
