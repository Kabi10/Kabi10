package com.senthapps.slagrimarket.ui.listings

import app.cash.turbine.test
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.repository.ListingRepository
import io.mockk.coEvery
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
class ListingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ListingRepository
    private lateinit var viewModel: ListingsViewModel

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
        ),
        Listing(
            id = "2",
            farmerId = "farmer2",
            cropType = "Rice",
            quantity = 500.0,
            unit = "kg",
            pricePerUnit = 80.0,
            quality = QualityGrade.B,
            harvestDate = "2025-11-19",
            location = "Colombo",
            isActive = true,
            createdAt = "2025-11-19T10:00:00Z",
            updatedAt = "2025-11-19T10:00:00Z"
        )
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should have empty listings and not loading`() = runTest {
        // Given: Repository returns empty flow
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(emptyList())

        // When: ViewModel is created
        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have empty listings and not be loading
        val state = viewModel.uiState.value
        assertTrue(state.listings.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadListings should emit success state with data`() = runTest {
        // Given: Repository returns mock listings
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)

        // When: ViewModel is created (which triggers loadListings)
        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have the listings
        val state = viewModel.uiState.value
        assertEquals(2, state.listings.size)
        assertEquals("Tomatoes", state.listings[0].cropType)
        assertEquals("Rice", state.listings[1].cropType)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `loadListings should emit error state on failure`() = runTest {
        // Given: Repository returns a flow that throws an exception when collected
        coEvery { repository.getAllActiveListingsFlow() } returns flow {
            throw RuntimeException("Network error")
        }

        // When: ViewModel is created
        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have error
        val state = viewModel.uiState.value
        assertTrue(state.listings.isEmpty())
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Network error") || state.error!!.contains("Failed"))
    }

    @Test
    fun `filterByCropType should filter listings by crop type`() = runTest {
        // Given: Repository returns filtered listings
        val tomatoListings = mockListings.filter { it.cropType == "Tomatoes" }
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)
        coEvery { repository.searchListingsFlow(cropType = "Tomatoes", location = null) } returns flowOf(tomatoListings)

        // When: ViewModel is created and filter is applied
        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByCropType("Tomatoes")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have filtered listings
        val state = viewModel.uiState.value
        assertEquals(1, state.listings.size)
        assertEquals("Tomatoes", state.listings[0].cropType)
    }

    @Test
    fun `filterByLocation should filter listings by location`() = runTest {
        // Given: Repository returns filtered listings
        val jaffnaListings = mockListings.filter { it.location == "Jaffna" }
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)
        coEvery { repository.searchListingsFlow(cropType = null, location = "Jaffna") } returns flowOf(jaffnaListings)

        // When: ViewModel is created and filter is applied
        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        
        viewModel.filterByLocation("Jaffna")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: State should have filtered listings
        val state = viewModel.uiState.value
        assertEquals(1, state.listings.size)
        assertEquals("Jaffna", state.listings[0].location)
    }

    @Test
    fun `clearError should reset error state`() = runTest {
        coEvery { repository.getAllActiveListingsFlow() } returns flow {
            throw RuntimeException("Error")
        }
        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `searchListings with blank query returns all listings`() = runTest {
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)

        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchListings("")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.listings.size)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `searchListings with crop type query filters matching listings`() = runTest {
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)

        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchListings("Tomato")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.listings.size)
        assertEquals("Tomatoes", state.listings[0].cropType)
        assertFalse(state.isLoading)
    }

    @Test
    fun `searchListings with location query filters matching listings`() = runTest {
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)

        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchListings("Colombo")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.listings.size)
        assertEquals("Colombo", state.listings[0].location)
    }

    @Test
    fun `searchListings with non-matching query returns empty list`() = runTest {
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)

        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchListings("NoMatch")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.listings.isEmpty())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `searchListings sets error state on repository exception`() = runTest {
        coEvery { repository.getAllActiveListingsFlow() } returnsMany listOf(
            flowOf(mockListings),
            flow { throw RuntimeException("Search failed") }
        )

        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.searchListings("query")
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
    }

    @Test
    fun `refreshListings reloads listings from repository`() = runTest {
        val updatedListings = mockListings.take(1)
        coEvery { repository.getAllActiveListingsFlow() } returnsMany listOf(
            flowOf(mockListings),
            flowOf(updatedListings)
        )

        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.listings.size)

        viewModel.refreshListings()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.listings.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `filterByCropType with null clears crop type filter`() = runTest {
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)
        coEvery { repository.searchListingsFlow(cropType = null, location = null) } returns flowOf(mockListings)

        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByCropType(null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.listings.size)
    }

    @Test
    fun `filterByLocation with null clears location filter`() = runTest {
        coEvery { repository.getAllActiveListingsFlow() } returns flowOf(mockListings)
        coEvery { repository.searchListingsFlow(cropType = null, location = null) } returns flowOf(mockListings)

        viewModel = ListingsViewModel(repository)
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.filterByLocation(null)
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.listings.size)
    }
}

