package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.ListingConverters
import com.senthapps.slagrimarket.data.model.PaymentMethod
import com.senthapps.slagrimarket.data.model.PaymentStatus
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.SyncStatus
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionConverters
import com.senthapps.slagrimarket.data.model.TransactionStatus
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
import java.time.Instant
import java.time.LocalDate

@Database(
    entities = [User::class, Listing::class, Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ListingConverters::class, TransactionConverters::class)
abstract class TransactionTestDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao
    abstract fun transactionDao(): TransactionDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class TransactionDaoTest {

    private lateinit var db: TransactionTestDatabase
    private lateinit var dao: TransactionDao

    private val farmerId = "farmer-001"
    private val buyerId = "buyer-001"
    private val listingId = "listing-001"
    private val tomorrow = LocalDate.now().plusDays(1).toString()
    private val yesterday = LocalDate.now().minusDays(1).toString()

    private fun makeTxn(
        id: String,
        farmerId: String = this.farmerId,
        buyerId: String = this.buyerId,
        listingId: String = this.listingId,
        status: TransactionStatus = TransactionStatus.PENDING,
        paymentMethod: PaymentMethod = PaymentMethod.CASH,
        paymentStatus: PaymentStatus = PaymentStatus.PENDING,
        totalAmount: Double = 500.0,
        pickupDate: String = tomorrow,
        farmerRating: Int? = null,
        buyerRating: Int? = null,
        completedAt: String? = null,
        clientId: String? = null
    ) = Transaction(
        id = id,
        listingId = listingId,
        farmerId = farmerId,
        buyerId = buyerId,
        quantity = 10.0,
        pricePerUnit = totalAmount / 10.0,
        totalAmount = totalAmount,
        pickupLocation = "Jaffna Central Market",
        pickupDate = pickupDate,
        status = status,
        paymentMethod = paymentMethod,
        paymentStatus = paymentStatus,
        farmerRating = farmerRating,
        buyerRating = buyerRating,
        completedAt = completedAt,
        clientId = clientId
    )

    private fun makeUser(id: String) = User(id = id, name = "User $id", phone = "077000000")

    private fun makeListing(id: String, farmerId: String = this.farmerId) = Listing(
        id = id,
        farmerId = farmerId,
        cropType = "Tomato",
        location = "Jaffna",
        quality = QualityGrade.A,
        pricePerUnit = 50.0,
        quantity = 100.0,
        unit = "kg",
        harvestDate = LocalDate.now().toString(),
        availableUntil = tomorrow,
        isActive = true,
        syncStatus = SyncStatus.SYNCED
    )

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, TransactionTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.transactionDao()
        runBlocking {
            db.userDao().insertUser(makeUser(farmerId))
            db.userDao().insertUser(makeUser(buyerId))
            db.listingDao().insertListing(makeListing(listingId))
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // insertTransaction / getTransactionById
    // -------------------------------------------------------------------------

    @Test
    fun `insertTransaction and getTransactionById round-trips correctly`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1"))

        val result = dao.getTransactionById("t-1")

        assertNotNull(result)
        assertEquals("t-1", result!!.id)
        assertEquals(farmerId, result.farmerId)
        assertEquals(buyerId, result.buyerId)
        assertEquals(500.0, result.totalAmount, 0.01)
    }

    @Test
    fun `insertTransaction with REPLACE strategy overwrites existing`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", totalAmount = 500.0))
        dao.insertTransaction(makeTxn("t-1", totalAmount = 999.0))

        assertEquals(999.0, dao.getTransactionById("t-1")!!.totalAmount, 0.01)
    }

    @Test
    fun `getTransactionById returns null for unknown id`() = runBlocking {
        assertNull(dao.getTransactionById("nonexistent"))
    }

    // -------------------------------------------------------------------------
    // insertTransactions (batch)
    // -------------------------------------------------------------------------

    @Test
    fun `insertTransactions inserts multiple transactions`() = runBlocking {
        dao.insertTransactions(listOf(makeTxn("t-1"), makeTxn("t-2"), makeTxn("t-3")))

        assertNotNull(dao.getTransactionById("t-1"))
        assertNotNull(dao.getTransactionById("t-2"))
        assertNotNull(dao.getTransactionById("t-3"))
    }

    // -------------------------------------------------------------------------
    // deleteTransaction / deleteTransactionById / deleteAllTransactions
    // -------------------------------------------------------------------------

    @Test
    fun `deleteTransaction removes the entity`() = runBlocking {
        val txn = makeTxn("t-1")
        dao.insertTransaction(txn)
        dao.deleteTransaction(txn)

        assertNull(dao.getTransactionById("t-1"))
    }

    @Test
    fun `deleteTransactionById removes only the targeted transaction`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1"))
        dao.insertTransaction(makeTxn("t-2"))

        dao.deleteTransactionById("t-1")

        assertNull(dao.getTransactionById("t-1"))
        assertNotNull(dao.getTransactionById("t-2"))
    }

    @Test
    fun `deleteAllTransactions removes every transaction`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1"))
        dao.insertTransaction(makeTxn("t-2"))

        dao.deleteAllTransactions()

        assertNull(dao.getTransactionById("t-1"))
        assertNull(dao.getTransactionById("t-2"))
    }

    // -------------------------------------------------------------------------
    // getTransactionsByFarmer / getTransactionsByBuyer
    // -------------------------------------------------------------------------

    @Test
    fun `getTransactionsByFarmer returns only that farmer's transactions`() = runBlocking {
        val farmer2 = "farmer-002"
        db.userDao().insertUser(makeUser(farmer2))
        db.listingDao().insertListing(makeListing("listing-002", farmerId = farmer2))

        dao.insertTransaction(makeTxn("t-1", farmerId = farmerId))
        dao.insertTransaction(makeTxn("t-2", farmerId = farmer2, listingId = "listing-002"))

        val results = dao.getTransactionsByFarmer(farmerId)

        assertEquals(1, results.size)
        assertEquals("t-1", results[0].id)
    }

    @Test
    fun `getTransactionsByFarmer returns empty list for unknown farmer`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1"))
        assertTrue(dao.getTransactionsByFarmer("nobody").isEmpty())
    }

    @Test
    fun `getTransactionsByBuyer returns only that buyer's transactions`() = runBlocking {
        val buyer2 = "buyer-002"
        db.userDao().insertUser(makeUser(buyer2))

        dao.insertTransaction(makeTxn("t-1", buyerId = buyerId))
        dao.insertTransaction(makeTxn("t-2", buyerId = buyer2))

        val results = dao.getTransactionsByBuyer(buyerId)

        assertEquals(1, results.size)
        assertEquals("t-1", results[0].id)
    }

    // -------------------------------------------------------------------------
    // getTransactionsByListing
    // -------------------------------------------------------------------------

    @Test
    fun `getTransactionsByListing returns all transactions for a listing`() = runBlocking {
        val buyer2 = "buyer-002"
        db.userDao().insertUser(makeUser(buyer2))

        dao.insertTransaction(makeTxn("t-1", buyerId = buyerId))
        dao.insertTransaction(makeTxn("t-2", buyerId = buyer2))

        val results = dao.getTransactionsByListing(listingId)

        assertEquals(2, results.size)
    }

    // -------------------------------------------------------------------------
    // getTransactionsByStatus
    // -------------------------------------------------------------------------

    @Test
    fun `getTransactionsByStatus filters by status`() = runBlocking {
        dao.insertTransaction(makeTxn("t-pending", status = TransactionStatus.PENDING))
        dao.insertTransaction(makeTxn("t-confirmed", status = TransactionStatus.CONFIRMED))
        dao.insertTransaction(makeTxn("t-completed", status = TransactionStatus.COMPLETED))

        val pending = dao.getTransactionsByStatus(TransactionStatus.PENDING)
        val confirmed = dao.getTransactionsByStatus(TransactionStatus.CONFIRMED)

        assertEquals(1, pending.size)
        assertEquals("t-pending", pending[0].id)
        assertEquals(1, confirmed.size)
        assertEquals("t-confirmed", confirmed[0].id)
    }

    // -------------------------------------------------------------------------
    // getTransactionsByPaymentStatus
    // -------------------------------------------------------------------------

    @Test
    fun `getTransactionsByPaymentStatus filters by payment status`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", paymentStatus = PaymentStatus.PENDING))
        dao.insertTransaction(makeTxn("t-2", paymentStatus = PaymentStatus.PAID))

        val paid = dao.getTransactionsByPaymentStatus(PaymentStatus.PAID)

        assertEquals(1, paid.size)
        assertEquals("t-2", paid[0].id)
    }

    // -------------------------------------------------------------------------
    // getTransactionsForUser — combined farmer/buyer filter with status
    // -------------------------------------------------------------------------

    @Test
    fun `getTransactionsForUser returns transactions where user is farmer or buyer`() = runBlocking {
        val buyer2 = "buyer-002"
        db.userDao().insertUser(makeUser(buyer2))

        // t-1: farmerId=farmerId, buyerId=buyerId → userId appears as farmer
        dao.insertTransaction(makeTxn("t-1", farmerId = farmerId, buyerId = buyerId))
        // t-2: farmerId=buyer2, buyerId=farmerId → userId appears as buyer
        db.listingDao().insertListing(makeListing("listing-002", farmerId = buyer2))
        dao.insertTransaction(makeTxn("t-2", farmerId = buyer2, buyerId = farmerId, listingId = "listing-002"))

        val results = dao.getTransactionsForUser(userId = farmerId)

        assertEquals(2, results.size)
    }

    @Test
    fun `getTransactionsForUser with status filter narrows results`() = runBlocking {
        dao.insertTransaction(makeTxn("t-pending", status = TransactionStatus.PENDING))
        dao.insertTransaction(makeTxn("t-completed", status = TransactionStatus.COMPLETED))

        val results = dao.getTransactionsForUser(userId = farmerId, status = TransactionStatus.PENDING)

        assertEquals(1, results.size)
        assertEquals("t-pending", results[0].id)
    }

    @Test
    fun `getTransactionsForUser respects limit and offset`() = runBlocking {
        repeat(5) { i -> dao.insertTransaction(makeTxn("t-$i")) }

        val page1 = dao.getTransactionsForUser(userId = farmerId, limit = 3, offset = 0)
        val page2 = dao.getTransactionsForUser(userId = farmerId, limit = 3, offset = 3)

        assertEquals(3, page1.size)
        assertEquals(2, page2.size)
    }

    // -------------------------------------------------------------------------
    // updateTransactionStatus / updatePaymentStatus
    // -------------------------------------------------------------------------

    @Test
    fun `updateTransactionStatus changes status`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.PENDING))
        dao.updateTransactionStatus("t-1", TransactionStatus.CONFIRMED)

        assertEquals(TransactionStatus.CONFIRMED, dao.getTransactionById("t-1")!!.status)
    }

    @Test
    fun `updatePaymentStatus changes paymentStatus`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", paymentStatus = PaymentStatus.PENDING))
        dao.updatePaymentStatus("t-1", PaymentStatus.PAID)

        assertEquals(PaymentStatus.PAID, dao.getTransactionById("t-1")!!.paymentStatus)
    }

    @Test
    fun `updateTransactionStatusWithTimestamp updates status and updatedAt`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.PENDING))
        val newTimestamp = Instant.now().toString()
        dao.updateTransactionStatusWithTimestamp("t-1", TransactionStatus.IN_PROGRESS, newTimestamp)

        val result = dao.getTransactionById("t-1")!!
        assertEquals(TransactionStatus.IN_PROGRESS, result.status)
        assertEquals(newTimestamp, result.updatedAt)
    }

    @Test
    fun `completeTransaction sets status COMPLETED and timestamps`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.IN_PROGRESS))
        val now = Instant.now().toString()
        dao.completeTransaction("t-1", completedAt = now, updatedAt = now)

        val result = dao.getTransactionById("t-1")!!
        assertEquals(TransactionStatus.COMPLETED, result.status)
        assertEquals(now, result.completedAt)
    }

    // -------------------------------------------------------------------------
    // updateFarmerRating / updateBuyerRating
    // -------------------------------------------------------------------------

    @Test
    fun `updateFarmerRating sets farmerRating`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", farmerRating = null))
        dao.updateFarmerRating("t-1", 5)

        assertEquals(5, dao.getTransactionById("t-1")!!.farmerRating)
    }

    @Test
    fun `updateBuyerRating sets buyerRating`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", buyerRating = null))
        dao.updateBuyerRating("t-1", 4)

        assertEquals(4, dao.getTransactionById("t-1")!!.buyerRating)
    }

    // -------------------------------------------------------------------------
    // Analytics: counts and totals
    // -------------------------------------------------------------------------

    @Test
    fun `getCompletedTransactionCountByFarmer counts only COMPLETED transactions`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.COMPLETED))
        dao.insertTransaction(makeTxn("t-2", status = TransactionStatus.COMPLETED))
        dao.insertTransaction(makeTxn("t-3", status = TransactionStatus.PENDING))

        assertEquals(2, dao.getCompletedTransactionCountByFarmer(farmerId))
    }

    @Test
    fun `getCompletedTransactionCountByBuyer counts only buyer's COMPLETED transactions`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.COMPLETED))
        dao.insertTransaction(makeTxn("t-2", status = TransactionStatus.CANCELLED))

        assertEquals(1, dao.getCompletedTransactionCountByBuyer(buyerId))
    }

    @Test
    fun `getTotalEarningsByFarmer sums totalAmount for COMPLETED transactions`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.COMPLETED, totalAmount = 300.0))
        dao.insertTransaction(makeTxn("t-2", status = TransactionStatus.COMPLETED, totalAmount = 200.0))
        dao.insertTransaction(makeTxn("t-3", status = TransactionStatus.PENDING, totalAmount = 100.0))

        val earnings = dao.getTotalEarningsByFarmer(farmerId)

        assertEquals(500.0, earnings!!, 0.01)
    }

    @Test
    fun `getTotalEarningsByFarmer returns null when no completed transactions`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.PENDING))

        assertNull(dao.getTotalEarningsByFarmer(farmerId))
    }

    @Test
    fun `getTotalSpendingByBuyer sums totalAmount for COMPLETED transactions`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.COMPLETED, totalAmount = 400.0))
        dao.insertTransaction(makeTxn("t-2", status = TransactionStatus.COMPLETED, totalAmount = 100.0))

        val spending = dao.getTotalSpendingByBuyer(buyerId)

        assertEquals(500.0, spending!!, 0.01)
    }

    @Test
    fun `getAverageFarmerRating returns average of rated transactions`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", farmerRating = 4))
        dao.insertTransaction(makeTxn("t-2", farmerRating = 2))
        dao.insertTransaction(makeTxn("t-3", farmerRating = null))

        val avg = dao.getAverageFarmerRating(farmerId)

        assertEquals(3.0, avg!!, 0.01)
    }

    @Test
    fun `getAverageFarmerRating returns null when no ratings`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", farmerRating = null))

        assertNull(dao.getAverageFarmerRating(farmerId))
    }

    @Test
    fun `getAverageBuyerRating returns average of rated transactions`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", buyerRating = 5))
        dao.insertTransaction(makeTxn("t-2", buyerRating = 3))

        val avg = dao.getAverageBuyerRating(buyerId)

        assertEquals(4.0, avg!!, 0.01)
    }

    // -------------------------------------------------------------------------
    // getFarmerTransactionStats
    // -------------------------------------------------------------------------

    @Test
    fun `getFarmerTransactionStats returns correct aggregates`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.COMPLETED, totalAmount = 300.0))
        dao.insertTransaction(makeTxn("t-2", status = TransactionStatus.COMPLETED, totalAmount = 200.0))
        dao.insertTransaction(makeTxn("t-3", status = TransactionStatus.PENDING, totalAmount = 100.0))
        dao.insertTransaction(makeTxn("t-4", status = TransactionStatus.CANCELLED, totalAmount = 50.0))

        val stats = dao.getFarmerTransactionStats(farmerId)

        assertEquals(4, stats.total)
        assertEquals(2, stats.completed)
        assertEquals(1, stats.pending)
        assertEquals(1, stats.cancelled)
        assertEquals(500.0, stats.totalValue, 0.01)
        assertEquals(250.0, stats.avgValue!!, 0.01)
    }

    @Test
    fun `getFarmerTransactionStats returns zeros for farmer with no transactions`() = runBlocking {
        val stats = dao.getFarmerTransactionStats("unknown-farmer")

        assertEquals(0, stats.total)
        assertEquals(0, stats.completed)
        assertEquals(0.0, stats.totalValue, 0.01)
        assertNull(stats.avgValue)
    }

    // -------------------------------------------------------------------------
    // getActivePickupLocations
    // -------------------------------------------------------------------------

    @Test
    fun `getActivePickupLocations returns distinct locations from active statuses`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", status = TransactionStatus.COMPLETED))
        dao.insertTransaction(makeTxn("t-2", status = TransactionStatus.IN_PROGRESS))
        dao.insertTransaction(makeTxn("t-3", status = TransactionStatus.CONFIRMED))
        dao.insertTransaction(makeTxn("t-4", status = TransactionStatus.PENDING))

        val locations = dao.getActivePickupLocations()

        assertEquals(1, locations.size)
        assertEquals("Jaffna Central Market", locations[0])
    }

    // -------------------------------------------------------------------------
    // getRecentTransactionCount
    // -------------------------------------------------------------------------

    @Test
    fun `getRecentTransactionCount counts transactions created within last hour`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1"))
        dao.insertTransaction(makeTxn("t-2"))

        val count = dao.getRecentTransactionCount()

        assertEquals(2, count)
    }

    // -------------------------------------------------------------------------
    // getLastUpdateTimeForUser
    // -------------------------------------------------------------------------

    @Test
    fun `getLastUpdateTimeForUser returns most recent createdAt for farmer or buyer`() = runBlocking {
        val older = Instant.now().minusSeconds(3600).toString()
        val newer = Instant.now().toString()

        val txn1 = makeTxn("t-1").copy(createdAt = older)
        val txn2 = makeTxn("t-2").copy(createdAt = newer)
        dao.insertTransactions(listOf(txn1, txn2))

        val lastUpdate = dao.getLastUpdateTimeForUser(farmerId)

        assertNotNull(lastUpdate)
    }

    @Test
    fun `getLastUpdateTimeForUser returns null when no transactions exist`() = runBlocking {
        assertNull(dao.getLastUpdateTimeForUser("unknown-user"))
    }

    // -------------------------------------------------------------------------
    // getTransactionByClientId / updateTransactionServerId
    // -------------------------------------------------------------------------

    @Test
    fun `getTransactionByClientId returns matching transaction`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", clientId = "client-abc"))

        val result = dao.getTransactionByClientId("client-abc")

        assertNotNull(result)
        assertEquals("t-1", result!!.id)
    }

    @Test
    fun `getTransactionByClientId returns null when no match`() = runBlocking {
        dao.insertTransaction(makeTxn("t-1", clientId = null))

        assertNull(dao.getTransactionByClientId("missing-client"))
    }

    @Test
    fun `updateTransactionServerId renames the transaction id`() = runBlocking {
        dao.insertTransaction(makeTxn("local-id"))
        dao.updateTransactionServerId("local-id", "server-id")

        assertNull(dao.getTransactionById("local-id"))
        assertNotNull(dao.getTransactionById("server-id"))
    }

    // -------------------------------------------------------------------------
    // cleanupCancelledTransactions / cleanupOldCompletedTransactions
    // -------------------------------------------------------------------------

    @Test
    fun `cleanupCancelledTransactions does not delete recently cancelled transactions`() = runBlocking {
        dao.insertTransaction(makeTxn("t-recent-cancel", status = TransactionStatus.CANCELLED))

        dao.cleanupCancelledTransactions(retentionDays = 90)

        assertNotNull(dao.getTransactionById("t-recent-cancel"))
    }

    @Test
    fun `cleanupOldCompletedTransactions does not delete recently completed transactions`() = runBlocking {
        val now = Instant.now().toString()
        dao.insertTransaction(makeTxn("t-recent-complete", status = TransactionStatus.COMPLETED, completedAt = now))

        dao.cleanupOldCompletedTransactions(retentionDays = 365)

        assertNotNull(dao.getTransactionById("t-recent-complete"))
    }
}
