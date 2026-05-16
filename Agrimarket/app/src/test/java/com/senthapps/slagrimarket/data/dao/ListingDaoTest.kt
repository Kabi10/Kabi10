package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.ListingConverters
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.SyncStatus
import com.senthapps.slagrimarket.data.model.User
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.time.LocalDate

@Database(entities = [User::class, Listing::class], version = 1, exportSchema = false)
@TypeConverters(ListingConverters::class)
abstract class ListingTestDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class ListingDaoTest {

    private lateinit var db: ListingTestDatabase
    private lateinit var dao: ListingDao

    private val farmerId = "farmer-001"
    private val tomorrow = LocalDate.now().plusDays(1).toString()
    private val yesterday = LocalDate.now().minusDays(1).toString()
    private val inThreeDays = LocalDate.now().plusDays(3).toString()

    private fun makeListing(
        id: String,
        farmerId: String = this.farmerId,
        cropType: String = "Tomato",
        location: String = "Colombo",
        quality: QualityGrade = QualityGrade.B,
        pricePerUnit: Double = 100.0,
        quantity: Double = 50.0,
        unit: String = "kg",
        isActive: Boolean = true,
        availableUntil: String = tomorrow,
        syncStatus: SyncStatus = SyncStatus.SYNCED,
        viewCount: Int = 0,
        clientId: String? = null
    ) = Listing(
        id = id,
        farmerId = farmerId,
        cropType = cropType,
        location = location,
        quality = quality,
        pricePerUnit = pricePerUnit,
        quantity = quantity,
        unit = unit,
        harvestDate = LocalDate.now().toString(),
        availableUntil = availableUntil,
        isActive = isActive,
        syncStatus = syncStatus,
        viewCount = viewCount,
        clientId = clientId
    )

    private fun makeUser(id: String = farmerId) = User(id = id, name = "Farmer $id", phone = "07700000")

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, ListingTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.listingDao()
        runBlocking { db.userDao().insertUser(makeUser()) }
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // insert / getListingById
    // -------------------------------------------------------------------------

    @Test
    fun `insertListing and getListingById round-trips correctly`() = runBlocking {
        val listing = makeListing("l-1")
        dao.insertListing(listing)

        val retrieved = dao.getListingById("l-1")

        assertNotNull(retrieved)
        assertEquals("l-1", retrieved!!.id)
        assertEquals(farmerId, retrieved.farmerId)
        assertEquals("Tomato", retrieved.cropType)
    }

    @Test
    fun `insertListing with REPLACE strategy overwrites existing`() = runBlocking {
        dao.insertListing(makeListing("l-1", pricePerUnit = 100.0))
        dao.insertListing(makeListing("l-1", pricePerUnit = 200.0))

        assertEquals(200.0, dao.getListingById("l-1")!!.pricePerUnit, 0.01)
    }

    @Test
    fun `getListingById returns null for unknown id`() = runBlocking {
        assertNull(dao.getListingById("unknown"))
    }

    // -------------------------------------------------------------------------
    // getListingsByFarmer
    // -------------------------------------------------------------------------

    @Test
    fun `getListingsByFarmer returns only that farmer's listings`() = runBlocking {
        val other = makeUser("farmer-002")
        db.userDao().insertUser(other)

        dao.insertListing(makeListing("l-1", farmerId = farmerId))
        dao.insertListing(makeListing("l-2", farmerId = "farmer-002"))

        val listings = dao.getListingsByFarmer(farmerId)

        assertEquals(1, listings.size)
        assertEquals("l-1", listings[0].id)
    }

    @Test
    fun `getListingsByFarmer returns empty list for unknown farmer`() = runBlocking {
        dao.insertListing(makeListing("l-1"))

        assertTrue(dao.getListingsByFarmer("nobody").isEmpty())
    }

    // -------------------------------------------------------------------------
    // getAllActiveListings — WHERE isActive = 1 AND availableUntil >= date('now')
    // -------------------------------------------------------------------------

    @Test
    fun `getAllActiveListings returns active non-expired listings`() = runBlocking {
        dao.insertListing(makeListing("active", isActive = true, availableUntil = tomorrow))
        dao.insertListing(makeListing("inactive", isActive = false, availableUntil = tomorrow))
        dao.insertListing(makeListing("expired", isActive = true, availableUntil = yesterday))

        val active = dao.getAllActiveListings()

        assertEquals(1, active.size)
        assertEquals("active", active[0].id)
    }

    @Test
    fun `getAllActiveListings returns empty when no active listings exist`() = runBlocking {
        dao.insertListing(makeListing("l-1", isActive = false))

        assertTrue(dao.getAllActiveListings().isEmpty())
    }

    // -------------------------------------------------------------------------
    // deactivateListing
    // -------------------------------------------------------------------------

