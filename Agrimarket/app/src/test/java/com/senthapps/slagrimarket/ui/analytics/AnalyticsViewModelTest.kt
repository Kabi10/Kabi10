package com.senthapps.slagrimarket.ui.analytics

import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.Transaction
import com.senthapps.slagrimarket.data.model.TransactionStatus
import com.senthapps.slagrimarket.data.model.User
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.data.repository.AuthRepository
import com.senthapps.slagrimarket.data.repository.ListingRepository
import com.senthapps.slagrimarket.data.repository.Resource
import com.senthapps.slagrimarket.data.repository.TransactionRepository
import io.mockk.coEvery
import io.mockk.mockk
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
class AnalyticsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var transactionRepository: TransactionRepository
    private lateinit var listingRepository: ListingRepository
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: AnalyticsViewModel

    private val mockUser = User(
        id = "farmer1",
        name = "Bob Farmer",
        phone = "+94771234567",
        userType = UserType.FARMER,
        verified = true,
        createdAt = "2026-01-01T00:00:00Z"
    )

    private fun makeListing(
        id: String,
        cropType: String = "Tomato",
        isActive: Boolean = true,
        viewCount: Int = 10
    ) = Listing(
        id = id,
        farmerId = "farmer1",
        cropType = cropType,
        quantity = 100.0,
        unit = "kg",
        pricePerUnit = 50.0,
        quality = QualityGrade.A,
        harvestDate = "2026-01-01",
        location = "Colombo",
        viewCount = viewCount,
        isActive = isActive,
        createdAt = "2026-01-01T00:00:00Z"
    )

    private fun makeTransaction(
        id: String,
        listingId: String,
        totalAmount: Double,
        status: TransactionStatus = TransactionStatus.COMPLETED
    ) = Transaction(
        id = id,
        listingId = listingId,
        farmerId = "farmer1",
        buyerId = "buyer1",
        quantity = 10.0,
        pricePerUnit = totalAmount / 10.0,
        totalAmount = totalAmount,
        pickupDate = "2026-01-10",
        pickupLocation = "Colombo",
        status = status,
        createdAt = "2026-01-05T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        transactionRepository = mockk(relaxed = true)
        listingRepository = mockk(relaxed = true)
        authRepository = mockk(relaxed = true)
        viewModel = AnalyticsViewModel(transactionRepository, listingRepository, authRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has zero values and is not loading`() {
        val state = viewModel.uiState.value
        assertEquals(0.0, state.totalRevenue, 0.001)
        assertEquals(0, state.totalOrders)
        assertEquals(0, state.activeListings)
        assertEquals(0, state.totalViews)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadAnalytics sets error when user not authenticated`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns null

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("User not authenticated", state.error)
    }

    @Test
    fun `loadAnalytics calculates total revenue from COMPLETED transactions only`() = runTest {
        val transactions = listOf(
            makeTransaction("t1", "l1", 500.0, TransactionStatus.COMPLETED),
            makeTransaction("t2", "l1", 300.0, TransactionStatus.PENDING),   // excluded
            makeTransaction("t3", "l1", 200.0, TransactionStatus.COMPLETED)
        )
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Success(transactions))
        coEvery { listingRepository.getListingsByFarmer("farmer1") } returns
            flowOf(Resource.Success(emptyList()))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        // 500 + 200 = 700, PENDING excluded
        assertEquals(700.0, viewModel.uiState.value.totalRevenue, 0.001)
    }

    @Test
    fun `loadAnalytics sets total orders to all transactions regardless of status`() = runTest {
        val transactions = listOf(
            makeTransaction("t1", "l1", 100.0, TransactionStatus.COMPLETED),
            makeTransaction("t2", "l1", 100.0, TransactionStatus.PENDING),
            makeTransaction("t3", "l1", 100.0, TransactionStatus.CANCELLED)
        )
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Success(transactions))
        coEvery { listingRepository.getListingsByFarmer("farmer1") } returns
            flowOf(Resource.Success(emptyList()))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(3, viewModel.uiState.value.totalOrders)
    }

    @Test
    fun `loadAnalytics counts only active listings`() = runTest {
        val listings = listOf(
            makeListing("l1", isActive = true),
            makeListing("l2", isActive = true),
            makeListing("l3", isActive = false)  // inactive — excluded
        )
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Success(emptyList()))
        coEvery { listingRepository.getListingsByFarmer("farmer1") } returns
            flowOf(Resource.Success(listings))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.activeListings)
    }

    @Test
    fun `loadAnalytics sums total views across all listings`() = runTest {
        val listings = listOf(
            makeListing("l1", viewCount = 15),
            makeListing("l2", viewCount = 25),
            makeListing("l3", viewCount = 10, isActive = false)
        )
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Success(emptyList()))
        coEvery { listingRepository.getListingsByFarmer("farmer1") } returns
            flowOf(Resource.Success(listings))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(50, viewModel.uiState.value.totalViews)
    }

    @Test
    fun `loadAnalytics builds popular crops sorted by revenue descending`() = runTest {
        val listings = listOf(
            makeListing("l1", cropType = "Tomato"),
            makeListing("l2", cropType = "Onion")
        )
        val transactions = listOf(
            makeTransaction("t1", "l1", 1000.0, TransactionStatus.COMPLETED),
            makeTransaction("t2", "l1", 500.0, TransactionStatus.COMPLETED),
            makeTransaction("t3", "l2", 2000.0, TransactionStatus.COMPLETED)
        )
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Success(transactions))
        coEvery { listingRepository.getListingsByFarmer("farmer1") } returns
            flowOf(Resource.Success(listings))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        val crops = viewModel.uiState.value.popularCrops
        assertEquals(2, crops.size)
        assertEquals("Onion", crops[0].name)    // 2000 revenue — first
        assertEquals("Tomato", crops[1].name)   // 1500 revenue — second
        assertEquals(2000.0, crops[0].revenue, 0.001)
        assertEquals(1500.0, crops[1].revenue, 0.001)
    }

    @Test
    fun `loadAnalytics pending transactions excluded from crop revenue`() = runTest {
        val listings = listOf(makeListing("l1", cropType = "Chili"))
        val transactions = listOf(
            makeTransaction("t1", "l1", 800.0, TransactionStatus.COMPLETED),
            makeTransaction("t2", "l1", 400.0, TransactionStatus.PENDING)  // excluded from revenue
        )
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Success(transactions))
        coEvery { listingRepository.getListingsByFarmer("farmer1") } returns
            flowOf(Resource.Success(listings))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        val crops = viewModel.uiState.value.popularCrops
        assertEquals(1, crops.size)
        assertEquals("Chili", crops[0].name)
        assertEquals(800.0, crops[0].revenue, 0.001)
        assertEquals(2, crops[0].count)  // count includes all statuses
    }

    @Test
    fun `loadAnalytics sets error when transaction resource is error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Error("Server unavailable"))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Server unavailable", state.error)
    }

    @Test
    fun `loadAnalytics sets error when listing resource is error`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Success(emptyList()))
        coEvery { listingRepository.getListingsByFarmer("farmer1") } returns
            flowOf(Resource.Error("DB error"))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("DB error", state.error)
    }

    @Test
    fun `loadAnalytics recent activity contains entries when data present`() = runTest {
        val listings = listOf(makeListing("l1"), makeListing("l2"))
        val transactions = listOf(
            makeTransaction("t1", "l1", 100.0),
            makeTransaction("t2", "l2", 200.0)
        )
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer("farmer1") } returns
            flowOf(Resource.Success(transactions))
        coEvery { listingRepository.getListingsByFarmer("farmer1") } returns
            flowOf(Resource.Success(listings))

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        // At most 5 items, at least 1 from 2 transactions + 2 listings
        val activity = viewModel.uiState.value.recentActivity
        assertTrue(activity.isNotEmpty())
        assertTrue(activity.size <= 5)
    }

    @Test
    fun `loadAnalytics sets error on unexpected exception`() = runTest {
        coEvery { authRepository.getCurrentUser() } returns mockUser
        coEvery { transactionRepository.getTransactionsByFarmer(any()) } throws RuntimeException("Boom")

        viewModel.loadAnalytics()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Failed to load analytics"))
    }
}
