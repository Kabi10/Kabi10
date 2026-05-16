package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.CreateReviewResponse
import com.senthapps.slagrimarket.data.api.ReviewApiService
import com.senthapps.slagrimarket.data.api.ReviewDto
import com.senthapps.slagrimarket.data.dao.ReviewDao
import com.senthapps.slagrimarket.data.model.Review
import com.senthapps.slagrimarket.data.model.ReviewType
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class ReviewRepositoryTest {

    private lateinit var reviewDao: ReviewDao
    private lateinit var reviewApiService: ReviewApiService
    private lateinit var repository: ReviewRepository

    private fun makeReview(id: String = "rev1", reviewerId: String = "user1") = Review(
        id = id,
        transactionId = "tx1",
        reviewerId = reviewerId,
        reviewerName = "Alice",
        revieweeId = "farmer1",
        rating = 4,
        comment = "Good quality",
        reviewType = ReviewType.BUYER_TO_FARMER,
        createdAt = "2026-01-01T00:00:00Z"
    )

    private fun makeDto(id: String = "server-rev1") = ReviewDto(
        id = id,
        transactionId = "tx1",
        reviewerId = "user1",
        reviewerName = "Alice",
        revieweeId = "farmer1",
        rating = 4,
        comment = "Good quality",
        reviewType = "BUYER_TO_FARMER",
        createdAt = "2026-01-01T00:00:00Z",
        updatedAt = null
    )

    @Before
    fun setup() {
        reviewDao = mockk(relaxed = true)
        reviewApiService = mockk(relaxed = true)
        repository = ReviewRepository(reviewDao, reviewApiService)
    }

    // -------------------------------------------------------------------------
    // createReview — server-rejection fix
    // -------------------------------------------------------------------------

    @Test
    fun `createReview succeeds with server-assigned id when API returns success`() = runTest {
        coEvery { reviewDao.getReviewsForTransaction("tx1") } returns emptyList()
        val apiResp = Response.success(CreateReviewResponse(success = true, data = makeDto("server-rev1")))
        coEvery { reviewApiService.createReview(any()) } returns apiResp

        val result = repository.createReview(
            transactionId = "tx1",
            reviewerId = "user1",
            reviewerName = "Alice",
            revieweeId = "farmer1",
            rating = 4,
            comment = "Good quality",
            reviewType = ReviewType.BUYER_TO_FARMER
        )

        assertTrue(result.isSuccess)
        assertEquals("server-rev1", result.getOrNull()?.id)
        coVerify { reviewDao.insertReview(any()) }
    }

    @Test
    fun `createReview returns failure when server explicitly rejects (4xx) — does NOT create locally`() = runTest {
        coEvery { reviewDao.getReviewsForTransaction("tx1") } returns emptyList()
        // 409 Conflict — duplicate review
        val errorResp = Response.error<CreateReviewResponse>(
            409, "duplicate review".toResponseBody()
        )
        coEvery { reviewApiService.createReview(any()) } returns errorResp

        val result = repository.createReview(
            transactionId = "tx1",
            reviewerId = "user1",
            reviewerName = "Alice",
            revieweeId = "farmer1",
            rating = 4,
            comment = "Good quality",
            reviewType = ReviewType.BUYER_TO_FARMER
        )

        assertTrue("Server rejection must propagate as failure", result.isFailure)
        // Critical: DAO must NOT be called — we must not persist a server-rejected review
        coVerify(exactly = 0) { reviewDao.insertReview(any()) }
    }

    @Test
    fun `createReview returns failure when server returns 403 Forbidden`() = runTest {
        coEvery { reviewDao.getReviewsForTransaction("tx1") } returns emptyList()
        val errorResp = Response.error<CreateReviewResponse>(
            403, "forbidden".toResponseBody()
        )
        coEvery { reviewApiService.createReview(any()) } returns errorResp

        val result = repository.createReview(
            transactionId = "tx1",
            reviewerId = "user1",
            reviewerName = "Alice",
            revieweeId = "farmer1",
            rating = 4,
            comment = "Good",
            reviewType = ReviewType.BUYER_TO_FARMER
        )

        assertTrue(result.isFailure)
        coVerify(exactly = 0) { reviewDao.insertReview(any()) }
    }

    @Test
    fun `createReview falls back to local-only when network throws (offline)`() = runTest {
        coEvery { reviewDao.getReviewsForTransaction("tx1") } returns emptyList()
        coEvery { reviewApiService.createReview(any()) } throws Exception("Network unreachable")

        val result = repository.createReview(
            transactionId = "tx1",
            reviewerId = "user1",
            reviewerName = "Alice",
            revieweeId = "farmer1",
            rating = 5,
            comment = "Excellent",
            reviewType = ReviewType.BUYER_TO_FARMER
        )

        // Offline fallback: still returns success so the local record is queued for sync
        assertTrue(result.isSuccess)
        coVerify(exactly = 1) { reviewDao.insertReview(any()) }
    }

    @Test
    fun `createReview returns failure immediately when duplicate found in local cache`() = runTest {
        val existing = listOf(makeReview(reviewerId = "user1"))
        coEvery { reviewDao.getReviewsForTransaction("tx1") } returns existing

        val result = repository.createReview(
            transactionId = "tx1",
            reviewerId = "user1",
            reviewerName = "Alice",
            revieweeId = "farmer1",
            rating = 3,
            comment = "Meh",
            reviewType = ReviewType.BUYER_TO_FARMER
        )

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("already reviewed") == true)
        coVerify(exactly = 0) { reviewApiService.createReview(any()) }
        coVerify(exactly = 0) { reviewDao.insertReview(any()) }
    }

    // -------------------------------------------------------------------------
    // updateReview
    // -------------------------------------------------------------------------

    @Test
    fun `updateReview returns failure when review not in local cache`() = runTest {
        coEvery { reviewDao.getReviewById("rev99") } returns null

        val result = repository.updateReview("rev99", rating = 3, comment = "Changed")

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
        coVerify(exactly = 0) { reviewDao.updateReview(any()) }
    }

    @Test
    fun `updateReview persists updated fields locally`() = runTest {
        val existing = makeReview()
        coEvery { reviewDao.getReviewById("rev1") } returns existing
        val updatedSlot = slot<Review>()
        coEvery { reviewDao.updateReview(capture(updatedSlot)) } just Runs

        val result = repository.updateReview("rev1", rating = 5, comment = "Updated comment")

        assertTrue(result.isSuccess)
        assertEquals(5, updatedSlot.captured.rating)
        assertEquals("Updated comment", updatedSlot.captured.comment)
    }

    @Test
    fun `updateReview still succeeds locally when API throws`() = runTest {
        val existing = makeReview()
        coEvery { reviewDao.getReviewById("rev1") } returns existing
        coEvery { reviewApiService.updateReview(any(), any()) } throws Exception("Network error")

        val result = repository.updateReview("rev1", rating = 2, comment = "Bad")

        assertTrue(result.isSuccess)
        coVerify { reviewDao.updateReview(any()) }
    }

    // -------------------------------------------------------------------------
    // deleteReview
    // -------------------------------------------------------------------------

    @Test
    fun `deleteReview removes from local DAO after API call`() = runTest {
        coEvery { reviewApiService.deleteReview("rev1") } returns mockk(relaxed = true)

        val result = repository.deleteReview("rev1")

        assertTrue(result.isSuccess)
        coVerify { reviewDao.deleteReview("rev1") }
    }

    @Test
    fun `deleteReview still deletes locally when API throws`() = runTest {
        coEvery { reviewApiService.deleteReview("rev1") } throws Exception("Network error")

        val result = repository.deleteReview("rev1")

        assertTrue(result.isSuccess)
        coVerify { reviewDao.deleteReview("rev1") }
    }

    // -------------------------------------------------------------------------
    // getUserRating
    // -------------------------------------------------------------------------

    @Test
    fun `getUserRating falls back to DAO aggregates when API throws`() = runTest {
        coEvery { reviewApiService.getReviewSummary("farmer1") } throws Exception("Network error")
        coEvery { reviewDao.getAverageRating("farmer1") } returns 4.2
        coEvery { reviewDao.getTotalReviews("farmer1") } returns 10
        coEvery { reviewDao.getReviewCountByStars("farmer1", 5) } returns 4
        coEvery { reviewDao.getReviewCountByStars("farmer1", 4) } returns 4
        coEvery { reviewDao.getReviewCountByStars("farmer1", 3) } returns 1
        coEvery { reviewDao.getReviewCountByStars("farmer1", 2) } returns 1
        coEvery { reviewDao.getReviewCountByStars("farmer1", 1) } returns 0

        val rating = repository.getUserRating("farmer1")

        assertEquals("farmer1", rating.userId)
        assertEquals(4.2, rating.averageRating, 0.001)
        assertEquals(10, rating.totalReviews)
        assertEquals(4, rating.fiveStars)
    }

    @Test
    fun `getUserRating returns zero rating on total failure`() = runTest {
        coEvery { reviewApiService.getReviewSummary(any()) } throws Exception("Network error")
        coEvery { reviewDao.getAverageRating(any()) } throws Exception("DB error")

        val rating = repository.getUserRating("farmer1")

        assertEquals(0.0, rating.averageRating, 0.001)
        assertEquals(0, rating.totalReviews)
    }
}
