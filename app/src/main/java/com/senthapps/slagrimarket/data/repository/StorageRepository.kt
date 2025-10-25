package com.senthapps.slagrimarket.data.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling image uploads to Firebase Storage
 */
@Singleton
class StorageRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) {
    
    companion object {
        private const val LISTING_IMAGES_PATH = "listing-images"
        private const val MAX_IMAGE_SIZE_MB = 5L
        private const val MAX_IMAGE_SIZE_BYTES = MAX_IMAGE_SIZE_MB * 1024 * 1024
    }
    
    /**
     * Upload a single image to Firebase Storage
     * 
     * @param uri The URI of the image to upload
     * @param listingId The ID of the listing this image belongs to
     * @return The download URL of the uploaded image, or null if upload failed
     */
    suspend fun uploadListingImage(uri: Uri, listingId: String): Result<String> {
        return try {
            // Validate file size
            val fileSize = getFileSize(uri)
            if (fileSize > MAX_IMAGE_SIZE_BYTES) {
                return Result.failure(Exception("Image size exceeds ${MAX_IMAGE_SIZE_MB}MB limit"))
            }
            
            // Generate unique filename
            val filename = "${listingId}/${UUID.randomUUID()}.jpg"
            val storageRef = storage.reference.child(LISTING_IMAGES_PATH).child(filename)
            
            // Upload file
            val uploadTask = storageRef.putFile(uri).await()
            
            // Get download URL
            val downloadUrl = storageRef.downloadUrl.await()
            
            Timber.d("Image uploaded successfully: $downloadUrl")
            Result.success(downloadUrl.toString())
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to upload image")
            Result.failure(e)
        }
    }
    
    /**
     * Upload multiple images to Firebase Storage
     * 
     * @param uris List of image URIs to upload
     * @param listingId The ID of the listing these images belong to
     * @return List of download URLs for successfully uploaded images
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
     * Delete an image from Firebase Storage
     * 
     * @param imageUrl The download URL of the image to delete
     */
    suspend fun deleteImage(imageUrl: String): Result<Unit> {
        return try {
            val storageRef = storage.getReferenceFromUrl(imageUrl)
            storageRef.delete().await()
            
            Timber.d("Image deleted successfully: $imageUrl")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete image")
            Result.failure(e)
        }
    }
    
    /**
     * Delete all images for a listing
     * 
     * @param listingId The ID of the listing
     */
    suspend fun deleteListingImages(listingId: String): Result<Unit> {
        return try {
            val listingFolder = storage.reference
                .child(LISTING_IMAGES_PATH)
                .child(listingId)
            
            // List all files in the folder
            val listResult = listingFolder.listAll().await()
            
            // Delete each file
            for (item in listResult.items) {
                item.delete().await()
            }
            
            Timber.d("Deleted ${listResult.items.size} images for listing $listingId")
            Result.success(Unit)
            
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete listing images")
            Result.failure(e)
        }
    }
    
    /**
     * Get the file size of a URI
     */
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.available().toLong()
            } ?: 0L
        } catch (e: Exception) {
            Timber.e(e, "Failed to get file size")
            0L
        }
    }
}

