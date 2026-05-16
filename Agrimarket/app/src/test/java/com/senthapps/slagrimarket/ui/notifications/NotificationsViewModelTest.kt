package com.senthapps.slagrimarket.ui.notifications

import com.senthapps.slagrimarket.data.model.Notification
import com.senthapps.slagrimarket.data.model.NotificationType
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.NotificationRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NotificationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var notificationRepository: NotificationRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: NotificationsViewModel

    private val mockUser = User(
        id = "user1",
        name = "Test Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        language = "en",
        createdAt = "2025-11-20T10:00:00Z"
    )

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
            message = "App updated successfully",
            isRead = true,
            createdAt = "2025-11-19T10:00:00Z"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        notificationRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNotifications should set error when user not found`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        viewModel.loadNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("User not found", state.error)
        assertTrue(state.notifications.isEmpty())
    }

    @Test
    fun `loadNotifications should populate notifications from repository`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } returns flowOf(mockNotifications)

        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        viewModel.loadNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.notifications.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadNotifications should handle empty notifications`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } returns flowOf(emptyList())

        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        viewModel.loadNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.notifications.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `markAsRead should call repository with correct notificationId`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } returns flowOf(mockNotifications)
        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        viewModel.loadNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.markAsRead("notif1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { notificationRepository.markAsRead("notif1") }
    }

    @Test
    fun `markAllAsRead should call repository with userId when user found`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } returns flowOf(mockNotifications)
        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.markAllAsRead()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { notificationRepository.markAllAsRead("user1") }
    }

    @Test
    fun `markAllAsRead should do nothing when user not found`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel = NotificationsViewModel(notificationRepository, authRepository)

        viewModel.markAllAsRead()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { notificationRepository.markAllAsRead(any()) }
    }

    @Test
    fun `deleteNotification should call repository with notificationId`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } returns flowOf(mockNotifications)
        viewModel = NotificationsViewModel(notificationRepository, authRepository)

        viewModel.deleteNotification("notif1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify { notificationRepository.deleteNotification("notif1") }
    }

    @Test
    fun `loadNotifications should set error on exception`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } throws RuntimeException("Network error")

        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        viewModel.loadNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNotNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `initial state has no notifications, not loading, and no error`() {
        viewModel = NotificationsViewModel(notificationRepository, authRepository)

        val state = viewModel.uiState.value
        assertTrue(state.notifications.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `markAsRead exception does not crash or set error state`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } returns flowOf(mockNotifications)
        coEvery { notificationRepository.markAsRead(any()) } throws RuntimeException("DB error")
        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        viewModel.loadNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.markAsRead("notif1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `deleteNotification exception does not crash or set error state`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } returns flowOf(mockNotifications)
        coEvery { notificationRepository.deleteNotification(any()) } throws RuntimeException("DB error")
        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        viewModel.loadNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.deleteNotification("notif1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `markAllAsRead exception does not crash or set error state`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { notificationRepository.getNotificationsByUser("user1") } returns flowOf(mockNotifications)
        coEvery { notificationRepository.markAllAsRead(any()) } throws RuntimeException("DB error")
        viewModel = NotificationsViewModel(notificationRepository, authRepository)
        viewModel.loadNotifications()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.markAllAsRead()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }
}
