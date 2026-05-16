package com.senthapps.slagrimarket.data.dao

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.senthapps.slagrimarket.data.model.Review
import com.senthapps.slagrimarket.data.model.ReviewType
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

@Database(entities = [Review::class], version = 1, exportSchema = false)
abstract class ReviewTestDatabase : RoomDatabase() {
    abstract fun reviewDao(): ReviewDao
}

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [33])
class ReviewDaoTest {

    private lateinit var db: ReviewTestDatabase
    private lateinit var dao: ReviewDao

    private val farmerId = "farmer-001"
    private val buyerId = "buyer-001"
    private val ts = "2026-01-01T10:00:00Z"

    private fun makeReview(
        id: String,
        transactionId: String = "txn-001",
        reviewerId: String = buyerId,
        reviewerName: String = "Buyer One",
        revieweeId: String = farmerId,
        rating: Int = 5,
        comment: String = "Great produce",
        reviewType: ReviewType = ReviewType.BUYER_TO_FARMER,
        createdAt: String = ts
    ) = Review(
        id = id,
        transactionId = transactionId,
        reviewerId = reviewerId,
        reviewerName = reviewerName,
        revieweeId = revieweeId,
        rating = rating,
        comment = comment,
        reviewType = reviewType,
        createdAt = createdAt
    )

    @Before
    fun setup() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, ReviewTestDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.reviewDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // insertReview / getReviewById
    // -------------------------------------------------------------------------

    @Test
    fun `insertReview and getReviewById round-trips correctly`() = runBlocking {
        val review = makeReview("r1", rating = 4, comment = "Good quality")
        dao.insertReview(review)

        val retrieved = dao.getReviewById("r1")

        assertNotNull(retrieved)
        assertEquals("r1", retrieved!!.id)
        assertEquals(4, retrieved.rating)
        assertEquals("Good quality", retrieved.comment)
    }

    @Test
    fun `insertReview REPLACE strategy overwrites existing`() = runBlocking {
        dao.insertReview(makeReview("r1", rating = 3))
        dao.insertReview(makeReview("r1", rating = 5))

        assertEquals(5, dao.getReviewById("r1")!!.rating)
    }

    @Test
    fun `getReviewById returns null for unknown id`() = runBlocking {
        assertNull(dao.getReviewById("nonexistent"))
    }

    // -------------------------------------------------------------------------
    // insertReviews (batch)
    // -------------------------------------------------------------------------

    @Test
    fun `insertReviews inserts all items`() = runBlocking {
        dao.insertReviews(listOf(makeReview("r1"), makeReview("r2"), makeReview("r3")))

        assertNotNull(dao.getReviewById("r1"))
        assertNotNull(dao.getReviewById("r2"))
        assertNotNull(dao.getReviewById("r3"))
    }

    // -------------------------------------------------------------------------
    // getReviewsForUser (revieweeId)
    // -------------------------------------------------------------------------

    @Test
    fun `getReviewsForUser returns only reviews for that reviewee`() = runBlocking {
        dao.insertReview(makeReview("r1", revieweeId = farmerId))
        dao.insertReview(makeReview("r2", revieweeId = "other-farmer"))
        dao.insertReview(makeReview("r3", revieweeId = farmerId))

        val reviews = dao.getReviewsForUser(farmerId).first()

        assertEquals(2, reviews.size)
        assertTrue(reviews.all { it.revieweeId == farmerId })
    }

    @Test
    fun `getReviewsForUser returns empty when user has no reviews`() = runBlocking {
        val reviews = dao.getReviewsForUser("nobody").first()
        assertTrue(reviews.isEmpty())
    }

    // -------------------------------------------------------------------------
    // getReviewsByUser (reviewerId)
    // -------------------------------------------------------------------------

    @Test
    fun `getReviewsByUser returns only reviews written by that reviewer`() = runBlocking {
        dao.insertReview(makeReview("r1", reviewerId = buyerId))
        dao.insertReview(makeReview("r2", reviewerId = "other-buyer"))
        dao.insertReview(makeReview("r3", reviewerId = buyerId))

        val reviews = dao.getReviewsByUser(buyerId).first()

        assertEquals(2, reviews.size)
        assertTrue(reviews.all { it.reviewerId == buyerId })
    }

    // -------------------------------------------------------------------------
    // getReviewsForTransaction
    // -------------------------------------------------------------------------

