package com.senthapps.slagrimarket.ui.chat

import com.senthapps.slagrimarket.data.model.Conversation
import com.senthapps.slagrimarket.data.model.Message
import com.senthapps.slagrimarket.data.model.MessageType
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.MessageRepository
import com.senthapps.slagrimarket.data.sync.ChatRealtimeService
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
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
class ChatViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var messageRepository: MessageRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var chatRealtimeService: ChatRealtimeService
    private lateinit var viewModel: ChatViewModel

    private val mockUser = User(
        id = "user1",
        name = "Alice",
        phone = "+94771234567",
        userType = UserType.BUYER,
        verified = true,
        createdAt = "2026-01-01T00:00:00Z"
    )

    private val mockConversation = Conversation(
        id = "conv1",
        listingId = null,
        participant1Id = "user1",
        participant1Name = "Alice",
        participant2Id = "farmer1",
        participant2Name = "Bob",
        lastMessage = null,
        lastMessageTime = null,
        unreadCount = 0,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z"
    )

    private fun makeMessage(id: String = "msg1") = Message(
        id = id,
        conversationId = "conv1",
        senderId = "user1",
        senderName = "Alice",
        receiverId = "farmer1",
        content = "Hello",
        messageType = MessageType.TEXT,
        isRead = false,
        createdAt = "2026-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        messageRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        chatRealtimeService = mockk(relaxed = true)
        every { chatRealtimeService.newMessages } returns MutableSharedFlow()
        viewModel = ChatViewModel(messageRepository, authRepository, chatRealtimeService)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is empty and not loading`() {
        val state = viewModel.uiState.value
        assertTrue(state.messages.isEmpty())
        assertFalse(state.isLoading)
        assertFalse(state.isSending)
        assertNull(state.error)
    }

    @Test
    fun `loadConversation sets error when user not authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel.loadConversation("conv1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("User not found", state.error)
    }

    @Test
    fun `loadConversation populates messages from flow`() = runTest {
        val messages = listOf(makeMessage("m1"), makeMessage("m2"))
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getMessagesForConversation("conv1") } returns flowOf(messages)

        viewModel.loadConversation("conv1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.messages.size)
        assertEquals("m1", state.messages[0].id)
        assertFalse(state.isLoading)
    }

    @Test
    fun `loadConversation subscribes to realtime and marks messages read`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getMessagesForConversation("conv1") } returns flowOf(emptyList())

        viewModel.loadConversation("conv1")
        testDispatcher.scheduler.advanceUntilIdle()

        verify { chatRealtimeService.subscribeToConversation("conv1") }
        coVerify { messageRepository.markMessagesAsRead("conv1", "user1") }
    }

    @Test
    fun `loadConversation stores current user id in state`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getMessagesForConversation("conv1") } returns flowOf(emptyList())

        viewModel.loadConversation("conv1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("user1", viewModel.uiState.value.currentUserId)
    }

    // -------------------------------------------------------------------------
    // updateMessage
    // -------------------------------------------------------------------------

    @Test
    fun `updateMessage updates current message in state`() {
        viewModel.updateMessage("Hello farmer")
        assertEquals("Hello farmer", viewModel.uiState.value.currentMessage)
    }

    @Test
    fun `updateMessage to empty string clears message`() {
        viewModel.updateMessage("draft")
        viewModel.updateMessage("")
        assertEquals("", viewModel.uiState.value.currentMessage)
    }

    // -------------------------------------------------------------------------
    // sendMessage
    // -------------------------------------------------------------------------

    @Test
    fun `sendMessage does nothing when message is blank`() = runTest {
        viewModel.updateMessage("   ")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { messageRepository.sendMessage(any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `sendMessage sets error when user not authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel.updateMessage("Hello")

        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSending)
        assertEquals("User not found", state.error)
    }

    @Test
    fun `sendMessage sets error when conversation not found`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationById(any()) } returns null
        viewModel.updateMessage("Hello")

        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSending)
        assertEquals("Conversation not found", state.error)
    }

    @Test
    fun `sendMessage clears input and isSending on success`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationById(any()) } returns mockConversation
        coEvery { messageRepository.sendMessage(any(), any(), any(), any(), any(), any()) } returns
            Result.success(makeMessage())
        viewModel.updateMessage("Hello")

        // Need a loaded conversation ID — simulate by loading first
        coEvery { messageRepository.getMessagesForConversation("conv1") } returns flowOf(emptyList())
        viewModel.loadConversation("conv1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateMessage("Hello")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.currentMessage)
        assertFalse(state.isSending)
        assertNull(state.error)
    }

    @Test
    fun `sendMessage sets error on repository failure`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { messageRepository.getConversationById(any()) } returns mockConversation
        coEvery { messageRepository.sendMessage(any(), any(), any(), any(), any(), any()) } returns
            Result.failure(Exception("Send failed"))
        coEvery { messageRepository.getMessagesForConversation("conv1") } returns flowOf(emptyList())

        viewModel.loadConversation("conv1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateMessage("Hello")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isSending)
        assertEquals("Send failed", state.error)
    }

    @Test
    fun `sendMessage derives receiver as participant2 when sender is participant1`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser  // id = "user1" = participant1Id
        coEvery { messageRepository.getConversationById(any()) } returns mockConversation
        val capturedReceiver = slot<String>()
        coEvery {
            messageRepository.sendMessage(any(), any(), any(), capture(capturedReceiver), any(), any())
        } returns Result.success(makeMessage())
        coEvery { messageRepository.getMessagesForConversation("conv1") } returns flowOf(emptyList())

        viewModel.loadConversation("conv1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.updateMessage("Hi Bob")
        viewModel.sendMessage()
        testDispatcher.scheduler.advanceUntilIdle()

        // participant1 sends → receiver should be participant2
        assertEquals("farmer1", capturedReceiver.captured)
    }
}
