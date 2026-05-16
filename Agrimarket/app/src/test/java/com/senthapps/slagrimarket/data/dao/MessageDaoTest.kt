package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senthapps.slagrimarket.data.model.Conversation
import com.senthapps.slagrimarket.data.model.Message
import com.senthapps.slagrimarket.data.model.MessageType
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

@Database(
    entities = [Message::class, Conversation::class],
    version = 1,
    exportSchema = false
)
abstract class MessageTestDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class MessageDaoTest {

    private lateinit var db: MessageTestDatabase
    private lateinit var dao: MessageDao

    private val user1 = "user-001"
    private val user2 = "user-002"
    private val user3 = "user-003"
    private val convId = "conv-001"
    private val ts = "2026-01-01T10:00:00Z"

    private fun makeConversation(
        id: String = convId,
        p1: String = user1,
        p2: String = user2,
        lastMessage: String? = "Hello",
        updatedAt: String = ts
    ) = Conversation(
        id = id,
        listingId = null,
        participant1Id = p1,
        participant1Name = "Farmer A",
        participant2Id = p2,
        participant2Name = "Buyer B",
        lastMessage = lastMessage,
        lastMessageTime = updatedAt,
        unreadCount = 0,
        createdAt = ts,
        updatedAt = updatedAt
    )

