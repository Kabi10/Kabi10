package com.senthapps.slagrimarket.ui.search

import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.repository.ListingRepository
import com.senthapps.slagrimarket.data.repository.Resource
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
class AdvancedSearchViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var listingRepository: ListingRepository
    private lateinit var viewModel: AdvancedSearchViewModel

    private fun makeListing(
        id: String,
        cropType: String = "Tomato",
        quality: QualityGrade = QualityGrade.A,
        pricePerUnit: Double = 50.0,
        location: String = "Colombo"
    ) = Listing(
        id = id,
        farmerId = "farmer1",
        cropType = cropType,
        quantity = 100.0,
        unit = "kg",
        pricePerUnit = pricePerUnit,
        quality = quality,
        harvestDate = "2026-01-01",
        location = location,
        isActive = true,
        createdAt = "2026-01-01T00:00:00Z"
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        listingRepository = mockk(relaxed = true)
        viewModel = AdvancedSearchViewModel(listingRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // -------------------------------------------------------------------------
    // initial state
    // -------------------------------------------------------------------------

    @Test
    fun `initial state is empty and isInitial is true`() {
        val state = viewModel.uiState.value
        assertEquals("", state.cropType)
        assertEquals("", state.quality)
        assertEquals("", state.minPrice)
        assertEquals("", state.maxPrice)
        assertEquals("", state.location)
        assertTrue(state.results.isEmpty())
        assertFalse(state.isLoading)
        assertTrue(state.isInitial)
        assertNull(state.error)
    }

    // -------------------------------------------------------------------------
    // filter updates
    // -------------------------------------------------------------------------

    @Test
    fun `updateCropType sets cropType in state`() {
        viewModel.updateCropType("Onion")
        assertEquals("Onion", viewModel.uiState.value.cropType)
    }

    @Test
    fun `updateQuality sets quality in state`() {
        viewModel.updateQuality("A")
        assertEquals("A", viewModel.uiState.value.quality)
    }

    @Test
    fun `updateMinPrice sets minPrice in state`() {
        viewModel.updateMinPrice("20.0")
        assertEquals("20.0", viewModel.uiState.value.minPrice)
    }

    @Test
    fun `updateMaxPrice sets maxPrice in state`() {
        viewModel.updateMaxPrice("100.0")
        assertEquals("100.0", viewModel.uiState.value.maxPrice)
    }

    @Test
    fun `updateLocation sets location in state`() {
        viewModel.updateLocation("Jaffna")
        assertEquals("Jaffna", viewModel.uiState.value.location)
    }

    @Test
    fun `clearFilters resets all state to defaults`() {
        viewModel.updateCropType("Chili")
        viewModel.updateQuality("B")
        viewModel.updateMinPrice("10.0")
        viewModel.updateMaxPrice("200.0")
        viewModel.updateLocation("Kandy")

        viewModel.clearFilters()

        val state = viewModel.uiState.value
        assertEquals("", state.cropType)
        assertEquals("", state.quality)
        assertEquals("", state.minPrice)
        assertEquals("", state.maxPrice)
        assertEquals("", state.location)
        assertTrue(state.isInitial)
    }

    // -------------------------------------------------------------------------
    // search — no filters applied
    // -------------------------------------------------------------------------

    @Test
    fun `search with no filters returns all repository results`() = runTest {
        val listings = listOf(
            makeListing("l1", quality = QualityGrade.A, pricePerUnit = 50.0),
            makeListing("l2", quality = QualityGrade.B, pricePerUnit = 80.0)
        )
        coEvery { listingRepository.searchListings(null, null, any()) } returns
            flowOf(Resource.Success(listings))

        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.results.size)
        assertFalse(state.isLoading)
        assertFalse(state.isInitial)
        assertNull(state.error)
    }

    // -------------------------------------------------------------------------
    // search — quality filter (client-side)
    // -------------------------------------------------------------------------

    @Test
    fun `search filters results by quality grade`() = runTest {
        val listings = listOf(
            makeListing("l1", quality = QualityGrade.A),
            makeListing("l2", quality = QualityGrade.B),
            makeListing("l3", quality = QualityGrade.A)
        )
        coEvery { listingRepository.searchListings(any(), any(), any()) } returns
            flowOf(Resource.Success(listings))

        viewModel.updateQuality("A")
        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.results
        assertEquals(2, results.size)
        assertTrue(results.all { it.quality == QualityGrade.A })
    }

    @Test
    fun `search with quality filter returns empty when no matches`() = runTest {
        val listings = listOf(
            makeListing("l1", quality = QualityGrade.B),
            makeListing("l2", quality = QualityGrade.C)
        )
        coEvery { listingRepository.searchListings(any(), any(), any()) } returns
            flowOf(Resource.Success(listings))

        viewModel.updateQuality("A")
        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.results.isEmpty())
    }

    // -------------------------------------------------------------------------
    // search — price range filter (client-side)
    // -------------------------------------------------------------------------

    @Test
    fun `search filters by minPrice`() = runTest {
        val listings = listOf(
            makeListing("l1", pricePerUnit = 30.0),
            makeListing("l2", pricePerUnit = 50.0),
            makeListing("l3", pricePerUnit = 80.0)
        )
        coEvery { listingRepository.searchListings(any(), any(), any()) } returns
            flowOf(Resource.Success(listings))

        viewModel.updateMinPrice("50.0")
        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.results
        assertEquals(2, results.size)
        assertTrue(results.all { it.pricePerUnit >= 50.0 })
    }

    @Test
    fun `search filters by maxPrice`() = runTest {
        val listings = listOf(
            makeListing("l1", pricePerUnit = 30.0),
            makeListing("l2", pricePerUnit = 50.0),
            makeListing("l3", pricePerUnit = 80.0)
        )
        coEvery { listingRepository.searchListings(any(), any(), any()) } returns
            flowOf(Resource.Success(listings))

        viewModel.updateMaxPrice("50.0")
        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.results
        assertEquals(2, results.size)
        assertTrue(results.all { it.pricePerUnit <= 50.0 })
    }

    @Test
    fun `search filters by price range minPrice and maxPrice together`() = runTest {
        val listings = listOf(
            makeListing("l1", pricePerUnit = 20.0),
            makeListing("l2", pricePerUnit = 50.0),
            makeListing("l3", pricePerUnit = 75.0),
            makeListing("l4", pricePerUnit = 120.0)
        )
        coEvery { listingRepository.searchListings(any(), any(), any()) } returns
            flowOf(Resource.Success(listings))

        viewModel.updateMinPrice("40.0")
        viewModel.updateMaxPrice("80.0")
        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.results
        assertEquals(2, results.size)
        assertTrue(results.all { it.pricePerUnit in 40.0..80.0 })
    }

    @Test
    fun `search ignores non-numeric price inputs`() = runTest {
        val listings = listOf(
            makeListing("l1", pricePerUnit = 50.0),
            makeListing("l2", pricePerUnit = 100.0)
        )
        coEvery { listingRepository.searchListings(any(), any(), any()) } returns
            flowOf(Resource.Success(listings))

        viewModel.updateMinPrice("not-a-number")
        viewModel.updateMaxPrice("")
        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        // No price filter applied — all results returned
        assertEquals(2, viewModel.uiState.value.results.size)
    }

    // -------------------------------------------------------------------------
    // search — combined filters
    // -------------------------------------------------------------------------

    @Test
    fun `search applies quality and price filters together`() = runTest {
        val listings = listOf(
            makeListing("l1", quality = QualityGrade.A, pricePerUnit = 30.0),
            makeListing("l2", quality = QualityGrade.A, pricePerUnit = 60.0),
            makeListing("l3", quality = QualityGrade.B, pricePerUnit = 60.0)
        )
        coEvery { listingRepository.searchListings(any(), any(), any()) } returns
            flowOf(Resource.Success(listings))

        viewModel.updateQuality("A")
        viewModel.updateMinPrice("50.0")
        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        val results = viewModel.uiState.value.results
        assertEquals(1, results.size)
        assertEquals("l2", results[0].id)
    }

    // -------------------------------------------------------------------------
    // search — error paths
    // -------------------------------------------------------------------------

    @Test
    fun `search sets error on Resource Error`() = runTest {
        coEvery { listingRepository.searchListings(any(), any(), any()) } returns
            flowOf(Resource.Error("Server error"))

        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals("Server error", state.error)
        assertTrue(state.results.isEmpty())
    }

    @Test
    fun `search sets error on exception`() = runTest {
        coEvery { listingRepository.searchListings(any(), any(), any()) } throws
            RuntimeException("Network timeout")

        viewModel.search()
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Search failed"))
    }
}
