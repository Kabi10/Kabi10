package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.senthapps.slagrimarket.data.model.Favorite
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.ListingConverters
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.SyncStatus
import com.senthapps.slagrimarket.data.model.User
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
import java.time.LocalDate

@Database(
    entities = [User::class, Listing::class, Favorite::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListingConverters::class)
abstract class FavoriteTestDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao
    abstract fun favoriteDao(): FavoriteDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class FavoriteDaoTest {

    private lateinit var db: FavoriteTestDatabase
    private lateinit var dao: FavoriteDao
    private lateinit var userDao: UserDao
    private lateinit var listingDao: ListingDao

    private val userId1 = "user-001"
    private val userId2 = "user-002"
    private val listingId1 = "listing-001"
    private val listingId2 = "listing-002"
    private val now = Instant.now().toString()
    private val tomorrow = LocalDate.now().plusDays(1).toString()

    private fun makeUser(id: String) = User(id = id, name = "Test User $id")

    private fun makeListing(id: String, farmerId: String = userId1) = Listing(
        id = id,
        farmerId = farmerId,
        cropType = "Tomato",
        quantity = 50.0,
        unit = "kg",
        pricePerUnit = 100.0,
        quality = QualityGrade.A,
        harvestDate = tomorrow,
        location = "Jaffna",
        syncStatus = SyncStatus.SYNCED
    )

    private fun makeFavorite(
        id: String,
        userId: String = userId1,
        listingId: String = listingId1
    ) = Favorite(id = id, userId = userId, listingId = listingId, createdAt = now)

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, FavoriteTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.favoriteDao()
        userDao = db.userDao()
        listingDao = db.listingDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    // ---- getFavorite ----

    @Test
    fun `getFavorite returns null when not favorited`() = runBlocking {
        assertNull(dao.getFavorite(userId1, listingId1))
    }

    @Test
    fun `getFavorite returns favorite after insert`() = runBlocking {
        dao.insertFavorite(makeFavorite("fav-1"))
        val result = dao.getFavorite(userId1, listingId1)
        assertNotNull(result)
        assertEquals("fav-1", result!!.id)
    }

    // ---- isFavorite ----

    @Test
    fun `isFavorite returns false for unknown combination`() = runBlocking {
        assertFalse(dao.isFavorite(userId1, listingId1))
    }

    @Test
    fun `isFavorite returns true after inserting favorite`() = runBlocking {
        dao.insertFavorite(makeFavorite("fav-1"))
        assertTrue(dao.isFavorite(userId1, listingId1))
    }

    @Test
    fun `isFavorite is user-specific`() = runBlocking {
        dao.insertFavorite(makeFavorite("fav-1", userId = userId1))
        assertFalse(dao.isFavorite(userId2, listingId1))
    }

    // ---- getFavoriteCount ----

    @Test
    fun `getFavoriteCount returns zero when no favorites`() = runBlocking {
        assertEquals(0, dao.getFavoriteCount(userId1))
    }

    @Test
    fun `getFavoriteCount returns correct count after inserts`() = runBlocking {
        dao.insertFavorite(makeFavorite("fav-1", userId = userId1, listingId = listingId1))
        dao.insertFavorite(makeFavorite("fav-2", userId = userId1, listingId = listingId2))
        dao.insertFavorite(makeFavorite("fav-3", userId = userId2, listingId = listingId1))
        assertEquals(2, dao.getFavoriteCount(userId1))
        assertEquals(1, dao.getFavoriteCount(userId2))
    }

    // ---- deleteFavorite ----

    @Test
    fun `deleteFavorite removes the specific entry`() = runBlocking {
        dao.insertFavorite(makeFavorite("fav-1", listingId = listingId1))
        dao.insertFavorite(makeFavorite("fav-2", listingId = listingId2))
        dao.deleteFavorite(userId1, listingId1)
        assertFalse(dao.isFavorite(userId1, listingId1))
        assertTrue(dao.isFavorite(userId1, listingId2))
    }

    @Test
    fun `deleteFavorite on non-existent entry does not throw`() = runBlocking {
        dao.deleteFavorite(userId1, listingId1)
        assertEquals(0, dao.getFavoriteCount(userId1))
    }

    // ---- deleteAllFavoritesForUser ----

    @Test
    fun `deleteAllFavoritesForUser removes all entries for that user`() = runBlocking {
        dao.insertFavorite(makeFavorite("fav-1", userId = userId1, listingId = listingId1))
        dao.insertFavorite(makeFavorite("fav-2", userId = userId1, listingId = listingId2))
        dao.insertFavorite(makeFavorite("fav-3", userId = userId2, listingId = listingId1))
        dao.deleteAllFavoritesForUser(userId1)
        assertEquals(0, dao.getFavoriteCount(userId1))
        assertEquals(1, dao.getFavoriteCount(userId2))
    }

    // ---- deleteFavoritesForListing ----

    @Test
    fun `deleteFavoritesForListing removes all entries for that listing`() = runBlocking {
        dao.insertFavorite(makeFavorite("fav-1", userId = userId1, listingId = listingId1))
        dao.insertFavorite(makeFavorite("fav-2", userId = userId2, listingId = listingId1))
        dao.insertFavorite(makeFavorite("fav-3", userId = userId1, listingId = listingId2))
        dao.deleteFavoritesForListing(listingId1)
        // user1 still has listingId2 favorited
        assertEquals(1, dao.getFavoriteCount(userId1))
        assertFalse(dao.isFavorite(userId2, listingId1))
    }

    // ---- getFavoritesForUser (Flow) ----

    @Test
    fun `getFavoritesForUser emits empty list when no favorites`() = runBlocking {
        val result = dao.getFavoritesForUser(userId1).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getFavoritesForUser emits favorites for the given user`() = runBlocking {
        dao.insertFavorite(makeFavorite("fav-1", userId = userId1))
        dao.insertFavorite(makeFavorite("fav-2", userId = userId2))
        val result = dao.getFavoritesForUser(userId1).first()
        assertEquals(1, result.size)
        assertEquals(userId1, result[0].userId)
    }

    // ---- getFavoriteListingsForUser (JOIN query) ----

    @Test
    fun `getFavoriteListingsForUser returns joined listing data`() = runBlocking {
        // Need User (FK) + Listing + Favorite
        userDao.insertUser(makeUser(userId1))
        listingDao.insertListing(makeListing(listingId1, farmerId = userId1))
        dao.insertFavorite(makeFavorite("fav-1", userId = userId2, listingId = listingId1))

        val result = dao.getFavoriteListingsForUser(userId2).first()
        assertEquals(1, result.size)
        assertEquals(listingId1, result[0].id)
    }

    @Test
    fun `getFavoriteListingsForUser returns empty when listing is not favorited`() = runBlocking {
        userDao.insertUser(makeUser(userId1))
        listingDao.insertListing(makeListing(listingId1, farmerId = userId1))

        val result = dao.getFavoriteListingsForUser(userId2).first()
        assertTrue(result.isEmpty())
    }

    // ---- insertFavorite REPLACE strategy ----

    @Test
    fun `insertFavorite with same id replaces existing entry`() = runBlocking {
        val original = makeFavorite("fav-1", listingId = listingId1)
        val replacement = makeFavorite("fav-1", listingId = listingId2)
        dao.insertFavorite(original)
        dao.insertFavorite(replacement)
        val result = dao.getFavorite(userId1, listingId2)
        assertNotNull(result)
        assertNull(dao.getFavorite(userId1, listingId1))
    }
}
