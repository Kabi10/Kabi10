package com.senthapps.slagrimarket.data.repository

import android.content.Context
import com.senthapps.slagrimarket.data.api.StorageApiService
import com.senthapps.slagrimarket.data.api.StorageDeleteRequest
import com.senthapps.slagrimarket.data.api.StorageDeleteResponse
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Response

@OptIn(ExperimentalCoroutinesApi::class)
class StorageRepositoryTest {

    private lateinit var context: Context
    private lateinit var storageApiService: StorageApiService
    private lateinit var repository: StorageRepository

    private val validImageUrl = "https://xyz.supabase.co/storage/v1/object/public/listing-images/test-image.jpg"
    private val expectedPath = "test-image.jpg"

    @Before
    fun setup() {
        context = mockk(relaxed = true)
        storageApiService = mockk(relaxed = true)
        repository = StorageRepository(context, storageApiService)
    }

    @Test
    fun `deleteImage should extract path and call API for valid URL`() = runTest {
        coEvery {
            storageApiService.deleteImage(StorageDeleteRequest(expectedPath))
        } returns Response.success(StorageDeleteResponse(success = true, message = null))

        val result = repository.deleteImage(validImageUrl)

        assertTrue(result.isSuccess)
        coVerify { storageApiService.deleteImage(StorageDeleteRequest(expectedPath)) }
    }

    @Test
    fun `deleteImage should fail immediately for URL without listing-images marker`() = runTest {
        val invalidUrl = "https://xyz.supabase.co/storage/v1/object/public/other-bucket/test.jpg"

        val result = repository.deleteImage(invalidUrl)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Invalid image URL") == true)
        coVerify(exactly = 0) { storageApiService.deleteImage(any()) }
    }

    @Test
    fun `deleteImage should fail when API returns success=false`() = runTest {
        coEvery {
            storageApiService.deleteImage(any())
        } returns Response.success(StorageDeleteResponse(success = false, message = "File not found"))

        val result = repository.deleteImage(validImageUrl)

        assertTrue(result.isFailure)
        assertEquals("File not found", result.exceptionOrNull()?.message)
    }

    @Test
    fun `deleteImage should fail when API throws exception`() = runTest {
        coEvery { storageApiService.deleteImage(any()) } throws Exception("Network error")

        val result = repository.deleteImage(validImageUrl)

        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteListingImages should return success immediately for empty list`() = runTest {
        val result = repository.deleteListingImages("listing1", emptyList())

        assertTrue(result.isSuccess)
        coVerify(exactly = 0) { storageApiService.deleteImage(any()) }
    }

    @Test
    fun `deleteListingImages should succeed when all images delete successfully`() = runTest {
        val urls = listOf(
            "https://xyz.supabase.co/listing-images/img1.jpg",
            "https://xyz.supabase.co/listing-images/img2.jpg"
        )
        coEvery {
            storageApiService.deleteImage(any())
        } returns Response.success(StorageDeleteResponse(success = true, message = null))

        val result = repository.deleteListingImages("listing1", urls)

        assertTrue(result.isSuccess)
        coVerify(exactly = 2) { storageApiService.deleteImage(any()) }
    }

    @Test
    fun `deleteListingImages should fail when single image deletion fails`() = runTest {
        val urls = listOf("https://xyz.supabase.co/listing-images/img1.jpg")
        coEvery {
            storageApiService.deleteImage(any())
        } returns Response.success(StorageDeleteResponse(success = false, message = "Error"))

        val result = repository.deleteListingImages("listing1", urls)

        assertTrue(result.isFailure)
    }

    @Test
    fun `deleteListingImages should succeed when multiple images have one failure`() = runTest {
        // When there are multiple images and one fails, result is still success (partial ok)
        val urls = listOf(
            "https://xyz.supabase.co/listing-images/img1.jpg",
            "https://xyz.supabase.co/listing-images/img2.jpg"
        )
        coEvery {
            storageApiService.deleteImage(StorageDeleteRequest("img1.jpg"))
        } returns Response.success(StorageDeleteResponse(success = true, message = null))
        coEvery {
            storageApiService.deleteImage(StorageDeleteRequest("img2.jpg"))
        } returns Response.success(StorageDeleteResponse(success = false, message = "Not found"))

        val result = repository.deleteListingImages("listing1", urls)

        // Multiple images: one failure does not fail the whole operation
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleteImage should correctly extract nested path from URL`() = runTest {
        val capturedRequest = slot<StorageDeleteRequest>()
        coEvery {
            storageApiService.deleteImage(capture(capturedRequest))
        } returns Response.success(StorageDeleteResponse(success = true))
        val url = "https://xyz.supabase.co/storage/listing-images/folder/subdir/image.png"

        repository.deleteImage(url)

        assertEquals("folder/subdir/image.png", capturedRequest.captured.path)
    }
}
