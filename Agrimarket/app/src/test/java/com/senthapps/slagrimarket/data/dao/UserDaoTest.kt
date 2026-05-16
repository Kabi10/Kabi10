package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
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

@Database(entities = [User::class], version = 1, exportSchema = false)
abstract class UserTestDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class UserDaoTest {

    private lateinit var db: UserTestDatabase
    private lateinit var dao: UserDao

    private val now = Instant.now().toString()

    private fun makeUser(
        id: String,
        name: String = "Test User",
        phone: String? = null,
        userType: UserType = UserType.FARMER,
        location: String? = "Jaffna",
        verified: Boolean = false
    ) = User(
        id = id,
        name = name,
        phone = phone,
        userType = userType,
        location = location,
        verified = verified,
        createdAt = now
    )

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, UserTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.userDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // insertUser / getUserById
    // -------------------------------------------------------------------------

    @Test
    fun `insertUser and getUserById round-trips correctly`() = runBlocking {
        val user = makeUser("user-1", name = "Ravi", phone = "+94771234567")
        dao.insertUser(user)

        val retrieved = dao.getUserById("user-1")

        assertNotNull(retrieved)
        assertEquals("user-1", retrieved!!.id)
        assertEquals("Ravi", retrieved.name)
        assertEquals("+94771234567", retrieved.phone)
    }

    @Test
    fun `insertUser REPLACE strategy overwrites existing record`() = runBlocking {
        dao.insertUser(makeUser("user-1", name = "Old Name"))
        dao.insertUser(makeUser("user-1", name = "New Name"))

        assertEquals("New Name", dao.getUserById("user-1")!!.name)
    }

    @Test
    fun `getUserById returns null for missing id`() = runBlocking {
        assertNull(dao.getUserById("nonexistent"))
    }

    // -------------------------------------------------------------------------
    // getUserByPhone
    // -------------------------------------------------------------------------

    @Test
    fun `getUserByPhone returns correct user`() = runBlocking {
        dao.insertUser(makeUser("u1", phone = "+94770000001"))
        dao.insertUser(makeUser("u2", phone = "+94770000002"))

        val found = dao.getUserByPhone("+94770000001")

        assertNotNull(found)
        assertEquals("u1", found!!.id)
    }

    @Test
    fun `getUserByPhone returns null when phone not found`() = runBlocking {
        dao.insertUser(makeUser("u1", phone = "+94770000001"))

        assertNull(dao.getUserByPhone("+94779999999"))
    }

    // -------------------------------------------------------------------------
    // getUserByIdFlow
    // -------------------------------------------------------------------------

    @Test
    fun `getUserByIdFlow emits null for unknown id`() = runBlocking {
        val value = dao.getUserByIdFlow("unknown").first()
        assertNull(value)
    }

    @Test
    fun `getUserByIdFlow emits user after insert`() = runBlocking {
        val user = makeUser("u-flow")
        dao.insertUser(user)

        val value = dao.getUserByIdFlow("u-flow").first()

        assertNotNull(value)
        assertEquals("u-flow", value!!.id)
    }

    // -------------------------------------------------------------------------
    // insertUsers (batch)
    // -------------------------------------------------------------------------

    @Test
    fun `insertUsers inserts all users`() = runBlocking {
        val users = listOf(makeUser("u1"), makeUser("u2"), makeUser("u3"))
        dao.insertUsers(users)

        assertEquals(3, dao.getAllUsers().size)
    }

    // -------------------------------------------------------------------------
    // updateUser
    // -------------------------------------------------------------------------

    @Test
    fun `updateUser changes stored fields`() = runBlocking {
        dao.insertUser(makeUser("u1", location = "Colombo", verified = false))
        dao.updateUser(makeUser("u1", location = "Kandy", verified = true))

        val updated = dao.getUserById("u1")!!
        assertEquals("Kandy", updated.location)
        assertTrue(updated.verified)
    }

    // -------------------------------------------------------------------------
    // deleteUser / deleteUserById
    // -------------------------------------------------------------------------

    @Test
    fun `deleteUser removes the record`() = runBlocking {
        val user = makeUser("u-del")
        dao.insertUser(user)
        dao.deleteUser(user)

        assertNull(dao.getUserById("u-del"))
    }

    @Test
    fun `deleteUserById removes only the targeted user`() = runBlocking {
        dao.insertUser(makeUser("u1"))
        dao.insertUser(makeUser("u2"))

        dao.deleteUserById("u1")

        assertNull(dao.getUserById("u1"))
        assertNotNull(dao.getUserById("u2"))
    }

    // -------------------------------------------------------------------------
    // getAllUsers / getAllUsersFlow
    // -------------------------------------------------------------------------

    @Test
    fun `getAllUsers returns every inserted user`() = runBlocking {
        dao.insertUsers(listOf(makeUser("u1"), makeUser("u2")))

        assertEquals(2, dao.getAllUsers().size)
    }

    @Test
    fun `getAllUsersFlow emits current list`() = runBlocking {
        dao.insertUsers(listOf(makeUser("u1"), makeUser("u2")))

        val list = dao.getAllUsersFlow().first()

        assertEquals(2, list.size)
    }

    @Test
    fun `getAllUsers returns empty list when table is empty`() = runBlocking {
        assertTrue(dao.getAllUsers().isEmpty())
    }

    // -------------------------------------------------------------------------
    // deleteAllUsers
    // -------------------------------------------------------------------------

    @Test
    fun `deleteAllUsers clears the table`() = runBlocking {
        dao.insertUsers(listOf(makeUser("u1"), makeUser("u2")))
        dao.deleteAllUsers()

        assertTrue(dao.getAllUsers().isEmpty())
    }
}
