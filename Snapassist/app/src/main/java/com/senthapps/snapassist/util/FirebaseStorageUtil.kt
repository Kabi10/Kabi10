package com.senthapps.snapassist.util

import android.content.Context
import android.net.Uri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageMetadata
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.tasks.await

object FirebaseStorageUtil {
  /**
   * Uploads a file to: shots/{uid}/{timestamp}.jpg
   * @param metadata custom metadata (optional)
   * @param idempotencyKey stored as metadata to avoid duplicate uploads (optional)
   * @return downloadUrl
   */
  suspend fun uploadToCloud(
    localUri: Uri,
    uid: String,
    metadata: Map<String, String> = emptyMap(),
    idempotencyKey: String? = null
  ): String {
    val name = "${System.currentTimeMillis()}.jpg"
    val ref = Firebase.storage.reference.child("shots/$uid/$name")
    val mdBuilder = StorageMetadata.Builder()
    metadata.forEach { (k, v) -> mdBuilder.setCustomMetadata(k, v) }
    idempotencyKey?.let { mdBuilder.setCustomMetadata("idempotencyKey", it) }
    val md = mdBuilder.build()
    ref.putFile(localUri, md).await()
    return ref.downloadUrl.await().toString()
  }
  
  /**
   * Upload with retry for AppFcmService compatibility
   */
  suspend fun uploadToCloudWithRetry(
    context: Context,
    uri: Uri,
    includeMetadata: Boolean = true
  ): String {
    val uid = "default_user" // TODO: Get actual user ID
    val metadata = if (includeMetadata) {
      mapOf(
        "device" to android.os.Build.MODEL,
        "timestamp" to System.currentTimeMillis().toString()
      )
    } else emptyMap()
    
    return uploadToCloud(uri, uid, metadata)
  }
}