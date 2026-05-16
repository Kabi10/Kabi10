package com.senthapps.slagrimarket.data.repository

import android.content.Context
import android.net.Uri
import com.senthapps.slagrimarket.data.api.StorageApiService
import com.senthapps.slagrimarket.data.api.StorageDeleteRequest
import com.senthapps.slagrimarket.util.ImageUploadUtil
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling image uploads via backend Storage API
 */
@Singleton
class StorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storageApiService: StorageApiService
) {

    /**
     * Upload a single image via backend Storage API
     *
     * @param uri The URI of the image to upload
     * @param listingId The ID of the listing this image belongs to (used for logging)
     * @return The public URL of the uploaded image
     */
    suspend fun uploadListingImage(uri: Uri, listingId: String): Result<String> {
        return try {
            val file = ImageUploadUtil.prepareImageForUpload(context, uri)
                ?: return Result.failure(Exception("Failed to prepare image for upload"))

            try {
                val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", file.name, requestBody)

                val response = storageApiService.uploadImage(part)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true && body.data != null) {
                        Timber.d("Image uploaded successfully for listing $listingId: ${body.data.url}")
                        Result.success(body.data.url)
                    } else {
                        Result.failure(Exception(body?.message ?: "Upload failed"))
                    }
                } else {
                    Result.failure(Exception("Upload failed with status ${response.code()}"))
                }
            } finally {
                file.delete()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload image for listing $listingId")
            Result.failure(e)
        }
    }

    /**
     * Upload multiple images via backend Storage API
     *
     * @param uris List of image URIs to upload
     * @param listingId The ID of the listing these images belong to
     * @return List of public URLs for successfully uploaded images
     */
    suspend fun uploadListingImages(uris: List<Uri>, listingId: String): Result<List<String>> {
        return try {
            val downloadUrls = mutableListOf<String>()

            for (uri in uris) {
                val result = uploadListingImage(uri, listingId)
                if (result.isSuccess) {
                    result.getOrNull()?.let { downloadUrls.add(it) }
                } else {
                    Timber.w("Failed to upload image: ${result.exceptionOrNull()?.message}")
                }
            }

            if (downloadUrls.isEmpty()) {
                Result.failure(Exception("No images were uploaded successfully"))
            } else {
                Timber.d("Uploaded ${downloadUrls.size} out of ${uris.size} images")
                Result.success(downloadUrls)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload images")
            Result.failure(e)
        }
    }

    /**
     * Delete an image from storage by its public URL.
     * Extracts the Supabase storage path from the URL and calls the backend delete endpoint.
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val marker = "listing-images/"
            val markerIndex = imageUrl.indexOf(marker)
            if (markerIndex < 0) {
                Timber.w("Cannot extract storage path from URL: $imageUrl")
                return Result.failure(Exception("Invalid image URL format"))
            }
            val path = imageUrl.substring(markerIndex + marker.length)

            val response = storageApiService.deleteImage(StorageDeleteRequest(path))
            if (response.isSuccessful && response.body()?.success == true) {
                Timber.d("Image deleted: $path")
                Result.success(Unit)
            } else {
                val msg = response.body()?.message ?: "Delete failed (${response.code()})"
                Timber.w("Delete failed for $path: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete image: $imageUrl")
            Result.failure(e)
        }
    }

    /**
     * Delete all images for a listing by their public URLs.
     * Logs warnings for individual failures but succeeds if at least one deletion succeeds.
     */
    suspend fun deleteListingImages(listingId: String, imageUrls: List<String>): Result<Unit> {
        if (imageUrls.isEmpty()) return Result.success(Unit)
        var anyFailure = false
        for (url in imageUrls) {
            deleteImage(url).onFailure {
                Timber.w("Failed to delete image for listing $listingId: ${it.message}")
                anyFailure = true
            }
        }
        return if (anyFailure && imageUrls.size == 1) {
            Result.failure(Exception("Failed to delete image for listing $listingId"))
        } else {
            Result.success(Unit)
        }
    }
}
