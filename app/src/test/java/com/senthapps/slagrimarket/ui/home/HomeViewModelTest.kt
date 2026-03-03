package com.senthapps.slagrimarket.ui.home

import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.PriceTrend
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.*
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
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
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var listingRepository: ListingRepository
    private lateinit var marketPriceRepository: MarketPriceRepository
    private lateinit var activityRepository: ActivityRepository
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var viewModel: HomeViewModel

    private val mockUser = User(
        id = "user1",
        name = "Test Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        language = "en",
        createdAt = "2025-11-20T10:00:00Z"
    )

    private val mockListings = listOf(
        Listing(
            id = "1",
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
    )

    private val mockMarketPrices = listOf(
        MarketPrice(
            id = "mp1",
            cropType = "Tomatoes",
            cropNameTamil = "தக்காளி",
            cropNameEnglish = "Tomatoes",
            cropNameSinhala = "තක්කාලි",
            currentPrice = 50.0,
            previousPrice = 48.0,
            unit = "kg",
            trend = PriceTrend.UP,
            changePercentage = 4.17,
            changeAmount = 2.0,
            location = "Jaffna",
            locationTamil = "யாழ்ப்பாணம்",
            locationSinhala = "යාපනය",
            lastUpdated = "2025-11-20T10:00:00Z"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        listingRepository = mockk(relaxed = true)
        marketPriceRepository = mockk(relaxed = true)
        activityRepository = mockk(relaxed = true)
        transactionRepository = mockk(relaxed = true)

        // Setup default mock behaviors
        every { authRepository.currentUser } returns flowOf(mockUser)
        coEvery { listingRepository.getAllActiveListingsFlow() } returns flowOf(mockListings)
        coEvery { marketPriceRepository.getAllMarketPrices(any()) } returns flowOf(Resource.Success(mockMarketPrices))
        coEvery { activityRepository.getActivitiesForUser(any(), any()) } returns flowOf(Resource.Success(emptyList()))
        coEvery { transactionRepository.getTransactionStatisticsResource(any()) } returns Resource.Success(mapOf("today_orders" to 5, "today_revenue" to 1000.0))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadAllData should load listings successfully`() = runTest {
        // When: ViewModel is created
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have listings
        val state = viewModel.uiState.value
        assertEquals(1, state.recentListings.size)
        assertEquals("Tomatoes", state.recentListings[0].cropType)
        assertFalse(state.isLoadingListings)
    }

    @Test
    fun `loadAllData should load market prices successfully`() = runTest {
        // When: ViewModel is created
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have market prices
        val state = viewModel.uiState.value
        assertEquals(1, state.marketPrices.size)
        assertEquals("Tomatoes", state.marketPrices[0].cropType)
        assertFalse(state.isLoadingPrices)
    }

    @Test
    fun `loadAllData should load statistics successfully`() = runTest {
        // When: ViewModel is created
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have statistics
        val state = viewModel.uiState.value
        assertEquals(5, state.todayOrders)
        assertEquals(1000.0, state.todayRevenue, 0.01)
        assertFalse(state.isLoadingStats)
    }

    @Test
    fun `loadAllData should handle listing error gracefully`() = runTest {
        // Given: Listing repository throws error
        coEvery { listingRepository.getAllActiveListingsFlow() } throws RuntimeException("Network error")

        // When: ViewModel is created
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should handle error gracefully
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingListings)
    }

    @Test
    fun `refreshData should update isRefreshing state`() = runTest {
        // Given: ViewModel is created
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When: refreshData is called
        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: isRefreshing should be false after completion
        assertFalse(viewModel.uiState.value.isRefreshing)
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        // Given: ViewModel with error
        coEvery { marketPriceRepository.getAllMarketPrices(any()) } returns flowOf(Resource.Error("Error"))
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When: clearError is called
        viewModel.clearError()

        // Then: Error should be null
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `loadListings emitsSuccess onNetworkSuccess`() = runTest {
        // Given: Flow returns a distinct listing to verify correct data is loaded
        val specificListings = listOf(
            Listing(
                id = "99",
                farmerId = "farmer99",
                cropType = "Onions",
                quantity = 75.0,
                unit = "kg",
                pricePerUnit = 40.0,
                quality = QualityGrade.A,
                harvestDate = "2025-11-22",
                location = "Kandy",
                isActive = true,
                createdAt = "2025-11-22T10:00:00Z",
                updatedAt = "2025-11-22T10:00:00Z"
            )
        )
        coEvery { listingRepository.getAllActiveListingsFlow() } returns flowOf(specificListings)

        // When: ViewModel is created and coroutines run
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: uiState reflects the loaded listings data
        val state = viewModel.uiState.value
        assertFalse(state.isLoadingListings)
        assertEquals(1, state.recentListings.size)
        assertEquals("Onions", state.recentListings.first().cropType)
        assertEquals(1, state.activeListingCount)
    }

    @Test
    fun `loadListings emitsError onNetworkFailure`() = runTest {
        // Given: Flow throws during emission — caught by .catch{} in ViewModel
        coEvery { listingRepository.getAllActiveListingsFlow() } returns flow {
            throw RuntimeException("Network connection lost")
        }

        // When: ViewModel is created and coroutines run
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: error is set from the .catch block, listings remain empty
        val state = viewModel.uiState.value
        assertEquals("Failed to load listings", state.error)
        assertFalse(state.isLoadingListings)
        assertTrue(state.recentListings.isEmpty())
    }

    @Test
    fun `refreshData callsRepositoryRefresh`() = runTest {
        // Given: ViewModel initialised
        viewModel = HomeViewModel(authRepository, listingRepository, marketPriceRepository, activityRepository, transactionRepository)
        testDispatcher.scheduler.advanceUntilIdle()

        // When: refreshData is called
        viewModel.refreshData()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: marketPriceRepository.refreshMarketPrices() called exactly once and refresh flag cleared
        coVerify(exactly = 1) { marketPriceRepository.refreshMarketPrices() }
        assertFalse(viewModel.uiState.value.isRefreshing)
    }
}

