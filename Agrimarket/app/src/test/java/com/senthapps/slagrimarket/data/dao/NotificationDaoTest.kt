package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senthapps.slagrimarket.data.model.Notification
import com.senthapps.slagrimarket.data.model.NotificationType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@Database(entities = [Notification::class], version = 1, exportSchema = false)
abstract class NotificationTestDatabase : RoomDatabase() {
    abstract fun notificationDao(): NotificationDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class NotificationDaoTest {

    private lateinit var db: NotificationTestDatabase
    private lateinit var dao: NotificationDao

    private val userId = "user-001"
    private val ts = "2026-01-01T10:00:00Z"

    private fun makeNotification(
        id: String,
        userId: String = this.userId,
        type: NotificationType = NotificationType.ORDER_RECEIVED,
        title: String = "Test",
        message: String = "Test message",
        isRead: Boolean = false,
        createdAt: String = ts
    ) = Notification(
        id = id,
        userId = userId,
        type = type,
        title = title,
        message = message,
        isRead = isRead,
        createdAt = createdAt
    )

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, NotificationTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.notificationDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // insertNotification / getNotificationById
    // -------------------------------------------------------------------------

    @Test
    fun `insertNotification and getNotificationById round-trips correctly`() = runBlocking {
        val n = makeNotification("n1", title = "Order arrived", message = "Your order is ready")
        dao.insertNotification(n)

        val retrieved = dao.getNotificationById("n1")

        assertNotNull(retrieved)
        assertEquals("n1", retrieved!!.id)
        assertEquals("Order arrived", retrieved.title)
        assertFalse(retrieved.isRead)
    }

    @Test
    fun `insertNotification REPLACE strategy overwrites existing`() = runBlocking {
        dao.insertNotification(makeNotification("n1", title = "Old"))
        dao.insertNotification(makeNotification("n1", title = "New"))

        assertEquals("New", dao.getNotificationById("n1")!!.title)
    }

    @Test
    fun `getNotificationById returns null for missing id`() = runBlocking {
        assertNull(dao.getNotificationById("nonexistent"))
    }

    // -------------------------------------------------------------------------
    // insertNotifications (batch)
    // -------------------------------------------------------------------------

    @Test
    fun `insertNotifications inserts all items`() = runBlocking {
        val list = listOf(makeNotification("n1"), makeNotification("n2"), makeNotification("n3"))
        dao.insertNotifications(list)

        // Verify all three are retrievable
        assertNotNull(dao.getNotificationById("n1"))
        assertNotNull(dao.getNotificationById("n2"))
        assertNotNull(dao.getNotificationById("n3"))
    }

    // -------------------------------------------------------------------------
    // getNotificationsByUser
    // -------------------------------------------------------------------------

    @Test
    fun `getNotificationsByUser returns only notifications for that user`() = runBlocking {
        dao.insertNotification(makeNotification("n1", userId = "user-A"))
        dao.insertNotification(makeNotification("n2", userId = "user-B"))
        dao.insertNotification(makeNotification("n3", userId = "user-A"))

        val list = dao.getNotificationsByUser("user-A").first()

        assertEquals(2, list.size)
        assertTrue(list.all { it.userId == "user-A" })
    }

    @Test
    fun `getNotificationsByUser returns empty list when user has no notifications`() = runBlocking {
        val list = dao.getNotificationsByUser("nobody").first()
        assertTrue(list.isEmpty())
    }

    // -------------------------------------------------------------------------
    // getUnreadNotifications
    // -------------------------------------------------------------------------

    @Test
    fun `getUnreadNotifications returns only unread`() = runBlocking {
        dao.insertNotification(makeNotification("n-unread", isRead = false))
        dao.insertNotification(makeNotification("n-read", isRead = true))

        val unread = dao.getUnreadNotifications(userId).first()

        assertEquals(1, unread.size)
        assertEquals("n-unread", unread[0].id)
    }

    @Test
    fun `getUnreadNotifications returns empty after all are read`() = runBlocking {
        dao.insertNotification(makeNotification("n1", isRead = true))
        dao.insertNotification(makeNotification("n2", isRead = true))

        val unread = dao.getUnreadNotifications(userId).first()

        assertTrue(unread.isEmpty())
    }

    // -------------------------------------------------------------------------
    // getUnreadCount
    // -------------------------------------------------------------------------

    @Test
    fun `getUnreadCount returns correct count`() = runBlocking {
        dao.insertNotification(makeNotification("n1", isRead = false))
        dao.insertNotification(makeNotification("n2", isRead = false))
        dao.insertNotification(makeNotification("n3", isRead = true))

        val count = dao.getUnreadCount(userId).first()

        assertEquals(2, count)
    }

    @Test
    fun `getUnreadCount returns zero when all are read`() = runBlocking {
        dao.insertNotification(makeNotification("n1", isRead = true))

        assertEquals(0, dao.getUnreadCount(userId).first())
    }

    // -------------------------------------------------------------------------
    // markAsRead
    // -------------------------------------------------------------------------

    @Test
    fun `markAsRead flips isRead to true for targeted notification`() = runBlocking {
        dao.insertNotification(makeNotification("n1", isRead = false))
        dao.insertNotification(makeNotification("n2", isRead = false))

        dao.markAsRead("n1")

        assertTrue(dao.getNotificationById("n1")!!.isRead)
        assertFalse(dao.getNotificationById("n2")!!.isRead)
    }

    // -------------------------------------------------------------------------
    // markAllAsRead
    // -------------------------------------------------------------------------

    @Test
    fun `markAllAsRead marks every notification for the user as read`() = runBlocking {
        dao.insertNotification(makeNotification("n1", isRead = false))
        dao.insertNotification(makeNotification("n2", isRead = false))
        dao.insertNotification(makeNotification("n3", userId = "other", isRead = false))

        dao.markAllAsRead(userId)

        assertEquals(0, dao.getUnreadCount(userId).first())
        // Other user's notification stays unread
        assertEquals(1, dao.getUnreadCount("other").first())
    }

    // -------------------------------------------------------------------------
    // deleteNotification
    // -------------------------------------------------------------------------

    @Test
    fun `deleteNotification removes only the targeted item`() = runBlocking {
        dao.insertNotification(makeNotification("n1"))
        dao.insertNotification(makeNotification("n2"))

        dao.deleteNotification("n1")

        assertNull(dao.getNotificationById("n1"))
        assertNotNull(dao.getNotificationById("n2"))
    }

    // -------------------------------------------------------------------------
    // deleteAllForUser
    // -------------------------------------------------------------------------

    @Test
    fun `deleteAllForUser removes only that user's notifications`() = runBlocking {
        dao.insertNotification(makeNotification("n1", userId = "user-A"))
        dao.insertNotification(makeNotification("n2", userId = "user-A"))
        dao.insertNotification(makeNotification("n3", userId = "user-B"))

        dao.deleteAllForUser("user-A")

        assertNull(dao.getNotificationById("n1"))
        assertNull(dao.getNotificationById("n2"))
        assertNotNull(dao.getNotificationById("n3"))
    }

    // -------------------------------------------------------------------------
    // deleteOldNotifications
    // -------------------------------------------------------------------------

    @Test
    fun `deleteOldNotifications removes items older than cutoff`() = runBlocking {
        dao.insertNotification(makeNotification("old", createdAt = "2026-01-01T00:00:00Z"))
        dao.insertNotification(makeNotification("recent", createdAt = "2026-06-01T00:00:00Z"))

        dao.deleteOldNotifications("2026-03-01T00:00:00Z")

        assertNull(dao.getNotificationById("old"))
        assertNotNull(dao.getNotificationById("recent"))
    }

    @Test
    fun `deleteOldNotifications does not remove notifications at or after cutoff`() = runBlocking {
        dao.insertNotification(makeNotification("n1", createdAt = "2026-04-01T00:00:00Z"))

        dao.deleteOldNotifications("2026-03-01T00:00:00Z")

        assertNotNull(dao.getNotificationById("n1"))
    }
}