    @Test
    fun `deactivateListing sets isActive to false`() = runBlocking {
        dao.insertListing(makeListing("l-1", isActive = true))
        dao.deactivateListing("l-1")

        assertFalse(dao.getListingById("l-1")!!.isActive)
    }

    @Test
    fun `deactivateListing removes listing from getAllActiveListings`() = runBlocking {
        dao.insertListing(makeListing("l-1"))
        dao.deactivateListing("l-1")

        assertTrue(dao.getAllActiveListings().isEmpty())
    }

    // -------------------------------------------------------------------------
    // updateSyncStatus
    // -------------------------------------------------------------------------

    @Test
    fun `updateSyncStatus changes sync status`() = runBlocking {
        dao.insertListing(makeListing("l-1", syncStatus = SyncStatus.PENDING))
        dao.updateSyncStatus("l-1", SyncStatus.SYNCED)

        assertEquals(SyncStatus.SYNCED, dao.getListingById("l-1")!!.syncStatus)
    }

    @Test
    fun `getListingsBySyncStatus returns only matching listings`() = runBlocking {
        dao.insertListing(makeListing("pending", syncStatus = SyncStatus.PENDING))
        dao.insertListing(makeListing("synced", syncStatus = SyncStatus.SYNCED))

        val pending = dao.getListingsBySyncStatus(SyncStatus.PENDING)

        assertEquals(1, pending.size)
        assertEquals("pending", pending[0].id)
    }

    @Test
    fun `getPendingSyncListings returns only PENDING listings`() = runBlocking {
        dao.insertListing(makeListing("p1", syncStatus = SyncStatus.PENDING))
        dao.insertListing(makeListing("s1", syncStatus = SyncStatus.SYNCED))

        val pending = dao.getPendingSyncListings()

        assertEquals(1, pending.size)
        assertEquals("p1", pending[0].id)
    }

    @Test
    fun `markAllPendingAsSynced updates all PENDING to SYNCED`() = runBlocking {
        dao.insertListing(makeListing("p1", syncStatus = SyncStatus.PENDING))
        dao.insertListing(makeListing("p2", syncStatus = SyncStatus.PENDING))
        dao.insertListing(makeListing("f1", syncStatus = SyncStatus.FAILED))

        dao.markAllPendingAsSynced()

        assertEquals(SyncStatus.SYNCED, dao.getListingById("p1")!!.syncStatus)
        assertEquals(SyncStatus.SYNCED, dao.getListingById("p2")!!.syncStatus)
        assertEquals(SyncStatus.FAILED, dao.getListingById("f1")!!.syncStatus)
    }

    // -------------------------------------------------------------------------
    // incrementViewCount / incrementInquiryCount
    // -------------------------------------------------------------------------

    @Test
    fun `incrementViewCount increases viewCount by 1`() = runBlocking {
        dao.insertListing(makeListing("l-1", viewCount = 5))
        dao.incrementViewCount("l-1")

        assertEquals(6, dao.getListingById("l-1")!!.viewCount)
    }

    @Test
    fun `incrementInquiryCount increases inquiryCount by 1`() = runBlocking {
        dao.insertListing(makeListing("l-1"))
        dao.incrementInquiryCount("l-1")

        assertEquals(1, dao.getListingById("l-1")!!.inquiryCount)
    }

    // -------------------------------------------------------------------------
    // getTrendingListings — viewCount > 0, ordered DESC
    // -------------------------------------------------------------------------

    @Test
    fun `getTrendingListings returns listings with viewCount above zero ordered by views`() = runBlocking {
        dao.insertListing(makeListing("popular", viewCount = 100))
        dao.insertListing(makeListing("medium", viewCount = 10))
        dao.insertListing(makeListing("no-views", viewCount = 0))

        val trending = dao.getTrendingListings(limit = 10)

        assertEquals(2, trending.size)
        assertEquals("popular", trending[0].id)
        assertEquals("medium", trending[1].id)
    }

    @Test
    fun `getTrendingListings respects limit`() = runBlocking {
        repeat(5) { i ->
            dao.insertListing(makeListing("l-$i", viewCount = i + 1))
        }

        val trending = dao.getTrendingListings(limit = 3)

        assertEquals(3, trending.size)
    }

    // -------------------------------------------------------------------------
    // getPremiumListings — quality = 'A'
    // -------------------------------------------------------------------------

    @Test
    fun `getPremiumListings returns only grade-A active listings`() = runBlocking {
        dao.insertListing(makeListing("grade-a", quality = QualityGrade.A))
        dao.insertListing(makeListing("grade-b", quality = QualityGrade.B))

        val premium = dao.getPremiumListings()

        assertEquals(1, premium.size)
        assertEquals("grade-a", premium[0].id)
    }

    // -------------------------------------------------------------------------
    // getActiveListingCountByFarmer
    // -------------------------------------------------------------------------

