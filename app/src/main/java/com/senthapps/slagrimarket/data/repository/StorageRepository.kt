package com.senthapps.slagrimarket.data.repository

import android.content.Context
import android.net.Uri
import com.senthapps.slagrimarket.data.api.StorageApiService
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
     * Delete an image from storage
     * TODO: Implement backend delete endpoint
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        Timber.d("Image deletion not yet implemented for: $imageUrl")
        return Result.success(Unit)
    }

    /**
     * Delete all images for a listing
     * TODO: Implement backend bulk delete endpoint
     */
    suspend fun deleteListingImages(listingId: String): Result<Unit> {
        Timber.d("Listing image deletion not yet implemented for: $listingId")
        return Result.success(Unit)
    }
}
