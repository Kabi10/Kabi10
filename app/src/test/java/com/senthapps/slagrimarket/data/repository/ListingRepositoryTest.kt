package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.ListingApiService
import com.senthapps.slagrimarket.data.api.ListingsResponse
import com.senthapps.slagrimarket.data.dao.ListingDao
import com.senthapps.slagrimarket.data.dao.LocalOpDao
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.squareup.moshi.Moshi
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
}

