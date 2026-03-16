package com.senthapps.slagrimarket.data.repository

import com.senthapps.slagrimarket.data.api.CropDto
import com.senthapps.slagrimarket.data.api.CropixApiService
import com.senthapps.slagrimarket.data.api.CropsResponse
import com.senthapps.slagrimarket.data.dao.DoaCropDao
import com.senthapps.slagrimarket.data.model.DoaCropEntity
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CropixRepositoryTest {

    private lateinit var cropixApiService: CropixApiService
    private lateinit var doaCropDao: DoaCropDao
    private lateinit var repository: CropixRepository

    private val CACHE_TTL_MS = 7L * 24 * 3600 * 1000

    private val mockCropDtos = listOf(
        CropDto(
            id = 1,
            cropId = "CROP/001",
            description = "Tomato",
            cropType = "Vegetable",
            scientificName = "Solanum lycopersicum",
            presignedUrl = null
        ),
        CropDto(
            id = 2,
            cropId = "CROP/002",
            description = "Red Onion",
            cropType = "Vegetable",
            scientificName = "Allium cepa",
            presignedUrl = null
        )
    )

    private val mockEntities = listOf(
        DoaCropEntity(id = 1, cropId = "CROP/001", description = "Tomato", cropType = "Vegetable", scientificName = "Solanum lycopersicum", categoryId = "CROP"),
        DoaCropEntity(id = 2, cropId = "CROP/002", description = "Red Onion", cropType = "Vegetable", scientificName = "Allium cepa", categoryId = "CROP")
    )

    @Before
    fun setup() {
        cropixApiService = mockk(relaxed = true)
        doaCropDao = mockk(relaxed = true)
        repository = CropixRepository(cropixApiService, doaCropDao)
    }

    @Test
    fun `getCrops should delegate to doaCropDao getAllCrops`() = runTest {
        coEvery { doaCropDao.getAllCrops() } returns flowOf(mockEntities)

        val result = repository.getCrops().first()

        assertEquals(2, result.size)
        assertEquals("Tomato", result[0].description)
    }

    @Test
    fun `syncCrops should skip API when cache is fresh`() = runTest {
        // Cache is non-empty and not stale
        coEvery { doaCropDao.getCropCount() } returns 10
        coEvery { doaCropDao.getOldestCacheTime() } returns System.currentTimeMillis() - 1000L // 1 second ago (not stale)

        repository.syncCrops()

        coVerify(exactly = 0) { cropixApiService.getCrops() }
        coVerify(exactly = 0) { doaCropDao.insertCrops(any()) }
    }

    @Test
    fun `syncCrops should call API when cache is empty`() = runTest {
        coEvery { doaCropDao.getCropCount() } returns 0
        coEvery { cropixApiService.getCrops() } returns CropsResponse(payload = mockCropDtos)
        coEvery { doaCropDao.insertCrops(any()) } just Runs

        repository.syncCrops()

        coVerify(exactly = 1) { cropixApiService.getCrops() }
        coVerify(exactly = 1) { doaCropDao.insertCrops(any()) }
    }

    @Test
    fun `syncCrops should call API when cache is stale`() = runTest {
        val staleTime = System.currentTimeMillis() - CACHE_TTL_MS - 1000L // stale by 1 second
        coEvery { doaCropDao.getCropCount() } returns 5
        coEvery { doaCropDao.getOldestCacheTime() } returns staleTime
        coEvery { cropixApiService.getCrops() } returns CropsResponse(payload = mockCropDtos)
        coEvery { doaCropDao.insertCrops(any()) } just Runs

        repository.syncCrops()

        coVerify(exactly = 1) { cropixApiService.getCrops() }
        coVerify(exactly = 1) { doaCropDao.insertCrops(any()) }
    }

    @Test
    fun `syncCrops should insert correct number of entities`() = runTest {
        val capturedEntities = slot<List<DoaCropEntity>>()
        coEvery { doaCropDao.getCropCount() } returns 0
        coEvery { cropixApiService.getCrops() } returns CropsResponse(payload = mockCropDtos)
        coEvery { doaCropDao.insertCrops(capture(capturedEntities)) } just Runs

        repository.syncCrops()

        assertEquals(2, capturedEntities.captured.size)
        assertEquals("Tomato", capturedEntities.captured[0].description)
        assertEquals("CROP", capturedEntities.captured[0].categoryId) // extracted from "CROP/001"
    }

    @Test
    fun `syncCrops should silently handle API exception`() = runTest {
        coEvery { doaCropDao.getCropCount() } returns 0
        coEvery { cropixApiService.getCrops() } throws Exception("Network error")

        // Should not throw
        repository.syncCrops()

        coVerify(exactly = 0) { doaCropDao.insertCrops(any()) }
    }

    @Test
    fun `syncCrops should not insert when API returns null payload`() = runTest {
        coEvery { doaCropDao.getCropCount() } returns 0
        coEvery { cropixApiService.getCrops() } returns CropsResponse(payload = null)

        repository.syncCrops()

        coVerify(exactly = 0) { doaCropDao.insertCrops(any()) }
    }

    @Test
    fun `syncCrops should handle empty payload list`() = runTest {
        coEvery { doaCropDao.getCropCount() } returns 0
        coEvery { cropixApiService.getCrops() } returns CropsResponse(payload = emptyList())
        coEvery { doaCropDao.insertCrops(any()) } just Runs

        repository.syncCrops()

        // Empty list is a valid payload, insertCrops should be called with empty list
        coVerify(exactly = 1) { doaCropDao.insertCrops(match { it.isEmpty() }) }
    }
}
