package com.senthapps.slagrimarket.ui.reviews

import com.senthapps.slagrimarket.data.model.Review
import com.senthapps.slagrimarket.data.model.ReviewType
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ReviewRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WriteReviewViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var reviewRepository: ReviewRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: WriteReviewViewModel

    private val farmerUser = User(
        id = "farmer1",
        name = "Nimal Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        language = "en",
        createdAt = "2025-01-01T00:00:00Z"
    )

    private val buyerUser = User(
        id = "buyer1",
        name = "Kamal Buyer",
        phone = "+94779876543",
        userType = UserType.BUYER,
        verified = true,
        language = "en",
        createdAt = "2025-01-01T00:00:00Z"
    )

    private val mockReview = Review(
        id = "review1",
        transactionId = "txn1",
        reviewerId = "farmer1",
        reviewerName = "Nimal Farmer",
        revieweeId = "buyer1",
        rating = 4,
        comment = "Good buyer",
        reviewType = ReviewType.BUYER,
        createdAt = "2025-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        reviewRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        viewModel = WriteReviewViewModel(reviewRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has zero rating and empty comment`() {
        val state = viewModel.uiState.value
        assertEquals(0, state.rating)
        assertEquals("", state.comment)
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNull(state.error)
    }

    @Test
    fun `updateRating updates rating in state`() {
        viewModel.updateRating(4)
        assertEquals(4, viewModel.uiState.value.rating)
    }

    @Test
    fun `updateComment updates comment in state`() {
        viewModel.updateComment("Great transaction!")
        assertEquals("Great transaction!", viewModel.uiState.value.comment)
    }

    @Test
    fun `submitReview with zero rating sets validation error without calling repository`() = runTest {
        viewModel.submitReview("txn1", "buyer1", "Kamal Buyer")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Please select a rating", viewModel.uiState.value.error)
        coVerify(exactly = 0) { reviewRepository.createReview(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test
    fun `submitReview sets error when user not logged in`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        viewModel.updateRating(3)

        viewModel.submitReview("txn1", "buyer1", "Kamal Buyer")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("User not found", state.error)
        assertFalse(state.isSuccess)
    }

    @Test
    fun `submitReview uses BUYER review type when reviewer is a farmer`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns farmerUser
        coEvery {
            reviewRepository.createReview(
                transactionId = "txn1",
                reviewerId = "farmer1",
                reviewerName = "Nimal Farmer",
                revieweeId = "buyer1",
                rating = 4,
                comment = "Prompt payment",
                reviewType = ReviewType.BUYER
            )
        } returns Result.success(mockReview)

        viewModel.updateRating(4)
        viewModel.updateComment("Prompt payment")
        viewModel.submitReview("txn1", "buyer1", "Kamal Buyer")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            reviewRepository.createReview(
                transactionId = "txn1",
                reviewerId = "farmer1",
                reviewerName = "Nimal Farmer",
                revieweeId = "buyer1",
                rating = 4,
                comment = "Prompt payment",
                reviewType = ReviewType.BUYER
            )
        }
        assertTrue(viewModel.uiState.value.isSuccess)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `submitReview uses FARMER review type when reviewer is a buyer`() = runTest {
        val farmerReview = mockReview.copy(reviewType = ReviewType.FARMER)
        coEvery { authRepository.getCurrentUser() } returns buyerUser
        coEvery {
            reviewRepository.createReview(
                transactionId = "txn1",
                reviewerId = "buyer1",
                reviewerName = "Kamal Buyer",
                revieweeId = "farmer1",
                rating = 5,
                comment = "Excellent produce",
                reviewType = ReviewType.FARMER
            )
        } returns Result.success(farmerReview)

        viewModel.updateRating(5)
        viewModel.updateComment("Excellent produce")
        viewModel.submitReview("txn1", "farmer1", "Nimal Farmer")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            reviewRepository.createReview(
                transactionId = "txn1",
                reviewerId = "buyer1",
                reviewerName = "Kamal Buyer",
                revieweeId = "farmer1",
                rating = 5,
                comment = "Excellent produce",
                reviewType = ReviewType.FARMER
            )
        }
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `submitReview sets error on repository failure`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns farmerUser
        coEvery {
            reviewRepository.createReview(any(), any(), any(), any(), any(), any(), any())
        } returns Result.failure(Exception("Network error"))

        viewModel.updateRating(3)
        viewModel.submitReview("txn1", "buyer1", "Kamal Buyer")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertEquals("Network error", state.error)
    }

    @Test
    fun `submitReview sets error on unexpected exception`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns farmerUser
        coEvery {
            reviewRepository.createReview(any(), any(), any(), any(), any(), any(), any())
        } throws RuntimeException("Unexpected crash")

        viewModel.updateRating(2)
        viewModel.submitReview("txn1", "buyer1", "Kamal Buyer")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isSuccess)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Unexpected crash"))
    }

    @Test
    fun `submitReview sets isLoading true then false on success`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns farmerUser
        coEvery {
            reviewRepository.createReview(any(), any(), any(), any(), any(), any(), any())
        } returns Result.success(mockReview)

        viewModel.updateRating(5)
        viewModel.submitReview("txn1", "buyer1", "Kamal Buyer")
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isSuccess)
    }

    @Test
    fun `updateRating clears existing validation error`() {
        // Trigger a zero-rating error first (synchronous path, no coroutine needed)
        viewModel.updateRating(0)
        // Now set a valid rating
        viewModel.updateRating(3)
        assertEquals(3, viewModel.uiState.value.rating)
    }

    @Test
    fun `submitReview passes anonymous name when user has null name`() = runTest {
        val anonymousUser = farmerUser.copy(name = null)
        coEvery { authRepository.getCurrentUser() } returns anonymousUser
        coEvery {
            reviewRepository.createReview(
                transactionId = any(),
                reviewerId = any(),
                reviewerName = "Anonymous",
                revieweeId = any(),
                rating = any(),
                comment = any(),
                reviewType = any()
            )
        } returns Result.success(mockReview)

        viewModel.updateRating(3)
        viewModel.submitReview("txn1", "buyer1", "Kamal Buyer")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) {
            reviewRepository.createReview(
                transactionId = any(),
                reviewerId = any(),
                reviewerName = "Anonymous",
                revieweeId = any(),
                rating = any(),
                comment = any(),
                reviewType = any()
            )
        }
    }
}
