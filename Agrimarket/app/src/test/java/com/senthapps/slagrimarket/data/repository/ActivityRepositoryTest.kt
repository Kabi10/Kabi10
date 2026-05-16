package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.ActivityApiService
import com.senthapps.slagrimarket.data.api.ActivitiesResponse
import com.senthapps.slagrimarket.data.api.MarkAllReadResponse
import com.senthapps.slagrimarket.data.api.MarkReadResponse
import com.senthapps.slagrimarket.data.dao.ActivityDao
import com.senthapps.slagrimarket.data.model.Activity
import com.senthapps.slagrimarket.data.model.ActivityPriority
import com.senthapps.slagrimarket.data.model.ActivityStatus
import com.senthapps.slagrimarket.data.model.ActivityType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityRepositoryTest {

    private lateinit var activityDao: ActivityDao
    private lateinit var activityApiService: ActivityApiService
    private lateinit var repository: ActivityRepository

    private val userId = "user1"

    private fun makeActivity(id: String, isRead: Boolean = false) = Activity(
        id = id,
        userId = userId,
        activityType = ActivityType.LISTING_CREATED,
        title = "Test",
        titleTamil = "",
        titleSinhala = "",
        description = "Desc",
        descriptionTamil = "",
        descriptionSinhala = "",
        relatedEntityType = null,
        relatedEntityId = null,
        priority = ActivityPriority.NORMAL,
        status = ActivityStatus.ACTIVE,
        isRead = isRead,
        isActionable = false,
        timestamp = "2026-01-01T00:00:00Z",
        expiresAt = null,
        metadata = emptyMap()
    )

    @Before
    fun setup() {
        activityDao = mockk(relaxed = true)
        activityApiService = mockk(relaxed = true)
        repository = ActivityRepository(activityApiService, activityDao)
    }

    // -------------------------------------------------------------------------
    // getActivitiesForUser
    // -------------------------------------------------------------------------

    @Test
    fun `getActivitiesForUser emits cached data when cache is non-empty and no refresh needed`() = runTest {
        val cached = listOf(makeActivity("a1"), makeActivity("a2"))
        coEvery { activityDao.getActivitiesByUser(userId) } returns cached
        coEvery { activityDao.getLastUpdateTimeForUser(userId) } returns "2026-01-01T00:01:00Z"

        val results = mutableListOf<Resource<List<Activity>>>()
        val flow = repository.getActivitiesForUser(userId, forceRefresh = false)
        flow.collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success && it.data == cached })
    }

    @Test
    fun `getActivitiesForUser uses server data on force refresh`() = runTest {
        val serverActivities = listOf(makeActivity("srv1"))
        val apiResponse = mockk<Response<ActivitiesResponse>> {
            every { isSuccessful } returns true
            every { body() } returns ActivitiesResponse(
                activities = serverActivities,
                totalCount = 1,
                page = 1,
                totalPages = 1,
                hasNext = false,
                hasPrevious = false,
                lastUpdated = "2026-01-01T00:00:00Z"
            )
        }
        coEvery { activityDao.getActivitiesByUser(userId) } returns emptyList()
        coEvery { activityDao.getLastUpdateTimeForUser(userId) } returns null
        coEvery { activityApiService.getActivities(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns apiResponse

        val results = mutableListOf<Resource<List<Activity>>>()
        repository.getActivitiesForUser(userId, forceRefresh = true).collect { results.add(it) }

        assertTrue(results.any { it is Resource.Success && it.data == serverActivities })
        coVerify { activityDao.insertActivities(serverActivities) }
    }

    @Test
    fun `getActivitiesForUser falls back to sample activities when network fails and cache empty`() = runTest {
        coEvery { activityDao.getActivitiesByUser(userId) } returns emptyList()
        coEvery { activityDao.getLastUpdateTimeForUser(userId) } returns null
        coEvery { activityApiService.getActivities(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } throws Exception("Network error")

        val results = mutableListOf<Resource<List<Activity>>>()
        repository.getActivitiesForUser(userId, forceRefresh = true).collect { results.add(it) }

        // Should emit sample activities (not an error) when cache is empty and network fails
        assertTrue(results.any { it is Resource.Success })
        coVerify { activityDao.insertActivities(any()) }
    }

    // -------------------------------------------------------------------------
    // getActivitiesFlow / getUnreadActivities / getActionableActivities
    // -------------------------------------------------------------------------

    @Test
    fun `getActivitiesFlow delegates to DAO`() = runTest {
        val activities = listOf(makeActivity("a1"))
        every { activityDao.getActivitiesByUserFlow(userId) } returns flowOf(activities)

        val result = repository.getActivitiesFlow(userId).first()

        assertEquals(activities, result)
    }

    @Test
    fun `getUnreadActivities delegates to DAO`() = runTest {
        val unread = listOf(makeActivity("u1", isRead = false))
        every { activityDao.getUnreadActivitiesByUserFlow(userId) } returns flowOf(unread)

        val result = repository.getUnreadActivities(userId).first()

        assertEquals(unread, result)
    }

    // -------------------------------------------------------------------------
    // markAsRead
    // -------------------------------------------------------------------------

    @Test
    fun `markAsRead updates DAO and syncs to server`() = runTest {
        coEvery { activityDao.markAsRead("act1") } just Runs
        val apiResp = mockk<Response<MarkReadResponse>> { every { isSuccessful } returns true }
        coEvery { activityApiService.markAsRead("act1") } returns apiResp

        val result = repository.markAsRead("act1")

        assertTrue(result is Resource.Success)
        coVerify { activityDao.markAsRead("act1") }
        coVerify { activityApiService.markAsRead("act1") }
    }

    @Test
    fun `markAsRead succeeds locally even when server throws`() = runTest {
        coEvery { activityDao.markAsRead("act1") } just Runs
        coEvery { activityApiService.markAsRead("act1") } throws Exception("Network error")

        val result = repository.markAsRead("act1")

        assertTrue(result is Resource.Success)
        coVerify { activityDao.markAsRead("act1") }
    }

    @Test
    fun `markAsRead returns error when DAO throws`() = runTest {
        coEvery { activityDao.markAsRead("act1") } throws RuntimeException("DB error")

        val result = repository.markAsRead("act1")

        assertTrue(result is Resource.Error)
    }

    // -------------------------------------------------------------------------
    // markAllAsRead — covers the count-before-mark fix
    // -------------------------------------------------------------------------

    @Test
    fun `markAllAsRead captures unread count BEFORE marking so fallback returns correct number`() = runTest {
        // Sequence: first call returns 5 (before mark), second call returns 0 (after mark)
        coEvery { activityDao.getUnreadCountByUser(userId) } returnsMany listOf(5, 0)
        coEvery { activityDao.markAllAsReadForUser(userId) } just Runs
        coEvery { activityApiService.markAllAsRead(userId) } throws Exception("Network error")

        val result = repository.markAllAsRead(userId)

        assertTrue(result is Resource.Success)
        // Must return 5 (pre-mark count), not 0 (post-mark count)
        assertEquals(5, result.data!!)
    }

    @Test
    fun `markAllAsRead uses server count when API succeeds`() = runTest {
        coEvery { activityDao.getUnreadCountByUser(userId) } returns 3
        coEvery { activityDao.markAllAsReadForUser(userId) } just Runs
        val apiResp = mockk<Response<MarkAllReadResponse>> {
            every { isSuccessful } returns true
            every { body() } returns MarkAllReadResponse(success = true, markedCount = 7)
        }
        coEvery { activityApiService.markAllAsRead(userId) } returns apiResp

        val result = repository.markAllAsRead(userId)

        assertTrue(result is Resource.Success)
        assertEquals(7, result.data!!)
    }

    @Test
    fun `markAllAsRead returns local count when API returns non-success`() = runTest {
        coEvery { activityDao.getUnreadCountByUser(userId) } returns 4
        coEvery { activityDao.markAllAsReadForUser(userId) } just Runs
        val apiResp = mockk<Response<MarkAllReadResponse>> { every { isSuccessful } returns false }
        coEvery { activityApiService.markAllAsRead(userId) } returns apiResp

        val result = repository.markAllAsRead(userId)

        assertTrue(result is Resource.Success)
        assertEquals(4, result.data!!)
    }

    // -------------------------------------------------------------------------
    // dismissActivity
    // -------------------------------------------------------------------------

    @Test
    fun `dismissActivity updates local status and syncs`() = runTest {
        coEvery { activityDao.updateActivityStatus("act1", ActivityStatus.DISMISSED) } just Runs
        coEvery { activityApiService.dismissActivity("act1") } returns mockk(relaxed = true)

        val result = repository.dismissActivity("act1")

        assertTrue(result is Resource.Success)
        coVerify { activityDao.updateActivityStatus("act1", ActivityStatus.DISMISSED) }
    }

    @Test
    fun `dismissActivity succeeds locally even when server throws`() = runTest {
        coEvery { activityDao.updateActivityStatus("act1", ActivityStatus.DISMISSED) } just Runs
        coEvery { activityApiService.dismissActivity("act1") } throws Exception("Network error")

        val result = repository.dismissActivity("act1")

        assertTrue(result is Resource.Success)
    }

    // -------------------------------------------------------------------------
    // getActivityById
    // -------------------------------------------------------------------------

    @Test
    fun `getActivityById returns cached activity when available`() = runTest {
        val activity = makeActivity("act1")
        coEvery { activityDao.getActivityById("act1") } returns activity

        val result = repository.getActivityById("act1")

        assertTrue(result is Resource.Success)
        assertEquals("act1", result.data!!.id)
        coVerify(exactly = 0) { activityApiService.getActivityById(any()) }
    }

    @Test
    fun `getActivityById fetches from network when not cached`() = runTest {
        val activity = makeActivity("act1")
        coEvery { activityDao.getActivityById("act1") } returns null
        val apiResp = mockk<Response<Activity>> {
            every { isSuccessful } returns true
            every { body() } returns activity
        }
        coEvery { activityApiService.getActivityById("act1") } returns apiResp

        val result = repository.getActivityById("act1")

        assertTrue(result is Resource.Success)
        assertEquals("act1", result.data!!.id)
        coVerify { activityDao.insertActivity(activity) }
    }

    @Test
    fun `getActivityById returns error when not cached and API fails`() = runTest {
        coEvery { activityDao.getActivityById("act1") } returns null
        val apiResp = mockk<Response<Activity>> { every { isSuccessful } returns false }
        coEvery { activityApiService.getActivityById("act1") } returns apiResp

        val result = repository.getActivityById("act1")

        assertTrue(result is Resource.Error)
    }

    // -------------------------------------------------------------------------
    // createActivity
    // -------------------------------------------------------------------------

    @Test
    fun `createActivity inserts locally first and returns success`() = runTest {
        coEvery { activityDao.insertActivity(any()) } just Runs
        coEvery { activityApiService.createActivity(any()) } throws Exception("Network error")

        val result = repository.createActivity(
            userId = userId,
            activityType = ActivityType.LISTING_CREATED,
            title = "New listing",
            description = "Tomatoes for sale"
        )

        assertTrue(result is Resource.Success)
        coVerify { activityDao.insertActivity(any()) }
    }

    @Test
    fun `createActivity returns server activity when API succeeds`() = runTest {
        val serverActivity = makeActivity("server-id")
        coEvery { activityDao.insertActivity(any()) } just Runs
        val apiResp = mockk<Response<Activity>> {
            every { isSuccessful } returns true
            every { body() } returns serverActivity
        }
        coEvery { activityApiService.createActivity(any()) } returns apiResp

        val result = repository.createActivity(
            userId = userId,
            activityType = ActivityType.LISTING_CREATED,
            title = "New",
            description = "Desc"
        )

        assertTrue(result is Resource.Success)
        assertEquals("server-id", result.data!!.id)
    }

    // -------------------------------------------------------------------------
    // getUnreadCount
    // -------------------------------------------------------------------------

    @Test
    fun `getUnreadCount falls back to DAO when API throws`() = runTest {
        coEvery { activityApiService.getUnreadCount(userId) } throws Exception("Network error")
        coEvery { activityDao.getUnreadCountByUser(userId) } returns 3

        val result = repository.getUnreadCount(userId)

        assertTrue(result is Resource.Success)
        assertEquals(3, result.data!!)
    }

    // -------------------------------------------------------------------------
    // clearOldActivities
    // -------------------------------------------------------------------------

    @Test
    fun `clearOldActivities archives expired and cleans up dismissed activities`() = runTest {
        coEvery { activityDao.archiveExpiredActivities() } just Runs
        coEvery { activityDao.cleanupDismissedActivities(any()) } just Runs
        coEvery { activityDao.cleanupArchivedActivities(any()) } just Runs

        repository.clearOldActivities(userId, daysToKeep = 30)

        coVerify { activityDao.archiveExpiredActivities() }
        coVerify { activityDao.cleanupDismissedActivities(30) }
        coVerify { activityDao.cleanupArchivedActivities(90) }
    }
}