    @Test
    fun `getActiveListingCountByFarmer counts only active listings for farmer`() = runBlocking {
        dao.insertListing(makeListing("l-1", isActive = true))
        dao.insertListing(makeListing("l-2", isActive = true))
        dao.insertListing(makeListing("l-3", isActive = false))

        assertEquals(2, dao.getActiveListingCountByFarmer(farmerId))
    }

    // -------------------------------------------------------------------------
    // getAveragePriceForCrop
    // -------------------------------------------------------------------------

    @Test
    fun `getAveragePriceForCrop returns average price for crop type`() = runBlocking {
        dao.insertListing(makeListing("l-1", cropType = "Tomato", pricePerUnit = 100.0))
        dao.insertListing(makeListing("l-2", cropType = "Tomato", pricePerUnit = 200.0))
        dao.insertListing(makeListing("l-3", cropType = "Onion", pricePerUnit = 50.0))

        val avg = dao.getAveragePriceForCrop("Tomato")

        assertEquals(150.0, avg!!, 0.01)
    }

    @Test
    fun `getAveragePriceForCrop returns null when no active listings for crop`() = runBlocking {
        val avg = dao.getAveragePriceForCrop("NonExistent")

        assertNull(avg)
    }

    // -------------------------------------------------------------------------
    // getAvailableCropTypes / getAvailableLocations
    // -------------------------------------------------------------------------

    @Test
    fun `getAvailableCropTypes returns distinct crop types from active non-expired listings`() = runBlocking {
        dao.insertListing(makeListing("l-1", cropType = "Tomato"))
        dao.insertListing(makeListing("l-2", cropType = "Tomato"))
        dao.insertListing(makeListing("l-3", cropType = "Onion"))
        dao.insertListing(makeListing("l-4", cropType = "Pepper", availableUntil = yesterday))

        val cropTypes = dao.getAvailableCropTypes()

        assertEquals(2, cropTypes.size)
        assertTrue(cropTypes.containsAll(listOf("Tomato", "Onion")))
    }

    @Test
    fun `getAvailableLocations returns distinct locations from active non-expired listings`() = runBlocking {
        dao.insertListing(makeListing("l-1", location = "Colombo"))
        dao.insertListing(makeListing("l-2", location = "Colombo"))
        dao.insertListing(makeListing("l-3", location = "Jaffna"))

        val locations = dao.getAvailableLocations()

        assertEquals(2, locations.size)
    }

    // -------------------------------------------------------------------------
    // getListingByClientId
    // -------------------------------------------------------------------------

    @Test
    fun `getListingByClientId returns listing with matching clientId`() = runBlocking {
        dao.insertListing(makeListing("l-1", clientId = "client-abc"))

        val result = dao.getListingByClientId("client-abc")

        assertNotNull(result)
        assertEquals("l-1", result!!.id)
    }

    @Test
    fun `getListingByClientId returns null when no match`() = runBlocking {
        dao.insertListing(makeListing("l-1", clientId = null))

        assertNull(dao.getListingByClientId("missing"))
    }

    // -------------------------------------------------------------------------
    // deleteListingById / deleteAllListings
    // -------------------------------------------------------------------------

    @Test
    fun `deleteListingById removes only the targeted listing`() = runBlocking {
        dao.insertListing(makeListing("l-1"))
        dao.insertListing(makeListing("l-2"))

        dao.deleteListingById("l-1")

        assertNull(dao.getListingById("l-1"))
        assertNotNull(dao.getListingById("l-2"))
    }

    @Test
    fun `deleteAllListings removes every listing`() = runBlocking {
        dao.insertListing(makeListing("l-1"))
        dao.insertListing(makeListing("l-2"))

        dao.deleteAllListings()

        assertTrue(dao.getAllActiveListings().isEmpty())
    }

    // -------------------------------------------------------------------------
    // getExpiringSoonListingsByFarmer — 0..3 days window
    // -------------------------------------------------------------------------

    @Test
    fun `getExpiringSoonListingsByFarmer returns listings expiring within 3 days`() = runBlocking {
        val inTwoDays = LocalDate.now().plusDays(2).toString()
        val inFiveDays = LocalDate.now().plusDays(5).toString()

        dao.insertListing(makeListing("soon", availableUntil = inTwoDays))
        dao.insertListing(makeListing("later", availableUntil = inFiveDays))

        val expiring = dao.getExpiringSoonListingsByFarmer(farmerId)

        assertEquals(1, expiring.size)
        assertEquals("soon", expiring[0].id)
    }

    // -------------------------------------------------------------------------
    // insertListings (batch)
    // -------------------------------------------------------------------------

    @Test
    fun `insertListings inserts multiple listings at once`() = runBlocking {
        val listings = listOf(makeListing("l-1"), makeListing("l-2"), makeListing("l-3"))
        dao.insertListings(listings)

        assertEquals(3, dao.getAllActiveListings().size)
    }
}
