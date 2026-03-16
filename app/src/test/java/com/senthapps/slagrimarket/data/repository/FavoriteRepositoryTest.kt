package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.AddFavoriteRequest
import com.senthapps.slagrimarket.data.api.AddFavoriteResponse
import com.senthapps.slagrimarket.data.api.CheckFavoriteResponse
import com.senthapps.slagrimarket.data.api.FavoriteApiService
import com.senthapps.slagrimarket.data.api.FavoriteListingData
import com.senthapps.slagrimarket.data.api.FavoriteListingDto
import com.senthapps.slagrimarket.data.api.FavoritesResponse
import com.senthapps.slagrimarket.data.dao.FavoriteDao
import com.senthapps.slagrimarket.data.model.Favorite
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class FavoriteRepositoryTest {

    private lateinit var favoriteDao: FavoriteDao
    private lateinit var favoriteApiService: FavoriteApiService
    private lateinit var repository: FavoriteRepository

    private val mockListings = listOf(
        Listing(
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
    )

    @Before
    fun setup() {
        favoriteDao = mockk(relaxed = true)
        favoriteApiService = mockk(relaxed = true)
        repository = FavoriteRepository(favoriteDao, favoriteApiService)
    }

    @Test
    fun `toggleFavorite should add to DAO when not already a favorite`() = runTest {
        coEvery { favoriteDao.isFavorite("user1", "listing1") } returns false
        coEvery { favoriteDao.insertFavorite(any()) } just Runs
        coEvery { favoriteApiService.addFavorite(any()) } returns Response.success(
            AddFavoriteResponse(success = true, data = null)
        )

        val result = repository.toggleFavorite("user1", "listing1")

        assertTrue(result.isSuccess)
        assertEquals(true, result.getOrNull()) // returns true when added
        coVerify { favoriteDao.insertFavorite(any()) }
        coVerify(exactly = 0) { favoriteDao.deleteFavorite(any(), any()) }
    }

    @Test
    fun `toggleFavorite should remove from DAO when already a favorite`() = runTest {
        coEvery { favoriteDao.isFavorite("user1", "listing1") } returns true
        coEvery { favoriteDao.deleteFavorite("user1", "listing1") } just Runs

        val result = repository.toggleFavorite("user1", "listing1")

        assertTrue(result.isSuccess)
        assertEquals(false, result.getOrNull()) // returns false when removed
        coVerify { favoriteDao.deleteFavorite("user1", "listing1") }
        coVerify(exactly = 0) { favoriteDao.insertFavorite(any()) }
    }

    @Test
    fun `toggleFavorite should succeed locally even when API throws`() = runTest {
        coEvery { favoriteDao.isFavorite("user1", "listing1") } returns false
        coEvery { favoriteApiService.addFavorite(any()) } throws Exception("Network error")
        coEvery { favoriteDao.insertFavorite(any()) } just Runs

        val result = repository.toggleFavorite("user1", "listing1")

        assertTrue(result.isSuccess)
        coVerify { favoriteDao.insertFavorite(any()) }
    }

    @Test
    fun `toggleFavorite should fail when DAO throws`() = runTest {
        coEvery { favoriteDao.isFavorite("user1", "listing1") } throws RuntimeException("DB error")

        val result = repository.toggleFavorite("user1", "listing1")

        assertTrue(result.isFailure)
    }

    @Test
    fun `isFavorite should return API result when API succeeds`() = runTest {
        coEvery { favoriteApiService.checkFavorite("listing1") } returns Response.success(
            CheckFavoriteResponse(success = true, isFavorited = true)
        )

        val result = repository.isFavorite("user1", "listing1")

        assertTrue(result)
        coVerify(exactly = 0) { favoriteDao.isFavorite(any(), any()) }
    }

    @Test
    fun `isFavorite should fall back to DAO when API throws`() = runTest {
        coEvery { favoriteApiService.checkFavorite("listing1") } throws Exception("Network error")
        coEvery { favoriteDao.isFavorite("user1", "listing1") } returns true

        val result = repository.isFavorite("user1", "listing1")

        assertTrue(result)
        coVerify { favoriteDao.isFavorite("user1", "listing1") }
    }

    @Test
    fun `isFavorite should fall back to DAO when API response is not successful`() = runTest {
        coEvery { favoriteApiService.checkFavorite("listing1") } returns Response.error(404, okhttp3.ResponseBody.create(null, ""))
        coEvery { favoriteDao.isFavorite("user1", "listing1") } returns false

        val result = repository.isFavorite("user1", "listing1")

        assertFalse(result)
        coVerify { favoriteDao.isFavorite("user1", "listing1") }
    }

    @Test
    fun `getFavoriteListingsForUser should delegate to DAO`() = runTest {
        coEvery { favoriteDao.getFavoriteListingsForUser("user1") } returns flowOf(mockListings)

        val result = repository.getFavoriteListingsForUser("user1").first()

        assertEquals(1, result.size)
        assertEquals("Tomatoes", result[0].cropType)
    }

    @Test
    fun `refreshFavorites should insert favorites from API into DAO`() = runTest {
        val dto = FavoriteListingDto(
            favoriteId = "fav1",
            favoritedAt = "2025-11-20T10:00:00Z",
            listing = FavoriteListingData(
                id = "listing1",
                farmerId = "farmer1",
                cropType = "Tomatoes",
                quantity = 100.0,
                unit = "kg",
                pricePerUnit = 50.0,
                quality = "A",
                location = "Jaffna",
                description = null,
                images = emptyList(),
                isActive = true,
                createdAt = "2025-11-20T10:00:00Z",
                farmerName = "Test Farmer",
                farmerPhone = null
            )
        )
        val response = FavoritesResponse(
            success = true,
            favorites = listOf(dto),
            totalCount = 1,
            page = 1,
            totalPages = 1,
            hasNext = false,
            hasPrevious = false
        )
        coEvery { favoriteApiService.getFavorites() } returns Response.success(response)
        coEvery { favoriteDao.insertFavorite(any()) } just Runs

        repository.refreshFavorites("user1")

        coVerify(exactly = 1) { favoriteDao.insertFavorite(any()) }
    }

    @Test
    fun `refreshFavorites should silently handle API exception`() = runTest {
        coEvery { favoriteApiService.getFavorites() } throws Exception("Network error")

        // Should not throw
        repository.refreshFavorites("user1")

        coVerify(exactly = 0) { favoriteDao.insertFavorite(any()) }
    }

    @Test
    fun `getFavoriteCount should return count from DAO`() = runTest {
        coEvery { favoriteDao.getFavoriteCount("user1") } returns 5

        val count = repository.getFavoriteCount("user1")

        assertEquals(5, count)
    }
}
