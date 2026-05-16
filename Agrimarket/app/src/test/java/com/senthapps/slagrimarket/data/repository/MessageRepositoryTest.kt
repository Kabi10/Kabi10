package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.CreateConversationData
import com.senthapps.slagrimarket.data.api.CreateConversationResponse
import com.senthapps.slagrimarket.data.api.MessageApiService
import com.senthapps.slagrimarket.data.api.SendMessageResponse
import com.senthapps.slagrimarket.data.api.MessageDto
import com.senthapps.slagrimarket.data.dao.MessageDao
import com.senthapps.slagrimarket.data.model.Conversation
import com.senthapps.slagrimarket.data.model.Message
import com.senthapps.slagrimarket.data.model.MessageType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MessageRepositoryTest {

    private lateinit var messageDao: MessageDao
    private lateinit var messageApiService: MessageApiService
    private lateinit var repository: MessageRepository

    private fun makeConversation(id: String = "conv1", unreadCount: Int = 2) = Conversation(
        id = id,
        listingId = null,
        participant1Id = "user1",
        participant1Name = "Alice",
        participant2Id = "farmer1",
        participant2Name = "Bob",
        lastMessage = "Previous",
        lastMessageTime = "2026-01-01T00:00:00Z",
        unreadCount = unreadCount,
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = "2026-01-01T00:00:00Z"
    )

    private fun makeMessageDto(id: String = "server-msg1") = MessageDto(
        id = id,
        conversationId = "conv1",
        senderId = "user1",
        senderName = "Alice",
        content = "Hello",
        messageType = "TEXT",
        isRead = false,
        createdAt = "2026-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        messageDao = mockk(relaxed = true)
        messageApiService = mockk(relaxed = true)
        repository = MessageRepository(messageDao, messageApiService)
    }

    // -------------------------------------------------------------------------
    // sendMessage — unread count regression
    // -------------------------------------------------------------------------

    @Test
    fun `sendMessage does NOT increment sender unread count`() = runTest {
        val conversation = makeConversation(unreadCount = 2)
        coEvery { messageDao.getConversationById("conv1") } returns conversation
        val updatedSlot = slot<Conversation>()
        coEvery { messageDao.updateConversation(capture(updatedSlot)) } just Runs
        coEvery { messageApiService.sendMessage(any(), any()) } throws Exception("Network error")

        val result = repository.sendMessage(
            conversationId = "conv1",
            senderId = "user1",
            senderName = "Alice",
            receiverId = "farmer1",
            content = "Hello"
        )

        assertTrue(result.isSuccess)
        // unreadCount must remain 2, not become 3
        assertEquals(2, updatedSlot.captured.unreadCount)
    }

    @Test
    fun `sendMessage updates last message preview`() = runTest {
        val conversation = makeConversation()
        coEvery { messageDao.getConversationById("conv1") } returns conversation
        val updatedSlot = slot<Conversation>()
        coEvery { messageDao.updateConversation(capture(updatedSlot)) } just Runs
        coEvery { messageApiService.sendMessage(any(), any()) } throws Exception("Network error")

        repository.sendMessage(
            conversationId = "conv1",
            senderId = "user1",
            senderName = "Alice",
            receiverId = "farmer1",
            content = "New message"
        )

        assertEquals("New message", updatedSlot.captured.lastMessage)
    }

    @Test
    fun `sendMessage inserts message locally before API call`() = runTest {
        coEvery { messageDao.getConversationById("conv1") } returns makeConversation()
        coEvery { messageApiService.sendMessage(any(), any()) } throws Exception("Network error")

        val result = repository.sendMessage(
            conversationId = "conv1",
            senderId = "user1",
            senderName = "Alice",
            receiverId = "farmer1",
            content = "Hello"
        )

        assertTrue(result.isSuccess)
        coVerify { messageDao.insertMessage(any()) }
    }

    @Test
    fun `sendMessage succeeds even when API throws`() = runTest {
        coEvery { messageDao.getConversationById("conv1") } returns makeConversation()
        coEvery { messageApiService.sendMessage(any(), any()) } throws Exception("Network error")

        val result = repository.sendMessage(
            conversationId = "conv1",
            senderId = "user1",
            senderName = "Alice",
            receiverId = "farmer1",
            content = "Hello"
        )

        assertTrue(result.isSuccess)
    }

    @Test
    fun `sendMessage returns failure when DAO insert throws`() = runTest {
        coEvery { messageDao.insertMessage(any()) } throws RuntimeException("DB full")

        val result = repository.sendMessage(
            conversationId = "conv1",
            senderId = "user1",
            senderName = "Alice",
            receiverId = "farmer1",
            content = "Hello"
        )

        assertTrue(result.isFailure)
    }

    // -------------------------------------------------------------------------
    // getOrCreateConversation
    // -------------------------------------------------------------------------

    @Test
    fun `getOrCreateConversation returns existing conversation from cache`() = runTest {
        val existing = makeConversation()
        coEvery { messageDao.getConversationBetweenUsers("user1", "farmer1") } returns existing

        val result = repository.getOrCreateConversation("user1", "Alice", "farmer1", "Bob")

        assertTrue(result.isSuccess)
        assertEquals("conv1", result.getOrNull()?.id)
        coVerify(exactly = 0) { messageApiService.createConversation(any()) }
    }

    @Test
    fun `getOrCreateConversation creates via API when not cached`() = runTest {
        coEvery { messageDao.getConversationBetweenUsers("user1", "farmer1") } returns null
        val apiResp = Response.success(
            CreateConversationResponse(
                success = true,
                data = CreateConversationData(id = "server-conv1", created = true, createdAt = "2026-01-01T00:00:00Z")
            )
        )
        coEvery { messageApiService.createConversation(any()) } returns apiResp

        val result = repository.getOrCreateConversation("user1", "Alice", "farmer1", "Bob")

        assertTrue(result.isSuccess)
        assertEquals("server-conv1", result.getOrNull()?.id)
        coVerify { messageDao.insertConversation(any()) }
    }

    @Test
    fun `getOrCreateConversation falls back to local when API throws`() = runTest {
        coEvery { messageDao.getConversationBetweenUsers("user1", "farmer1") } returns null
        coEvery { messageApiService.createConversation(any()) } throws Exception("Network error")

        val result = repository.getOrCreateConversation("user1", "Alice", "farmer1", "Bob")

        assertTrue(result.isSuccess)
        coVerify { messageDao.insertConversation(any()) }
    }

    // -------------------------------------------------------------------------
    // markMessagesAsRead
    // -------------------------------------------------------------------------

    @Test
    fun `markMessagesAsRead resets conversation unread count to zero`() = runTest {
        val conversation = makeConversation(unreadCount = 5)
        coEvery { messageDao.getConversationById("conv1") } returns conversation
        val updatedSlot = slot<Conversation>()
        coEvery { messageDao.updateConversation(capture(updatedSlot)) } just Runs

        repository.markMessagesAsRead("conv1", "user1")

        assertEquals(0, updatedSlot.captured.unreadCount)
        coVerify { messageDao.markMessagesAsRead("conv1", "user1") }
    }

    @Test
    fun `markMessagesAsRead succeeds locally even when API throws`() = runTest {
        coEvery { messageDao.getConversationById("conv1") } returns makeConversation()
        coEvery { messageApiService.markConversationRead(any()) } throws Exception("Network error")

        // Should not throw
        repository.markMessagesAsRead("conv1", "user1")

        coVerify { messageDao.markMessagesAsRead("conv1", "user1") }
    }

    // -------------------------------------------------------------------------
    // deleteConversation
    // -------------------------------------------------------------------------

    @Test
    fun `deleteConversation removes messages and conversation from DAO`() = runTest {
        val result = repository.deleteConversation("conv1")

        assertTrue(result.isSuccess)
        coVerify { messageDao.deleteMessagesForConversation("conv1") }
        coVerify { messageDao.deleteConversation("conv1") }
    }

    @Test
    fun `deleteConversation returns failure when DAO throws`() = runTest {
        coEvery { messageDao.deleteMessagesForConversation("conv1") } throws RuntimeException("DB error")

        val result = repository.deleteConversation("conv1")

        assertTrue(result.isFailure)
    }

    // -------------------------------------------------------------------------
    // getConversationsForUser / getMessagesForConversation (Flow delegates)
    // -------------------------------------------------------------------------

    @Test
    fun `getConversationsForUser delegates to DAO flow`() = runTest {
        val conversations = listOf(makeConversation())
        every { messageDao.getConversationsForUser("user1") } returns flowOf(conversations)

        val result = repository.getConversationsForUser("user1").first()

        assertEquals(conversations, result)
    }

    @Test
    fun `getMessagesForConversation delegates to DAO flow`() = runTest {
        val messages = listOf(
            Message("m1", "conv1", "user1", "Alice", "farmer1", "Hello", MessageType.TEXT, false, "2026-01-01T00:00:00Z")
        )
        every { messageDao.getMessagesForConversation("conv1") } returns flowOf(messages)

        val result = repository.getMessagesForConversation("conv1").first()

        assertEquals(messages, result)
    }
}
