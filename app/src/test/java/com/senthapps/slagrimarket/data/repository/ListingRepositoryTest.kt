package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.ListingApiService
import com.senthapps.slagrimarket.data.api.ListingsResponse
import com.senthapps.slagrimarket.data.dao.ListingDao
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.squareup.moshi.Moshi
import app.cash.turbine.test
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
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class ListingRepositoryTest {

    private lateinit var listingApiService: ListingApiService
    private lateinit var listingDao: ListingDao
    private lateinit var localOpDao: LocalOpDao
    private lateinit var moshi: Moshi
    private lateinit var storageRepository: StorageRepository
    private lateinit var repository: ListingRepository

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
        listingApiService = mockk(relaxed = true)
        listingDao = mockk(relaxed = true)
        localOpDao = mockk(relaxed = true)
        moshi = Moshi.Builder().build()
        storageRepository = mockk(relaxed = true)

        repository = ListingRepository(
            listingApiService,
            listingDao,
            localOpDao,
            moshi,
            storageRepository
        )
    }

    @Test
    fun `getAllActiveListingsFlow should return listings from DAO`() = runTest {
        // Given: DAO returns listings
        coEvery { listingDao.getAllActiveListingsFlow() } returns flowOf(mockListings)

        // When: getAllActiveListingsFlow is called
        val result = repository.getAllActiveListingsFlow().first()

        // Then: Should return listings
        assertEquals(2, result.size)
        assertEquals("Tomatoes", result[0].cropType)
    }

    @Test
    fun `getAllActiveListings should emit cached data first`() = runTest {
        // Given: DAO has cached data
        val listingsResponse = ListingsResponse(
            listings = mockListings,
            totalCount = mockListings.size,
            page = 1,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false,
            lastUpdated = "2025-11-20T10:00:00Z"
        )
        coEvery { listingDao.getAllActiveListings() } returns mockListings
        coEvery { listingApiService.getListings(any(), any()) } returns Response.success(listingsResponse)

        // When: getAllActiveListings is called
        val results = repository.getAllActiveListings().toList()

        // Then: Should emit loading, then cached data
        assertTrue(results.any { it is Resource.Loading })
        assertTrue(results.any { it is Resource.Success && it.data?.size == 2 })
    }

    @Test
    fun `createListing should save to DAO and create local op`() = runTest {
        // Given: DAO operations succeed
        coEvery { listingDao.insertListing(any()) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        // When: createListing is called
        val result = repository.createListing(
            cropType = "Tomatoes",
            quantity = 100.0,
            unit = "kg",
            pricePerUnit = 50.0,
            quality = "A",
            harvestDate = "2025-11-20",
            location = "Jaffna",
            farmerId = "farmer1"
        )

        // Then: Should succeed and save to DAO
        assertTrue(result.isSuccess)
        coVerify { listingDao.insertListing(any()) }
        coVerify { localOpDao.insertOp(any()) }
    }

    @Test
    fun `updateListing should update DAO and create local op`() = runTest {
        // Given: Existing listing in DAO
        coEvery { listingDao.getListingById("1") } returns mockListings[0]
        coEvery { listingDao.updateListing(any()) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        // When: updateListing is called
        val result = repository.updateListing(
            listingId = "1",
            quantity = 150.0,
            pricePerUnit = 55.0
        )

        // Then: Should succeed and update DAO
        assertTrue(result.isSuccess)
        assertEquals(150.0, result.getOrNull()?.quantity)
        coVerify { listingDao.updateListing(any()) }
        coVerify { localOpDao.insertOp(any()) }
    }

    @Test
    fun `updateListing should fail if listing not found`() = runTest {
        // Given: Listing not in DAO
        coEvery { listingDao.getListingById("nonexistent") } returns null

        // When: updateListing is called
        val result = repository.updateListing(
            listingId = "nonexistent",
            quantity = 150.0
        )

        // Then: Should fail
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("not found") == true)
    }

    @Test
    fun `deleteListing should deactivate in DAO and create local op`() = runTest {
        // Given: DAO operations succeed
        coEvery { listingDao.deactivateListing("1") } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        // When: deleteListing is called
        val result = repository.deleteListing("1")

        // Then: Should succeed
        assertTrue(result.isSuccess)
        coVerify { listingDao.deactivateListing("1") }
        coVerify { localOpDao.insertOp(any()) }
    }

    @Test
    fun `searchListingsFlow should return filtered listings from DAO`() = runTest {
        // Given: DAO returns filtered listings
        val tomatoListings = mockListings.filter { it.cropType == "Tomatoes" }
        coEvery { listingDao.searchListingsFlow("Tomatoes", null) } returns flowOf(tomatoListings)

        // When: searchListingsFlow is called
        val result = repository.searchListingsFlow("Tomatoes", null).first()

        // Then: Should return filtered listings
        assertEquals(1, result.size)
        assertEquals("Tomatoes", result[0].cropType)
    }

    @Test
    fun `getAvailableCropTypes should return crop types from DAO`() = runTest {
        // Given: DAO returns crop types
        coEvery { listingDao.getAvailableCropTypes() } returns listOf("Tomatoes", "Rice", "Onions")

        // When: getAvailableCropTypes is called
        val result = repository.getAvailableCropTypes()

        // Then: Should return crop types
        assertEquals(3, result.size)
        assertTrue(result.contains("Tomatoes"))
    }

    @Test
    fun `getAvailableLocations should return locations from DAO`() = runTest {
        // Given: DAO returns locations
        coEvery { listingDao.getAvailableLocations() } returns listOf("Jaffna", "Colombo", "Kandy")

        // When: getAvailableLocations is called
        val result = repository.getAvailableLocations()

        // Then: Should return locations
        assertEquals(3, result.size)
        assertTrue(result.contains("Jaffna"))
    }

    @Test
    fun `getListings emitsCachedDataFirst thenNetworkData`() = runTest {
        // Given: Cache has one listing, network returns a larger set (both 2 items)
        val cachedListings = listOf(mockListings[0])
        val networkListings = mockListings // both listings from network
        val networkResponse = ListingsResponse(
            listings = networkListings,
            totalCount = networkListings.size,
            page = 1,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false,
            lastUpdated = "2025-11-20T10:00:00Z"
        )
        coEvery { listingDao.getAllActiveListings() } returns cachedListings
        // Match the exact params used in getAllActiveListings(): limit=100, isActive=true
        coEvery { listingApiService.getListings(limit = 100, isActive = true) } returns Response.success(networkResponse)

        // When/Then: Turbine verifies Loading → Success(cached 1) → Success(network 2)
        repository.getAllActiveListings(forceRefresh = true).test {
            val loading = awaitItem()
            assertTrue(loading is Resource.Loading)

            val cachedSuccess = awaitItem()
            assertTrue(cachedSuccess is Resource.Success)
            assertEquals(1, (cachedSuccess as Resource.Success).data?.size)

            val networkSuccess = awaitItem()
            assertTrue(networkSuccess is Resource.Success)
            assertEquals(2, (networkSuccess as Resource.Success).data?.size)

            awaitComplete()
        }
    }

    @Test
    fun `createListing writesToDaoAndCreatesLocalOp`() = runTest {
        // Given: DAO methods ready to accept calls
        coEvery { listingDao.insertListing(any()) } just Runs
        coEvery { localOpDao.insertOp(any()) } just Runs

        // When: createListing is called with specific test data
        val result = repository.createListing(
            cropType = "Onions",
            quantity = 50.0,
            unit = "kg",
            pricePerUnit = 30.0,
            quality = "A",
            harvestDate = "2025-11-22",
            location = "Kandy",
            farmerId = "farmer3"
        )

        // Then: Both DAOs called exactly once and returned listing has correct data
        assertTrue(result.isSuccess)
        val createdListing = result.getOrNull()
        assertNotNull(createdListing)
        assertEquals("Onions", createdListing?.cropType)
        assertEquals(50.0, createdListing?.quantity)
        assertEquals("farmer3", createdListing?.farmerId)
        coVerify(exactly = 1) { listingDao.insertListing(any()) }
        coVerify(exactly = 1) { localOpDao.insertOp(any()) }
    }

    @Test
    fun `getListings emitsError whenNetworkFails`() = runTest {
        // Given: Empty cache and network throws IOException (offline scenario)
        coEvery { listingDao.getAllActiveListings() } returns emptyList()
        // Match exact params used in getAllActiveListings(): limit=100, isActive=true
        coEvery { listingApiService.getListings(limit = 100, isActive = true) } throws IOException("No internet connection")

        // When/Then: Turbine verifies Loading → Error (cached data was empty)
        repository.getAllActiveListings(forceRefresh = true).test {
            val loading = awaitItem()
            assertTrue(loading is Resource.Loading)

            val error = awaitItem()
            assertTrue(error is Resource.Error)
            assertTrue((error as Resource.Error).message?.contains("internet") == true ||
                    error.message?.contains("connection") == true ||
                    error.message?.isNotEmpty() == true)

            awaitComplete()
        }
    }

    // ============================================================================
    // KAB-7: Additional coverage — error paths and offline edge cases
    // ============================================================================

    @Test
    fun `createListing daoInsertThrows returnsFailure`() = runTest {
        // Given: DAO throws on insert (e.g. DB constraint or disk error)
        coEvery { listingDao.insertListing(any()) } throws RuntimeException("DB write failed")

        // When
        val result = repository.createListing(
            cropType = "Tomatoes",
            quantity = 100.0,
            unit = "kg",
            pricePerUnit = 50.0,
            quality = "A",
            harvestDate = "2025-11-20",
            location = "Jaffna",
            farmerId = "farmer1"
        )

        // Then: propagated as failure, localOpDao never called
        assertTrue(result.isFailure)
        coVerify(exactly = 0) { localOpDao.insertOp(any()) }
    }

    @Test
    fun `getAllActiveListings cachedData thenNetworkError completesSilently`() = runTest {
        // Given: cache has one listing, network fails
        // Offline-first behaviour: network error is SUPPRESSED when cache is available —
        // the user sees cached data and no error toast (line 76 in ListingRepository).
        val cachedListings = listOf(mockListings[0])
        coEvery { listingDao.getAllActiveListings() } returns cachedListings
        coEvery { listingApiService.getListings(limit = 100, isActive = true) } throws IOException("Offline")

        // When/Then: Loading → Success(cached) → complete (no Error emitted)
        repository.getAllActiveListings(forceRefresh = true).test {
            assertTrue(awaitItem() is Resource.Loading)

            val cached = awaitItem()
            assertTrue(cached is Resource.Success)
            assertEquals(1, (cached as Resource.Success).data?.size)

            awaitComplete() // flow ends cleanly — no error when cache is present
        }
    }

    @Test
    fun `refreshListings networkSuccess updatesDao`() = runTest {
        // Given: API returns listings successfully
        val networkResponse = ListingsResponse(
            listings = mockListings,
            totalCount = mockListings.size,
            page = 1,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false,
            lastUpdated = "2025-11-20T10:00:00Z"
        )
        coEvery { listingApiService.getListings(limit = 100, isActive = true) } returns Response.success(networkResponse)
        coEvery { listingDao.insertListings(any()) } just Runs

        // When
        val result = repository.refreshListings()

        // Then: Success and DAO updated with network data
        assertTrue(result is Resource.Success)
        coVerify { listingDao.insertListings(mockListings) }
    }

    @Test
    fun `getListingById existsLocally emitsSuccess`() = runTest {
        // Given: listing in local cache and API mirrors it
        coEvery { listingDao.getListingById("1") } returns mockListings[0]
        coEvery { listingApiService.getListingById("1") } returns Response.success(mockListings[0])
        coEvery { listingDao.insertListing(any()) } just Runs

        // When/Then: Loading → Success(local) → (network refresh, ignored)
        repository.getListingById("1").test {
            assertTrue(awaitItem() is Resource.Loading)
            val result = awaitItem()
            assertTrue(result is Resource.Success)
            assertEquals("Tomatoes", (result as Resource.Success).data?.cropType)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getListingById notFoundAnywhere emitsError`() = runTest {
        // Given: not in cache, network throws
        coEvery { listingDao.getListingById("notfound") } returns null
        coEvery { listingApiService.getListingById("notfound") } throws IOException("Not found")

        // When/Then: Loading → Error
        repository.getListingById("notfound").test {
            assertTrue(awaitItem() is Resource.Loading)
            assertTrue(awaitItem() is Resource.Error)
            awaitComplete()
        }
    }
}

