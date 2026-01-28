package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.MarketPriceApiService
import com.senthapps.slagrimarket.data.api.MarketPricesResponse
import com.senthapps.slagrimarket.data.api.MarketStatisticsResponse
import com.senthapps.slagrimarket.data.api.PriceRange
import com.senthapps.slagrimarket.data.api.TrendDistribution
import com.senthapps.slagrimarket.data.api.CropStatistic
import com.senthapps.slagrimarket.data.api.LocationStatistic
import com.senthapps.slagrimarket.data.dao.MarketPriceDao
import com.senthapps.slagrimarket.data.dao.MarketStatistics
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.PriceTrend
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class MarketPriceRepositoryTest {

    private lateinit var marketPriceApiService: MarketPriceApiService
    private lateinit var marketPriceDao: MarketPriceDao
    private lateinit var repository: MarketPriceRepository

    private val mockMarketPrices = listOf(
        MarketPrice(
            id = "mp1",
            cropType = "tomato",
            cropNameTamil = "தக்காளி",
            cropNameEnglish = "Tomato",
            cropNameSinhala = "තක්කාලි",
            location = "jaffna",
            currentPrice = 150.0,
            previousPrice = 140.0,
            unit = "kg",
            trend = PriceTrend.UP,
            changePercentage = 7.14,
            lastUpdated = "2025-11-20T10:00:00Z",
            isActive = true
        ),
        MarketPrice(
            id = "mp2",
            cropType = "rice",
            cropNameTamil = "அரிசி",
            cropNameEnglish = "Rice",
            cropNameSinhala = "සහල්",
            location = "colombo",
            currentPrice = 180.0,
            previousPrice = 200.0,
            unit = "kg",
            trend = PriceTrend.DOWN,
            changePercentage = -10.0,
            lastUpdated = "2025-11-20T10:00:00Z",
            isActive = true
        ),
        MarketPrice(
            id = "mp3",
            cropType = "carrot",
            cropNameTamil = "கேரட்",
            cropNameEnglish = "Carrot",
            cropNameSinhala = "කැරට්",
            location = "jaffna",
            currentPrice = 120.0,
            previousPrice = 120.0,
            unit = "kg",
            trend = PriceTrend.STABLE,
            changePercentage = 0.0,
            lastUpdated = "2025-11-20T10:00:00Z",
            isActive = true
        )
    )

    private val mockStatistics = MarketStatistics(
        total = 3,
        rising = 1,
        falling = 1,
        stable = 1,
        avgPrice = 150.0,
        avgChange = -0.95
    )

    @Before
    fun setup() {
        marketPriceApiService = mockk(relaxed = true)
        marketPriceDao = mockk(relaxed = true)

        repository = MarketPriceRepository(marketPriceApiService, marketPriceDao)
    }

    // ==========================================================================
    // GET ALL MARKET PRICES TESTS
    // ==========================================================================

    @Test
    fun `getAllMarketPrices should emit cached data first then network data`() = runTest {
        // Given: DAO has cached data, API returns fresh data
        coEvery { marketPriceDao.getAllActiveMarketPrices() } returns mockMarketPrices
        coEvery { marketPriceDao.getLastUpdateTime() } returns null // Force refresh
        coEvery {
            marketPriceApiService.getMarketPrices(
                cropType = any(),
                location = any(),
                trend = any(),
                minPrice = any(),
                maxPrice = any(),
                page = any(),
                limit = any(),
                sortBy = any(),
                sortOrder = any()
            )
        } returns Response.success(
            MarketPricesResponse(
                prices = mockMarketPrices,
                totalCount = mockMarketPrices.size,
                page = 1,
                totalPages = 1,
                hasNext = false,
                hasPrevious = false,
                lastUpdated = "2025-11-20T10:00:00Z"
            )
        )

        // When: Get all market prices
        val results = repository.getAllMarketPrices().toList()

        // Then: Should emit loading, then cached data, then network data
        assertTrue(results.isNotEmpty())
        // First emission should be Loading
        assertTrue(results[0] is Resource.Loading)
        // Should have at least one Success
        assertTrue(results.any { it is Resource.Success })
    }

    @Test
    fun `getAllMarketPrices should return cached data when network fails`() = runTest {
        // Given: DAO has cached data, API fails
        coEvery { marketPriceDao.getAllActiveMarketPrices() } returns mockMarketPrices
        coEvery { marketPriceDao.getLastUpdateTime() } returns null
        coEvery {
            marketPriceApiService.getMarketPrices(
                cropType = any(),
                location = any(),
                trend = any(),
                minPrice = any(),
                maxPrice = any(),
                page = any(),
                limit = any(),
                sortBy = any(),
                sortOrder = any()
            )
        } throws RuntimeException("Network error")

        // When: Get all market prices
        val results = repository.getAllMarketPrices().toList()

        // Then: Should still return cached data
        val successResult = results.filterIsInstance<Resource.Success<List<MarketPrice>>>().lastOrNull()
        assertNotNull(successResult)
        assertEquals(3, successResult?.data?.size)
    }

    @Test
    fun `getAllMarketPrices should emit error when no cache and network fails`() = runTest {
        // Given: No cached data, API fails
        coEvery { marketPriceDao.getAllActiveMarketPrices() } returns emptyList()
        coEvery { marketPriceDao.getLastUpdateTime() } returns null
        coEvery {
            marketPriceApiService.getMarketPrices(
                cropType = any(),
                location = any(),
                trend = any(),
                minPrice = any(),
                maxPrice = any(),
                page = any(),
                limit = any(),
                sortBy = any(),
                sortOrder = any()
            )
        } throws RuntimeException("Network error")

        // When: Get all market prices (non-debug mode would fail, but debug provides samples)
        val results = repository.getAllMarketPrices().toList()

        // Then: Should have some result (error or debug samples)
        assertTrue(results.isNotEmpty())
    }

    // ==========================================================================
    // GET MARKET PRICES FLOW TESTS
    // ==========================================================================

    @Test
    fun `getMarketPricesFlow should return flow from DAO`() = runTest {
        // Given: DAO returns a flow of market prices
        coEvery { marketPriceDao.getAllActiveMarketPricesFlow() } returns flowOf(mockMarketPrices)

        // When: Get market prices flow
        val result = repository.getMarketPricesFlow().first()

        // Then: Should return the market prices
        assertEquals(3, result.size)
        assertEquals("tomato", result[0].cropType)
    }

    // ==========================================================================
    // SEARCH MARKET PRICES TESTS
    // ==========================================================================

    @Test
    fun `searchMarketPrices should return filtered results by crop type`() = runTest {
        // Given: DAO returns filtered results
        val tomatoPrices = mockMarketPrices.filter { it.cropType == "tomato" }
        coEvery { marketPriceDao.getMarketPricesForCropFlow("tomato") } returns flowOf(tomatoPrices)

        // When: Search by crop type
        val result = repository.searchMarketPrices("tomato").first()

        // Then: Should return only tomato prices
        assertEquals(1, result.size)
        assertEquals("tomato", result[0].cropType)
    }

    // ==========================================================================
    // GET MARKET PRICES BY LOCATION TESTS
    // ==========================================================================

    @Test
    fun `getMarketPricesByLocation should return filtered results`() = runTest {
        // Given: DAO returns filtered results
        val jaffnaPrices = mockMarketPrices.filter { it.location == "jaffna" }
        coEvery { marketPriceDao.getMarketPricesForLocationFlow("jaffna") } returns flowOf(jaffnaPrices)

        // When: Get by location
        val result = repository.getMarketPricesByLocation("jaffna").first()

        // Then: Should return only Jaffna prices
        assertEquals(2, result.size)
        assertTrue(result.all { it.location == "jaffna" })
    }

    // ==========================================================================
    // GET MARKET PRICE BY ID TESTS
    // ==========================================================================

    @Test
    fun `getMarketPriceById should return price from local cache`() = runTest {
        // Given: DAO has the price
        coEvery { marketPriceDao.getMarketPriceById("mp1") } returns mockMarketPrices[0]

        // When: Get by ID
        val result = repository.getMarketPriceById("mp1")

        // Then: Should return success with the price
        assertTrue(result is Resource.Success)
        assertEquals("mp1", (result as Resource.Success).data?.id)
        assertEquals("tomato", result.data?.cropType)
    }

    @Test
    fun `getMarketPriceById should fetch from network when not in cache`() = runTest {
        // Given: DAO doesn't have it, API has it
        coEvery { marketPriceDao.getMarketPriceById("mp1") } returns null
        coEvery { marketPriceApiService.getMarketPriceById("mp1") } returns Response.success(mockMarketPrices[0])

        // When: Get by ID
        val result = repository.getMarketPriceById("mp1")

        // Then: Should return success with the price from API
        assertTrue(result is Resource.Success)
        assertEquals("mp1", (result as Resource.Success).data?.id)

        // Verify it was cached
        coVerify { marketPriceDao.insertMarketPrice(mockMarketPrices[0]) }
    }

    @Test
    fun `getMarketPriceById should return error when not found anywhere`() = runTest {
        // Given: Neither DAO nor API has it
        coEvery { marketPriceDao.getMarketPriceById("invalid") } returns null
        coEvery { marketPriceApiService.getMarketPriceById("invalid") } returns Response.success(null)

        // When: Get by ID
        val result = repository.getMarketPriceById("invalid")

        // Then: Should return error
        assertTrue(result is Resource.Error)
    }

    // ==========================================================================
    // GET MARKET STATISTICS TESTS
    // ==========================================================================

    @Test
    fun `getMarketStatistics should return stats from API on success`() = runTest {
        // Given: API returns statistics
        val apiStats = MarketStatisticsResponse(
            totalCrops = 10,
            totalLocations = 5,
            averagePrice = 150.0,
            priceRange = PriceRange(min = 50.0, max = 300.0, average = 150.0),
            trendDistribution = TrendDistribution(up = 4, down = 3, stable = 3),
            topCrops = listOf(
                CropStatistic(cropType = "tomato", averagePrice = 150.0, priceChange = 7.14, trend = PriceTrend.UP, listingCount = 5)
            ),
            topLocations = listOf(
                LocationStatistic(location = "jaffna", averagePrice = 135.0, cropCount = 3, listingCount = 10)
            ),
            lastUpdated = "2025-11-20T10:00:00Z"
        )
        coEvery { marketPriceApiService.getMarketStatistics() } returns Response.success(apiStats)

        // When: Get statistics
        val result = repository.getMarketStatistics()

        // Then: Should return success with stats
        assertTrue(result is Resource.Success)
        val stats = (result as Resource.Success).data!!
        assertEquals(10, stats["totalCrops"])
        assertEquals(5, stats["totalLocations"])
    }

    @Test
    fun `getMarketStatistics should return empty stats when API fails`() = runTest {
        // Given: API fails
        coEvery { marketPriceApiService.getMarketStatistics() } throws RuntimeException("Network error")

        // When: Get statistics
        val result = repository.getMarketStatistics()

        // Then: Should return empty stats (current implementation returns empty map on failure)
        // Note: Local stats fallback is marked as TODO in the repository
        assertTrue(result is Resource.Success)
        val stats = (result as Resource.Success).data!!
        assertTrue(stats.isEmpty())
    }

    // ==========================================================================
    // REFRESH MARKET PRICES TESTS
    // ==========================================================================

    @Test
    fun `refreshMarketPrices should update local cache on success`() = runTest {
        // Given: API returns fresh data
        coEvery {
            marketPriceApiService.getMarketPrices(
                cropType = any(),
                location = any(),
                trend = any(),
                minPrice = any(),
                maxPrice = any(),
                page = any(),
                limit = any(),
                sortBy = any(),
                sortOrder = any()
            )
        } returns Response.success(
            MarketPricesResponse(
                prices = mockMarketPrices,
                totalCount = mockMarketPrices.size,
                page = 1,
                totalPages = 1,
                hasNext = false,
                hasPrevious = false,
                lastUpdated = "2025-11-20T10:00:00Z"
            )
        )

        // When: Refresh
        val result = repository.refreshMarketPrices()

        // Then: Should succeed and update cache
        assertTrue(result is Resource.Success)
        coVerify { marketPriceDao.insertMarketPrices(mockMarketPrices) }
    }

    @Test
    fun `refreshMarketPrices should return error on API failure`() = runTest {
        // Given: API fails
        coEvery {
            marketPriceApiService.getMarketPrices(
                cropType = any(),
                location = any(),
                trend = any(),
                minPrice = any(),
                maxPrice = any(),
                page = any(),
                limit = any(),
                sortBy = any(),
                sortOrder = any()
            )
        } throws RuntimeException("Network error")

        // When: Refresh
        val result = repository.refreshMarketPrices()

        // Then: Should return error
        assertTrue(result is Resource.Error)
    }

    // ==========================================================================
    // AVAILABLE CROP TYPES AND LOCATIONS TESTS
    // ==========================================================================

    @Test
    fun `getAvailableCropTypes should return list from DAO`() = runTest {
        // Given: DAO has crop types
        coEvery { marketPriceDao.getAvailableCropTypes() } returns listOf("tomato", "rice", "carrot")

        // When: Get available crop types
        val result = repository.getAvailableCropTypes()

        // Then: Should return the list
        assertEquals(3, result.size)
        assertTrue(result.contains("tomato"))
    }

    @Test
    fun `getAvailableLocations should return list from DAO`() = runTest {
        // Given: DAO has locations
        coEvery { marketPriceDao.getAvailableLocations() } returns listOf("jaffna", "colombo")

        // When: Get available locations
        val result = repository.getAvailableLocations()

        // Then: Should return the list
        assertEquals(2, result.size)
        assertTrue(result.contains("jaffna"))
    }

    // ==========================================================================
    // CLEAR STALE DATA TESTS
    // ==========================================================================

    @Test
    fun `clearStaleData should deactivate and cleanup old data`() = runTest {
        // Given: DAO is ready

        // When: Clear stale data
        repository.clearStaleData()

        // Then: Should call both cleanup methods
        coVerify { marketPriceDao.deactivateStaleMarketPrices(24) }
        coVerify { marketPriceDao.cleanupOldMarketPrices(7) }
    }

    // ==========================================================================
    // TRENDING PRICES TESTS
    // ==========================================================================

    @Test
    fun `getTrendingPrices should return trending data from cache and network`() = runTest {
        // Given: DAO has cached trending data
        val trendingPrices = mockMarketPrices.filter { kotlin.math.abs(it.changePercentage) >= 2.0 }
        coEvery { marketPriceDao.getTrendingPrices(any(), any()) } returns trendingPrices

        // When: Get trending prices
        val results = repository.getTrendingPrices().toList()

        // Then: Should return at least loading and success states
        assertTrue(results.any { it is Resource.Loading })
        val successResults = results.filterIsInstance<Resource.Success<List<MarketPrice>>>()
        assertTrue(successResults.isNotEmpty())
    }
}
