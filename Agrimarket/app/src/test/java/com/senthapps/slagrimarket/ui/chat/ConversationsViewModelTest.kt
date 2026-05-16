package com.senthapps.slagrimarket.ui.chat

import com.senthapps.slagrimarket.data.model.Conversation
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.MessageRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
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
class ConversationsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var messageRepository: MessageRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ConversationsViewModel

    private val mockUser = User(
        id = "user1",
        name = "Alice",
        phone = "+94771234567",
        userType = UserType.BUYER,
        verified = true,
        createdAt = "2026-01-01T00:00:00Z"
    )

    private fun makeConversation(id: String) = Conversation(
        id = id,
        listingId = null,
        participant1Id = "user1",
        participant1Name = "Alice",
        participant2Id = "farmer1",
        participant2Name = "Bob",
        lastMessage = "Hi",
        lastMessageTime = "2026-01-01T00:00:00Z",
        unreadCount = 0,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        messageRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        viewModel = ConversationsViewModel(messageRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not loading`() {
        val state = viewModel.uiState.value
        assertTrue(state.conversations.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadConversations sets error when user not authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel.loadConversations()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("User not found", state.error)
        assertTrue(state.conversations.isEmpty())
    }

    @Test
    fun `loadConversations populates state with conversations from flow`() = runTest {
        val conversations = listOf(makeConversation("c1"), makeConversation("c2"))
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationsForUser("user1") } returns flowOf(conversations)

        viewModel.loadConversations()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(2, state.conversations.size)
        assertEquals("c1", state.conversations[0].id)
        assertNull(state.error)
    }

    @Test
    fun `loadConversations sets current user id in state`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationsForUser("user1") } returns flowOf(emptyList())

        viewModel.loadConversations()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("user1", viewModel.uiState.value.currentUserId)
    }

    @Test
    fun `loadConversations shows empty list when no conversations`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationsForUser("user1") } returns flowOf(emptyList())

        viewModel.loadConversations()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.conversations.isEmpty())
        assertNull(state.error)
    }

    @Test
    fun `loadConversations sets error on repository exception`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationsForUser(any()) } throws RuntimeException("DB error")

        viewModel.loadConversations()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load conversations"))
    }

    @Test
    fun `loadConversations sets isLoading true before coroutine completes`() = runTest {
        // Use a suspending answer so the coroutine pauses after setting isLoading = true
        coEvery { authRepository.getCurrentUser() } coAnswers { delay(100); mockUser }
        coEvery { messageRepository.getConversationsForUser("user1") } returns flowOf(emptyList())

        viewModel.loadConversations()
        testDispatcher.scheduler.runCurrent() // advance past isLoading = true, pause at delay(100)
        assertTrue(viewModel.uiState.value.isLoading)

        testDispatcher.scheduler.advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadConversations clears previous error on retry`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel.loadConversations()
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals("User not found", viewModel.uiState.value.error)

        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationsForUser("user1") } returns flowOf(emptyList())
        viewModel.loadConversations()
        // runCurrent advances past error = null assignment before any suspension
        testDispatcher.scheduler.runCurrent()
        assertNull(viewModel.uiState.value.error)
        testDispatcher.scheduler.advanceUntilIdle()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadConversations updates conversations when flow emits new list`() = runTest {
        val conversationsFlow = MutableStateFlow<List<Conversation>>(emptyList())
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationsForUser("user1") } returns conversationsFlow

        viewModel.loadConversations()
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value.conversations.isEmpty())

        conversationsFlow.value = listOf(makeConversation("c1"), makeConversation("c2"))
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.conversations.size)
    }

    @Test
    fun `conversations with unread messages reflected in state`() = runTest {
        val unreadConversation = makeConversation("c1").copy(unreadCount = 5)
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationsForUser("user1") } returns flowOf(listOf(unreadConversation))

        viewModel.loadConversations()
        testDispatcher.scheduler.advanceUntilIdle()

        val conversations = viewModel.uiState.value.conversations
        assertEquals(1, conversations.size)
        assertEquals(5, conversations[0].unreadCount)
    }
}
