package com.senthapps.slagrimarket.ui.listings

import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.FavoriteRepository
import com.senthapps.slagrimarket.data.repository.ListingRepository
import com.senthapps.slagrimarket.data.repository.Resource
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ListingDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var listingRepository: ListingRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var favoriteRepository: FavoriteRepository
    private lateinit var viewModel: ListingDetailViewModel

    private val mockUser = User(
        id = "user1",
        name = "Test Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        language = "en",
        createdAt = "2025-11-20T10:00:00Z"
    )

    private val mockListing = Listing(
        id = "listing1",
        farmerId = "farmer1",
        cropType = "Tomatoes",
        quantity = 100.0,
        unit = "kg",
        pricePerUnit = 50.0,
        quality = QualityGrade.A,
        harvestDate = "2025-11-20",
        location = "Jaffna",
        isActive = true,
        createdAt = "2025-11-20T10:00:00Z",
        updatedAt = "2025-11-20T10:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        listingRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        favoriteRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── loadListing ──────────────────────────────────────────────────────────

    @Test
    fun `loadListing success sets listing and clears loading`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { favoriteRepository.isFavorite("user1", "listing1") } returns false
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Success(mockListing))
        coEvery { listingRepository.incrementViewCount("listing1") } returns Resource.Success(Unit)

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(mockListing, state.listing)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadListing sets isFavorite when user is authenticated and listing is favorited`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { favoriteRepository.isFavorite("user1", "listing1") } returns true
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Success(mockListing))
        coEvery { listingRepository.incrementViewCount("listing1") } returns Resource.Success(Unit)

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun `loadListing skips favorite check when user is not authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Success(mockListing))
        coEvery { listingRepository.incrementViewCount("listing1") } returns Resource.Success(Unit)

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 0) { favoriteRepository.isFavorite(any(), any()) }
        assertFalse(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun `loadListing error sets error message and clears loading`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Error("Not found"))

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Not found"))
        assertNull(state.listing)
    }

    @Test
    fun `loadListing loading resource sets isLoading true`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Loading())

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loadListing exception sets error and clears loading`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { favoriteRepository.isFavorite(any(), any()) } throws RuntimeException("DB error")

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `loadListing increments view count on success`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Success(mockListing))
        coEvery { listingRepository.incrementViewCount("listing1") } returns Resource.Success(Unit)

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(atLeast = 1) { listingRepository.incrementViewCount("listing1") }
    }

    // ── toggleFavorite ───────────────────────────────────────────────────────

    @Test
    fun `toggleFavorite sets error when user not authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Please login to add favorites", viewModel.uiState.value.error)
        coVerify(exactly = 0) { favoriteRepository.toggleFavorite(any(), any()) }
    }

    @Test
    fun `toggleFavorite updates isFavorite to true on success`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Success(mockListing))
        coEvery { listingRepository.incrementViewCount("listing1") } returns Resource.Success(Unit)
        coEvery { favoriteRepository.isFavorite("user1", "listing1") } returns false
        coEvery { favoriteRepository.toggleFavorite("user1", "listing1") } returns Result.success(true)

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun `toggleFavorite updates isFavorite to false on success`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Success(mockListing))
        coEvery { listingRepository.incrementViewCount("listing1") } returns Resource.Success(Unit)
        coEvery { favoriteRepository.isFavorite("user1", "listing1") } returns true
        coEvery { favoriteRepository.toggleFavorite("user1", "listing1") } returns Result.success(false)

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isFavorite)
    }

    @Test
    fun `toggleFavorite does not update isFavorite on failure`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { listingRepository.getListingById("listing1") } returns
            flowOf(Resource.Success(mockListing))
        coEvery { listingRepository.incrementViewCount("listing1") } returns Resource.Success(Unit)
        coEvery { favoriteRepository.isFavorite("user1", "listing1") } returns true
        coEvery { favoriteRepository.toggleFavorite("user1", "listing1") } returns
            Result.failure(RuntimeException("Network error"))

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.loadListing("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.toggleFavorite()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isFavorite)
    }

    // ── incrementInquiryCount ────────────────────────────────────────────────

    @Test
    fun `incrementInquiryCount delegates to repository`() = runTest {
        coEvery { listingRepository.incrementInquiryCount("listing1") } returns Resource.Success(Unit)

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.incrementInquiryCount("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { listingRepository.incrementInquiryCount("listing1") }
    }

    @Test
    fun `incrementInquiryCount silently handles repository exception`() = runTest {
        coEvery { listingRepository.incrementInquiryCount("listing1") } throws RuntimeException("fail")

        viewModel = ListingDetailViewModel(listingRepository, authRepository, favoriteRepository)
        viewModel.incrementInquiryCount("listing1")
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }
}
