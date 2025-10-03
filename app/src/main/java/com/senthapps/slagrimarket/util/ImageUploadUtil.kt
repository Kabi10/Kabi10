package com.senthapps.slagrimarket.util

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

/**
 * Utility for handling image uploads
 */
object ImageUploadUtil {
    
    /**
     * Convert URI to MultipartBody.Part for upload
     */
    suspend fun uriToMultipartPart(
        context: Context,
        uri: Uri,
        partName: String = "image"
    ): MultipartBody.Part? = withContext(Dispatchers.IO) {
        try {
            val file = uriToFile(context, uri) ?: return@withContext null
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(partName, file.name, requestBody)
        } catch (e: Exception) {
            Timber.e(e, "Error converting URI to multipart")
            null
        }
    }
    
    /**
     * Convert URI to File
     */
    private fun uriToFile(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val fileName = getFileName(context, uri) ?: "image_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)
            
            FileOutputStream(file).use { output ->
                inputStream.copyTo(output)
            }
            
            file
        } catch (e: Exception) {
            Timber.e(e, "Error converting URI to file")
            null
        }
    }
    
    /**
     * Get file name from URI
     */
    private fun getFileName(context: Context, uri: Uri): String? {
        return try {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        return it.getString(nameIndex)
                    }
                }
            }
            null
        } catch (e: Exception) {
            Timber.e(e, "Error getting file name")
            null
        }
    }
    
    /**
     * Compress and prepare image for upload
     */
    suspend fun prepareImageForUpload(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1920,
        maxHeight: Int = 1080,
        quality: Int = 85
    ): File? = withContext(Dispatchers.IO) {
        try {
            // For now, just convert to file
            // TODO: Add compression logic
            uriToFile(context, uri)
        } catch (e: Exception) {
            Timber.e(e, "Error preparing image for upload")
            null
        }
    }
}