    @Test
    fun `getReviewsForTransaction returns reviews for that transaction only`() = runBlocking {
        dao.insertReview(makeReview("r1", transactionId = "txn-A"))
        dao.insertReview(makeReview("r2", transactionId = "txn-B"))
        dao.insertReview(makeReview("r3", transactionId = "txn-A"))

        val reviews = dao.getReviewsForTransaction("txn-A")

        assertEquals(2, reviews.size)
        assertTrue(reviews.all { it.transactionId == "txn-A" })
    }

    @Test
    fun `getReviewsForTransaction returns empty for unknown transaction`() = runBlocking {
        assertTrue(dao.getReviewsForTransaction("unknown").isEmpty())
    }

    // -------------------------------------------------------------------------
    // getAverageRating
    // -------------------------------------------------------------------------

    @Test
    fun `getAverageRating returns correct average`() = runBlocking {
        dao.insertReview(makeReview("r1", revieweeId = farmerId, rating = 4))
        dao.insertReview(makeReview("r2", revieweeId = farmerId, rating = 2))

        val avg = dao.getAverageRating(farmerId)

        assertNotNull(avg)
        assertEquals(3.0, avg!!, 0.01)
    }

    @Test
    fun `getAverageRating returns null when no reviews exist`() = runBlocking {
        assertNull(dao.getAverageRating("nobody"))
    }

    @Test
    fun `getAverageRating ignores reviews for other users`() = runBlocking {
        dao.insertReview(makeReview("r1", revieweeId = farmerId, rating = 5))
        dao.insertReview(makeReview("r2", revieweeId = "other", rating = 1))

        val avg = dao.getAverageRating(farmerId)

        assertEquals(5.0, avg!!, 0.01)
    }

    // -------------------------------------------------------------------------
    // getTotalReviews
    // -------------------------------------------------------------------------

    @Test
    fun `getTotalReviews returns correct count`() = runBlocking {
        dao.insertReview(makeReview("r1", revieweeId = farmerId))
        dao.insertReview(makeReview("r2", revieweeId = farmerId))
        dao.insertReview(makeReview("r3", revieweeId = "other"))

        assertEquals(2, dao.getTotalReviews(farmerId))
    }

    @Test
    fun `getTotalReviews returns zero when no reviews`() = runBlocking {
        assertEquals(0, dao.getTotalReviews("nobody"))
    }

    // -------------------------------------------------------------------------
    // getReviewCountByStars
    // -------------------------------------------------------------------------

    @Test
    fun `getReviewCountByStars counts only matching star ratings`() = runBlocking {
        dao.insertReview(makeReview("r1", revieweeId = farmerId, rating = 5))
        dao.insertReview(makeReview("r2", revieweeId = farmerId, rating = 5))
        dao.insertReview(makeReview("r3", revieweeId = farmerId, rating = 3))

        assertEquals(2, dao.getReviewCountByStars(farmerId, 5))
        assertEquals(1, dao.getReviewCountByStars(farmerId, 3))
        assertEquals(0, dao.getReviewCountByStars(farmerId, 1))
    }

    // -------------------------------------------------------------------------
    // updateReview
    // -------------------------------------------------------------------------

    @Test
    fun `updateReview changes stored rating and comment`() = runBlocking {
        dao.insertReview(makeReview("r1", rating = 3, comment = "OK"))
        dao.updateReview(makeReview("r1", rating = 5, comment = "Excellent"))

        val updated = dao.getReviewById("r1")!!
        assertEquals(5, updated.rating)
        assertEquals("Excellent", updated.comment)
    }

    // -------------------------------------------------------------------------
    // deleteReview
    // -------------------------------------------------------------------------

    @Test
    fun `deleteReview removes only the targeted review`() = runBlocking {
        dao.insertReview(makeReview("r1"))
        dao.insertReview(makeReview("r2"))

        dao.deleteReview("r1")

        assertNull(dao.getReviewById("r1"))
        assertNotNull(dao.getReviewById("r2"))
    }

    // -------------------------------------------------------------------------
    // deleteReviewsForTransaction
    // -------------------------------------------------------------------------

    @Test
    fun `deleteReviewsForTransaction removes only reviews for that transaction`() = runBlocking {
        dao.insertReview(makeReview("r1", transactionId = "txn-del"))
        dao.insertReview(makeReview("r2", transactionId = "txn-del"))
        dao.insertReview(makeReview("r3", transactionId = "txn-keep"))

        dao.deleteReviewsForTransaction("txn-del")

        assertTrue(dao.getReviewsForTransaction("txn-del").isEmpty())
        assertEquals(1, dao.getReviewsForTransaction("txn-keep").size)
    }
}
