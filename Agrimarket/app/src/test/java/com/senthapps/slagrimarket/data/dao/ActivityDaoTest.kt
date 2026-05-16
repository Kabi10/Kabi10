package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senthapps.slagrimarket.data.model.*
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
import java.time.Instant

@Database(entities = [User::class, Activity::class], version = 1, exportSchema = false)
abstract class ActivityTestDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun userDao(): UserDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class ActivityDaoTest {

    private lateinit var db: ActivityTestDatabase
    private lateinit var dao: ActivityDao

    private val userId = "user-001"
    private val recentTs = Instant.now().toString()
    private val oldTs = Instant.now().minusSeconds(3600L * 48).toString()

    private fun makeActivity(
        id: String,
        userId: String = this.userId,
        type: ActivityType = ActivityType.LISTING_CREATED,
        status: ActivityStatus = ActivityStatus.ACTIVE,
        priority: ActivityPriority = ActivityPriority.NORMAL,
        isRead: Boolean = false,
        isActionable: Boolean = false,
        timestamp: String = recentTs,
        expiresAt: String? = null
    ) = Activity(
        id = id,
        userId = userId,
        activityType = type,
        title = "Test Activity",
        description = "Test description",
        status = status,
        priority = priority,
        isRead = isRead,
        isActionable = isActionable,
        timestamp = timestamp,
        expiresAt = expiresAt
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            ActivityTestDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.activityDao()
        // Pre-insert user to satisfy FK constraint
        runBlocking {
            db.userDao().insertUser(User(id = userId, name = "Test Farmer"))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ---- CRUD ----

    @Test
    fun `insertActivity and getActivityById returns inserted activity`() = runBlocking {
        val activity = makeActivity("act-1")
        dao.insertActivity(activity)

        val result = dao.getActivityById("act-1")
        assertNotNull(result)
        assertEquals("act-1", result!!.id)
        assertEquals(userId, result.userId)
    }

    @Test
    fun `getActivityById returns null for unknown id`() = runBlocking {
        assertNull(dao.getActivityById("nonexistent"))
    }

    @Test
    fun `insertActivities bulk inserts all records`() = runBlocking {
        dao.insertActivities(listOf(makeActivity("a1"), makeActivity("a2"), makeActivity("a3")))

        val results = dao.getActivitiesByUser(userId)
        assertEquals(3, results.size)
    }

    @Test
    fun `insertActivity replaces on conflict`() = runBlocking {
        dao.insertActivity(makeActivity("act-1", type = ActivityType.ORDER_PLACED))
        dao.insertActivity(makeActivity("act-1", type = ActivityType.ORDER_COMPLETED))

        val result = dao.getActivityById("act-1")
        assertEquals(ActivityType.ORDER_COMPLETED, result!!.activityType)
    }

    @Test
    fun `updateActivity persists changes`() = runBlocking {
        val original = makeActivity("act-1", isRead = false)
        dao.insertActivity(original)

        dao.updateActivity(original.copy(isRead = true))

        assertTrue(dao.getActivityById("act-1")!!.isRead)
    }

    @Test
    fun `deleteActivity removes the entity`() = runBlocking {
        val act = makeActivity("act-1")
        dao.insertActivity(act)
        dao.deleteActivity(act)

        assertNull(dao.getActivityById("act-1"))
    }

    @Test
    fun `deleteActivityById removes the entity`() = runBlocking {
        dao.insertActivity(makeActivity("act-del"))
        dao.deleteActivityById("act-del")

        assertNull(dao.getActivityById("act-del"))
    }

    @Test
    fun `deleteAllActivities clears the table`() = runBlocking {
        dao.insertActivities(listOf(makeActivity("a1"), makeActivity("a2")))
        dao.deleteAllActivities()

        assertTrue(dao.getActivitiesByUser(userId).isEmpty())
    }

    // ---- User queries ----

    @Test
    fun `getActivitiesByUser returns only activities for that user`() = runBlocking {
        db.userDao().insertUser(User(id = "other-user", name = "Other"))
        dao.insertActivity(makeActivity("a1", userId = userId))
        dao.insertActivity(makeActivity("a2", userId = "other-user"))

        val results = dao.getActivitiesByUser(userId)
        assertEquals(1, results.size)
        assertEquals("a1", results[0].id)
    }

    @Test
    fun `getActivitiesByUserFlow emits activities for user`() = runBlocking {
        dao.insertActivity(makeActivity("a1"))
        dao.insertActivity(makeActivity("a2"))

        val results = dao.getActivitiesByUserFlow(userId).first()
        assertEquals(2, results.size)
    }

    @Test
    fun `getActiveActivitiesByUser excludes archived and dismissed`() = runBlocking {
        dao.insertActivity(makeActivity("a-active", status = ActivityStatus.ACTIVE))
        dao.insertActivity(makeActivity("a-archived", status = ActivityStatus.ARCHIVED))
        dao.insertActivity(makeActivity("a-dismissed", status = ActivityStatus.DISMISSED))

        val results = dao.getActiveActivitiesByUser(userId)
        assertEquals(1, results.size)
        assertEquals("a-active", results[0].id)
    }

    @Test
    fun `getActiveActivitiesByUser sorts by priority descending`() = runBlocking {
        dao.insertActivity(makeActivity("a-low", priority = ActivityPriority.LOW))
        dao.insertActivity(makeActivity("a-urgent", priority = ActivityPriority.URGENT))
        dao.insertActivity(makeActivity("a-high", priority = ActivityPriority.HIGH))
        dao.insertActivity(makeActivity("a-normal", priority = ActivityPriority.NORMAL))

        val results = dao.getActiveActivitiesByUser(userId)
        assertEquals(ActivityPriority.URGENT, results[0].priority)
        assertEquals(ActivityPriority.HIGH, results[1].priority)
        assertEquals(ActivityPriority.NORMAL, results[2].priority)
        assertEquals(ActivityPriority.LOW, results[3].priority)
    }

    @Test
    fun `getActiveActivitiesByUser respects limit and offset`() = runBlocking {
        repeat(5) { i -> dao.insertActivity(makeActivity("a-$i")) }

        val page = dao.getActiveActivitiesByUser(userId, limit = 2, offset = 0)
        assertEquals(2, page.size)
    }

    @Test
    fun `getUnreadActivitiesByUser returns only unread active activities`() = runBlocking {
        dao.insertActivity(makeActivity("a-unread", isRead = false))
        dao.insertActivity(makeActivity("a-read", isRead = true))
        dao.insertActivity(makeActivity("a-archived-unread", isRead = false, status = ActivityStatus.ARCHIVED))

        val results = dao.getUnreadActivitiesByUser(userId)
        assertEquals(1, results.size)
        assertEquals("a-unread", results[0].id)
    }

    @Test
    fun `getActionableActivitiesByUser returns actionable unread active activities`() = runBlocking {
        dao.insertActivity(makeActivity("a-actionable", isActionable = true, isRead = false))
        dao.insertActivity(makeActivity("a-not-actionable", isActionable = false, isRead = false))
        dao.insertActivity(makeActivity("a-actionable-read", isActionable = true, isRead = true))

        val results = dao.getActionableActivitiesByUser(userId)
        assertEquals(1, results.size)
        assertEquals("a-actionable", results[0].id)
    }

    // ---- Filtering ----

    @Test
    fun `getFilteredActivities with type filter returns matching activities`() = runBlocking {
        dao.insertActivity(makeActivity("a-listing", type = ActivityType.LISTING_CREATED))
        dao.insertActivity(makeActivity("a-order", type = ActivityType.ORDER_PLACED))

        val results = dao.getFilteredActivities(userId, activityType = ActivityType.LISTING_CREATED)
        assertEquals(1, results.size)
        assertEquals(ActivityType.LISTING_CREATED, results[0].activityType)
    }

    @Test
    fun `getFilteredActivities with status filter returns matching activities`() = runBlocking {
        dao.insertActivity(makeActivity("a-active", status = ActivityStatus.ACTIVE))
        dao.insertActivity(makeActivity("a-archived", status = ActivityStatus.ARCHIVED))

        val results = dao.getFilteredActivities(userId, status = ActivityStatus.ARCHIVED)
        assertEquals(1, results.size)
        assertEquals("a-archived", results[0].id)
    }

    @Test
    fun `getFilteredActivities with isRead filter returns matching activities`() = runBlocking {
        dao.insertActivity(makeActivity("a-unread", isRead = false))
        dao.insertActivity(makeActivity("a-read", isRead = true))

        val unread = dao.getFilteredActivities(userId, isRead = false)
        assertEquals(1, unread.size)
        assertEquals("a-unread", unread[0].id)
    }

    @Test
    fun `getActivitiesByType returns only matching type`() = runBlocking {
        dao.insertActivity(makeActivity("a-listing", type = ActivityType.LISTING_CREATED))
        dao.insertActivity(makeActivity("a-market", type = ActivityType.MARKET_UPDATE))

        val results = dao.getActivitiesByType(ActivityType.LISTING_CREATED)
        assertEquals(1, results.size)
    }

    @Test
    fun `getActivitiesForEntity returns activities linked to that entity`() = runBlocking {
        val act = makeActivity("a-linked").copy(
            relatedEntityId = "listing-abc",
            relatedEntityType = EntityType.LISTING
        )
        dao.insertActivity(act)
        dao.insertActivity(makeActivity("a-unlinked"))

        val results = dao.getActivitiesForEntity("listing-abc", EntityType.LISTING)
        assertEquals(1, results.size)
        assertEquals("a-linked", results[0].id)
    }

    // ---- Update operations ----

    @Test
    fun `markAsRead sets isRead to true`() = runBlocking {
        dao.insertActivity(makeActivity("act-1", isRead = false))
        dao.markAsRead("act-1")

        assertTrue(dao.getActivityById("act-1")!!.isRead)
    }

    @Test
    fun `markAllAsReadForUser marks all unread activities as read`() = runBlocking {
        dao.insertActivity(makeActivity("a1", isRead = false))
        dao.insertActivity(makeActivity("a2", isRead = false))
        dao.insertActivity(makeActivity("a3", isRead = true))

        dao.markAllAsReadForUser(userId)

        val unread = dao.getUnreadActivitiesByUser(userId)
        assertTrue(unread.isEmpty())
    }

    @Test
    fun `updateActivityStatus changes status correctly`() = runBlocking {
        dao.insertActivity(makeActivity("act-1", status = ActivityStatus.ACTIVE))
        dao.updateActivityStatus("act-1", ActivityStatus.DISMISSED)

        assertEquals(ActivityStatus.DISMISSED, dao.getActivityById("act-1")!!.status)
    }

    @Test
    fun `archiveAllActivitiesForUser sets all active to archived`() = runBlocking {
        dao.insertActivity(makeActivity("a1", status = ActivityStatus.ACTIVE))
        dao.insertActivity(makeActivity("a2", status = ActivityStatus.ACTIVE))
        dao.insertActivity(makeActivity("a3", status = ActivityStatus.DISMISSED))

        dao.archiveAllActivitiesForUser(userId)

        val active = dao.getActiveActivitiesByUser(userId)
        assertTrue(active.isEmpty())
    }

    @Test
    fun `dismissActivity sets status to DISMISSED`() = runBlocking {
        dao.insertActivity(makeActivity("act-dismiss"))
        dao.dismissActivity("act-dismiss")

        assertEquals(ActivityStatus.DISMISSED, dao.getActivityById("act-dismiss")!!.status)
    }

    // ---- Statistics ----

    @Test
    fun `getUnreadCountByUser returns correct count`() = runBlocking {
        dao.insertActivity(makeActivity("a1", isRead = false))
        dao.insertActivity(makeActivity("a2", isRead = false))
        dao.insertActivity(makeActivity("a3", isRead = true))

        assertEquals(2, dao.getUnreadCountByUser(userId))
    }

    @Test
    fun `getUnreadCountByUserFlow emits correct count`() = runBlocking {
        dao.insertActivity(makeActivity("a1", isRead = false))
        dao.insertActivity(makeActivity("a2", isRead = true))

        val count = dao.getUnreadCountByUserFlow(userId).first()
        assertEquals(1, count)
    }

    @Test
    fun `getActionableCountByUser returns count of actionable unread active`() = runBlocking {
        dao.insertActivity(makeActivity("a1", isActionable = true, isRead = false))
        dao.insertActivity(makeActivity("a2", isActionable = true, isRead = false))
        dao.insertActivity(makeActivity("a3", isActionable = false, isRead = false))
        dao.insertActivity(makeActivity("a4", isActionable = true, isRead = true))

        assertEquals(2, dao.getActionableCountByUser(userId))
    }

    @Test
    fun `getActivitySummaryByUser returns correct aggregated counts`() = runBlocking {
        dao.insertActivity(makeActivity("a1", isRead = false, isActionable = true, priority = ActivityPriority.URGENT))
        dao.insertActivity(makeActivity("a2", isRead = false, isActionable = false, priority = ActivityPriority.HIGH))
        dao.insertActivity(makeActivity("a3", isRead = true, isActionable = false, priority = ActivityPriority.NORMAL))

        val summary = dao.getActivitySummaryByUser(userId)
        assertEquals(3, summary.total)
        assertEquals(2, summary.unread)
        assertEquals(1, summary.actionable)
        assertEquals(1, summary.urgent)
        assertEquals(1, summary.high)
    }

    // ---- Cleanup ----

    @Test
    fun `cleanupDismissedActivities removes old dismissed activities`() = runBlocking {
        val oldDismissedTs = Instant.now().minusSeconds(3600L * 24 * 35).toString()
        dao.insertActivity(makeActivity("a-old-dismissed", status = ActivityStatus.DISMISSED, timestamp = oldDismissedTs))
        dao.insertActivity(makeActivity("a-recent-dismissed", status = ActivityStatus.DISMISSED, timestamp = recentTs))
        dao.insertActivity(makeActivity("a-active"))

        dao.cleanupDismissedActivities(retentionDays = 30)

        assertNull(dao.getActivityById("a-old-dismissed"))
        assertNotNull(dao.getActivityById("a-recent-dismissed"))
        assertNotNull(dao.getActivityById("a-active"))
    }

    @Test
    fun `cleanupArchivedActivities removes old archived activities`() = runBlocking {
        val oldArchivedTs = Instant.now().minusSeconds(3600L * 24 * 95).toString()
        dao.insertActivity(makeActivity("a-old-archived", status = ActivityStatus.ARCHIVED, timestamp = oldArchivedTs))
        dao.insertActivity(makeActivity("a-recent-archived", status = ActivityStatus.ARCHIVED, timestamp = recentTs))

        dao.cleanupArchivedActivities(retentionDays = 90)

        assertNull(dao.getActivityById("a-old-archived"))
        assertNotNull(dao.getActivityById("a-recent-archived"))
    }

    @Test
    fun `archiveExpiredActivities archives active activities whose expiresAt is in the past`() = runBlocking {
        val pastExpiry = Instant.now().minusSeconds(3600).toString()
        val futureExpiry = Instant.now().plusSeconds(3600).toString()
        dao.insertActivity(makeActivity("a-expired", expiresAt = pastExpiry))
        dao.insertActivity(makeActivity("a-not-expired", expiresAt = futureExpiry))
        dao.insertActivity(makeActivity("a-no-expiry"))

        dao.archiveExpiredActivities()

        assertEquals(ActivityStatus.ARCHIVED, dao.getActivityById("a-expired")!!.status)
        assertEquals(ActivityStatus.ACTIVE, dao.getActivityById("a-not-expired")!!.status)
        assertEquals(ActivityStatus.ACTIVE, dao.getActivityById("a-no-expiry")!!.status)
    }

    @Test
    fun `getRecentActivityCount returns count from last hour`() = runBlocking {
        dao.insertActivity(makeActivity("a-recent", timestamp = Instant.now().minusSeconds(300).toString()))
        dao.insertActivity(makeActivity("a-old", timestamp = Instant.now().minusSeconds(7200).toString()))

        val count = dao.getRecentActivityCount()
        assertEquals(1, count)
    }

    @Test
    fun `getLastUpdateTimeForUser returns latest timestamp for active activities`() = runBlocking {
        val earlierTs = Instant.now().minusSeconds(3600).toString()
        dao.insertActivity(makeActivity("a-earlier", timestamp = earlierTs))
        dao.insertActivity(makeActivity("a-later", timestamp = recentTs))

        val lastUpdate = dao.getLastUpdateTimeForUser(userId)
        assertNotNull(lastUpdate)
    }

    @Test
    fun `getLastUpdateTimeForUser returns null when no activities exist`() = runBlocking {
        val result = dao.getLastUpdateTimeForUser(userId)
        assertNull(result)
    }

    // ---- Flow ----

    @Test
    fun `getActivityByIdFlow emits activity after insert`() = runBlocking {
        dao.insertActivity(makeActivity("a-flow"))

        val result = dao.getActivityByIdFlow("a-flow").first()
        assertNotNull(result)
        assertEquals("a-flow", result!!.id)
    }

    @Test
    fun `getActiveActivitiesByUserFlow emits only active activities`() = runBlocking {
        dao.insertActivity(makeActivity("a-active", status = ActivityStatus.ACTIVE))
        dao.insertActivity(makeActivity("a-archived", status = ActivityStatus.ARCHIVED))

        val results = dao.getActiveActivitiesByUserFlow(userId).first()
        assertEquals(1, results.size)
        assertEquals("a-active", results[0].id)
    }

    @Test
    fun `getUnreadActivitiesByUserFlow emits unread active activities`() = runBlocking {
        dao.insertActivity(makeActivity("a-unread", isRead = false))
        dao.insertActivity(makeActivity("a-read", isRead = true))

        val results = dao.getUnreadActivitiesByUserFlow(userId).first()
        assertEquals(1, results.size)
        assertEquals("a-unread", results[0].id)
    }
}
