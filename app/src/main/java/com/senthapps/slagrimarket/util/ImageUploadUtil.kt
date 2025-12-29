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
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val originalBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            if (originalBitmap == null) return@withContext null

            // Calculate scaling
            val ratio = Math.min(
                maxWidth.toFloat() / originalBitmap.width,
                maxHeight.toFloat() / originalBitmap.height
            )
            
            val finalBitmap = if (ratio < 1.0) {
                val width = (originalBitmap.width * ratio).toInt()
                val height = (originalBitmap.height * ratio).toInt()
                android.graphics.Bitmap.createScaledBitmap(originalBitmap, width, height, true)
            } else {
                originalBitmap
            }

            // Create temp file for compressed image
            val fileName = "comp_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)
            
            finalBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, quality, outputStream)
            outputStream.flush()
            outputStream.close()

            // Cleanup bitmaps
            if (finalBitmap != originalBitmap) {
                finalBitmap.recycle()
            }
            originalBitmap.recycle()

            file
        } catch (e: Exception) {
            Timber.e(e, "Error preparing image for upload")
            null
        }
    }
}