    private fun makeMessage(
        id: String,
        conversationId: String = convId,
        senderId: String = user1,
        receiverId: String = user2,
        content: String = "Hi",
        isRead: Boolean = false,
        createdAt: String = ts
    ) = Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        senderName = "Farmer A",
        receiverId = receiverId,
        content = content,
        messageType = MessageType.TEXT,
        isRead = isRead,
        createdAt = createdAt
    )

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, MessageTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.messageDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // insertConversation / getConversationById
    // -------------------------------------------------------------------------

    @Test
    fun `insertConversation and getConversationById round-trips correctly`() = runBlocking {
        val conv = makeConversation()
        dao.insertConversation(conv)

        val retrieved = dao.getConversationById(convId)

        assertNotNull(retrieved)
        assertEquals(convId, retrieved!!.id)
        assertEquals(user1, retrieved.participant1Id)
        assertEquals(user2, retrieved.participant2Id)
        assertEquals("Hello", retrieved.lastMessage)
    }

    @Test
    fun `getConversationById returns null when conversation does not exist`() = runBlocking {
        val result = dao.getConversationById("nonexistent")
        assertNull(result)
    }

    // -------------------------------------------------------------------------
    // getConversationsForUser
    // -------------------------------------------------------------------------

    @Test
    fun `getConversationsForUser returns conversations where user is participant1`() = runBlocking {
        dao.insertConversation(makeConversation(id = "conv-A", p1 = user1, p2 = user2))
        dao.insertConversation(makeConversation(id = "conv-B", p1 = user3, p2 = user2))

        val conversations = dao.getConversationsForUser(user1).first()

        assertEquals(1, conversations.size)
        assertEquals("conv-A", conversations[0].id)
    }

    @Test
    fun `getConversationsForUser returns conversations where user is participant2`() = runBlocking {
        dao.insertConversation(makeConversation(id = "conv-A", p1 = user1, p2 = user2))
        dao.insertConversation(makeConversation(id = "conv-B", p1 = user3, p2 = user1))

        val conversations = dao.getConversationsForUser(user1).first()

        assertEquals(2, conversations.size)
        val ids = conversations.map { it.id }.toSet()
        assertTrue(ids.contains("conv-A"))
        assertTrue(ids.contains("conv-B"))
    }

    @Test
    fun `getConversationsForUser returns empty list when user has no conversations`() = runBlocking {
        dao.insertConversation(makeConversation(id = "conv-A", p1 = user2, p2 = user3))

        val conversations = dao.getConversationsForUser(user1).first()

        assertTrue(conversations.isEmpty())
    }

    // -------------------------------------------------------------------------
    // getConversationBetweenUsers
    // -------------------------------------------------------------------------

    @Test
    fun `getConversationBetweenUsers finds conversation in participant1-participant2 order`() = runBlocking {
        dao.insertConversation(makeConversation(p1 = user1, p2 = user2))

        val conv = dao.getConversationBetweenUsers(user1, user2)

        assertNotNull(conv)
        assertEquals(convId, conv!!.id)
    }

    @Test
    fun `getConversationBetweenUsers finds conversation in reversed order`() = runBlocking {
        dao.insertConversation(makeConversation(p1 = user1, p2 = user2))

        val conv = dao.getConversationBetweenUsers(user2, user1)

        assertNotNull(conv)
        assertEquals(convId, conv!!.id)
    }

    @Test
    fun `getConversationBetweenUsers returns null when no conversation exists`() = runBlocking {
        val conv = dao.getConversationBetweenUsers(user1, user2)
        assertNull(conv)
    }

    // -------------------------------------------------------------------------
    // updateConversation / deleteConversation
    // -------------------------------------------------------------------------

    @Test
    fun `updateConversation persists lastMessage change`() = runBlocking {
        dao.insertConversation(makeConversation())
        val updated = makeConversation(lastMessage = "Updated message")
        dao.updateConversation(updated)

        val retrieved = dao.getConversationById(convId)
        assertEquals("Updated message", retrieved!!.lastMessage)
    }

    @Test
    fun `deleteConversation removes the conversation`() = runBlocking {
        dao.insertConversation(makeConversation())
        dao.deleteConversation(convId)

        val retrieved = dao.getConversationById(convId)
        assertNull(retrieved)
    }

    // -------------------------------------------------------------------------
    // insertMessage / getMessageById
    // -------------------------------------------------------------------------

    @Test
    fun `insertMessage and getMessageById round-trips correctly`() = runBlocking {
        dao.insertConversation(makeConversation())
        val msg = makeMessage(id = "msg-1", content = "First message")
        dao.insertMessage(msg)

        val retrieved = dao.getMessageById("msg-1")

        assertNotNull(retrieved)
        assertEquals("msg-1", retrieved!!.id)
        assertEquals("First message", retrieved.content)
        assertFalse(retrieved.isRead)
    }

    @Test
    fun `getMessageById returns null for nonexistent message`() = runBlocking {
        val result = dao.getMessageById("ghost")
        assertNull(result)
    }

    // -------------------------------------------------------------------------
    // getMessagesForConversation
    // -------------------------------------------------------------------------

    @Test
    fun `getMessagesForConversation returns messages in ascending createdAt order`() = runBlocking {
        dao.insertConversation(makeConversation())
        dao.insertMessage(makeMessage("msg-1", content = "Hello", createdAt = "2026-01-01T10:00:00Z"))
        dao.insertMessage(makeMessage("msg-2", content = "World", createdAt = "2026-01-01T10:01:00Z"))
        dao.insertMessage(makeMessage("msg-3", content = "!", createdAt = "2026-01-01T10:02:00Z"))

        val messages = dao.getMessagesForConversation(convId).first()

        assertEquals(3, messages.size)
        assertEquals("Hello", messages[0].content)
        assertEquals("World", messages[1].content)
        assertEquals("!", messages[2].content)
    }

    @Test
    fun `getMessagesForConversation does not return messages from other conversations`() = runBlocking {
        dao.insertConversation(makeConversation(id = "conv-A"))
        dao.insertConversation(makeConversation(id = "conv-B", p1 = user2, p2 = user3))
        dao.insertMessage(makeMessage("msg-A", conversationId = "conv-A"))
        dao.insertMessage(makeMessage("msg-B", conversationId = "conv-B"))

        val messages = dao.getMessagesForConversation("conv-A").first()

        assertEquals(1, messages.size)
        assertEquals("msg-A", messages[0].id)
    }

    // -------------------------------------------------------------------------
    // insertMessages (bulk)
    // -------------------------------------------------------------------------

    @Test
    fun `insertMessages inserts all messages atomically`() = runBlocking {
        dao.insertConversation(makeConversation())
        val msgs = listOf(
            makeMessage("bulk-1"),
            makeMessage("bulk-2"),
            makeMessage("bulk-3")
        )
        dao.insertMessages(msgs)

        val retrieved = dao.getMessagesForConversation(convId).first()
        assertEquals(3, retrieved.size)
    }

    // -------------------------------------------------------------------------
    // getUnreadMessageCount
    // -------------------------------------------------------------------------

    @Test
    fun `getUnreadMessageCount returns correct count for receiver`() = runBlocking {
        dao.insertConversation(makeConversation())
        dao.insertMessage(makeMessage("msg-1", receiverId = user2, isRead = false))
        dao.insertMessage(makeMessage("msg-2", receiverId = user2, isRead = true))
        dao.insertMessage(makeMessage("msg-3", receiverId = user2, isRead = false))

        val unread = dao.getUnreadMessageCount(convId, user2)

        assertEquals(2, unread)
    }

    @Test
    fun `getUnreadMessageCount returns zero when all messages are read`() = runBlocking {
        dao.insertConversation(makeConversation())
        dao.insertMessage(makeMessage("msg-1", receiverId = user2, isRead = true))
        dao.insertMessage(makeMessage("msg-2", receiverId = user2, isRead = true))

        val unread = dao.getUnreadMessageCount(convId, user2)

        assertEquals(0, unread)
    }

    @Test
    fun `getUnreadMessageCount only counts messages for specified receiver`() = runBlocking {
        dao.insertConversation(makeConversation())
        // Messages sent to user2 (unread)
        dao.insertMessage(makeMessage("msg-1", receiverId = user2, isRead = false))
        // Messages sent to user1 (unread) — should not count for user2
        dao.insertMessage(makeMessage("msg-2", senderId = user2, receiverId = user1, isRead = false))

        val unreadForUser2 = dao.getUnreadMessageCount(convId, user2)

        assertEquals(1, unreadForUser2)
    }

    // -------------------------------------------------------------------------
    // markMessagesAsRead
    // -------------------------------------------------------------------------

    @Test
    fun `markMessagesAsRead marks all unread messages as read for receiver`() = runBlocking {
        dao.insertConversation(makeConversation())
        dao.insertMessage(makeMessage("msg-1", receiverId = user2, isRead = false))
        dao.insertMessage(makeMessage("msg-2", receiverId = user2, isRead = false))
        dao.insertMessage(makeMessage("msg-3", receiverId = user2, isRead = true))

        dao.markMessagesAsRead(convId, user2)

        val unread = dao.getUnreadMessageCount(convId, user2)
        assertEquals(0, unread)
    }

    @Test
    fun `markMessagesAsRead does not affect messages in other conversations`() = runBlocking {
        dao.insertConversation(makeConversation(id = "conv-A"))
        dao.insertConversation(makeConversation(id = "conv-B", p1 = user2, p2 = user3))
        dao.insertMessage(makeMessage("msg-A", conversationId = "conv-A", receiverId = user2, isRead = false))
        dao.insertMessage(makeMessage("msg-B", conversationId = "conv-B", receiverId = user2, isRead = false))

        dao.markMessagesAsRead("conv-A", user2)

        val unreadInB = dao.getUnreadMessageCount("conv-B", user2)
        assertEquals(1, unreadInB)
    }

    // -------------------------------------------------------------------------
    // deleteMessage / deleteMessagesForConversation
    // -------------------------------------------------------------------------

    @Test
    fun `deleteMessage removes only the specified message`() = runBlocking {
        dao.insertConversation(makeConversation())
        dao.insertMessage(makeMessage("msg-1"))
        dao.insertMessage(makeMessage("msg-2"))

        dao.deleteMessage("msg-1")

        assertNull(dao.getMessageById("msg-1"))
        assertNotNull(dao.getMessageById("msg-2"))
    }

    @Test
    fun `deleteMessagesForConversation removes all messages in conversation`() = runBlocking {
        dao.insertConversation(makeConversation())
        dao.insertMessage(makeMessage("msg-1"))
        dao.insertMessage(makeMessage("msg-2"))
        dao.insertMessage(makeMessage("msg-3"))

        dao.deleteMessagesForConversation(convId)

        val messages = dao.getMessagesForConversation(convId).first()
        assertTrue(messages.isEmpty())
    }

    @Test
    fun `REPLACE strategy updates existing message on re-insert`() = runBlocking {
        dao.insertConversation(makeConversation())
        dao.insertMessage(makeMessage("msg-1", content = "Original"))
        dao.insertMessage(makeMessage("msg-1", content = "Replaced"))

        val retrieved = dao.getMessageById("msg-1")
        assertEquals("Replaced", retrieved!!.content)
    }
}
