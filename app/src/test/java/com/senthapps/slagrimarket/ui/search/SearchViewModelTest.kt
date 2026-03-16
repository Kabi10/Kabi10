package com.senthapps.slagrimarket.ui.search

import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.repository.ListingRepository
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
class SearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var listingRepository: ListingRepository
    private lateinit var viewModel: SearchViewModel

    private val tomatoListing = Listing(
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

    private val riceListing = Listing(
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

    private val allListings = listOf(tomatoListing, riceListing)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        listingRepository = mockk(relaxed = true)
        coEvery { listingRepository.getAllActiveListingsFlow() } returns flowOf(allListings)
        coEvery { listingRepository.searchListingsFlow(any(), any()) } returns flowOf(allListings)
        viewModel = SearchViewModel(listingRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `updateSearchQuery should update searchQuery in state`() = runTest {
        viewModel.updateSearchQuery("Tomatoes")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Tomatoes", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `performSearch with query should filter results by crop type`() = runTest {
        coEvery { listingRepository.getAllActiveListingsFlow() } returns flowOf(allListings)
        viewModel.updateSearchQuery("Tomatoes")

        viewModel.performSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.searchResults
        assertEquals(1, results.size)
        assertEquals("Tomatoes", results[0].cropType)
    }

    @Test
    fun `performSearch with location query should filter by location`() = runTest {
        coEvery { listingRepository.getAllActiveListingsFlow() } returns flowOf(allListings)
        viewModel.updateSearchQuery("Jaffna")

        viewModel.performSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.searchResults
        assertEquals(1, results.size)
        assertEquals("Jaffna", results[0].location)
    }

    @Test
    fun `performSearch with blank query should use filters path`() = runTest {
        coEvery { listingRepository.searchListingsFlow(null, null) } returns flowOf(allListings)
        viewModel.updateSearchQuery("")

        viewModel.performSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.searchResults
        assertEquals(2, results.size)
    }

    @Test
    fun `selectCropType should update selectedCropType in state`() = runTest {
        viewModel.selectCropType("Tomatoes")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Tomatoes", viewModel.uiState.value.selectedCropType)
    }

    @Test
    fun `selectLocation should update selectedLocation in state`() = runTest {
        viewModel.selectLocation("Jaffna")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals("Jaffna", viewModel.uiState.value.selectedLocation)
    }

    @Test
    fun `clearSearch should reset query and results`() = runTest {
        viewModel.updateSearchQuery("Tomatoes")
        viewModel.performSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("", state.searchQuery)
        assertTrue(state.searchResults.isEmpty())
    }

    @Test
    fun `clearFilters should reset cropType and location filters`() = runTest {
        viewModel.selectCropType("Tomatoes")
        viewModel.selectLocation("Jaffna")
        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.clearFilters()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.selectedCropType)
        assertNull(state.selectedLocation)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        viewModel.clearError()
        testDispatcher.scheduler.advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `performSearch should handle empty results gracefully`() = runTest {
        coEvery { listingRepository.getAllActiveListingsFlow() } returns flowOf(emptyList())
        viewModel.updateSearchQuery("NonExistentCrop")

        viewModel.performSearch()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.searchResults.isEmpty())
        assertNull(state.error)
    }
}
