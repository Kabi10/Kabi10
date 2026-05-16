package com.senthapps.snapassist.service

import android.app.Notification
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.util.Log
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import com.senthapps.snapassist.util.Notifs
import java.io.File
import java.util.concurrent.Executor

class CameraService : LifecycleService() {

  private lateinit var mainExecutor: Executor
  private var imageCapture: ImageCapture? = null
  private lateinit var localSnapReceiver: BroadcastReceiver

  override fun onCreate() {
    super.onCreate()
    mainExecutor = ContextCompat.getMainExecutor(this)
    startForeground(NOTIF_ID, Notifs.foreground(this, "Processing"))
    bindCamera()
    setupLocalSnapReceiver()
    isServiceRunning = true
    serviceInstance = this
  }

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    // Keep running if process is killed
    return Service.START_STICKY
  }

  override fun onDestroy() {
    super.onDestroy()
    // Unbinds handled by lifecycle automatically when service stops
    try {
      unregisterReceiver(localSnapReceiver)
    } catch (e: Exception) {
      Log.w(TAG, "Error unregistering receiver", e)
    }
    isServiceRunning = false
    serviceInstance = null
  }

  override fun onBind(intent: Intent): IBinder? {
    return super.onBind(intent)
  }

  private fun bindCamera() {
    val future = ProcessCameraProvider.getInstance(this@CameraService)
    future.addListener({
      val provider = future.get()
      val capture = ImageCapture.Builder()
        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
        .build()
      provider.unbindAll()
      provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, capture)
      imageCapture = capture
    }, mainExecutor)
  }

  fun snapTo(file: File, onOk: (File) -> Unit, onErr: (Throwable) -> Unit) {
    val capture = imageCapture ?: return onErr(IllegalStateException("Camera not ready"))
    val opts = ImageCapture.OutputFileOptions.Builder(file).build()
    capture.takePicture(opts, mainExecutor, object : ImageCapture.OnImageSavedCallback {
      override fun onImageSaved(output: ImageCapture.OutputFileResults) { onOk(file) }
      override fun onError(exception: ImageCaptureException) { onErr(exception) }
    })
  }
  
  private fun setupLocalSnapReceiver() {
    localSnapReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == "com.senthapps.snapassist.SNAP_LOCAL") {
          val path = intent.getStringExtra("path") ?: return
          Log.d(TAG, "Local SNAP request received: $path")
          snapTo(File(path),
            onOk = { file -> Log.d(TAG, "Local SNAP captured: ${file.name}") },
            onErr = { error -> Log.e(TAG, "Local SNAP failed", error) }
          )
        }
      }
    }
    registerReceiver(localSnapReceiver, IntentFilter("com.senthapps.snapassist.SNAP_LOCAL"))
  }
  
  suspend fun snapToTempFile(): android.net.Uri {
    return kotlin.coroutines.suspendCoroutine { continuation ->
      val tempFile = File(cacheDir, "snap_${System.currentTimeMillis()}.jpg")
      snapTo(tempFile, 
        onOk = { file -> continuation.resumeWith(Result.success(android.net.Uri.fromFile(file))) },
        onErr = { error -> continuation.resumeWith(Result.failure(error)) }
      )
    }
  }

  companion object {
    private const val NOTIF_ID = 1001
    private const val TAG = "CameraService"
    
    @Volatile
    private var isServiceRunning = false
    
    @Volatile
    private var serviceInstance: CameraService? = null
    
    /**
     * Check if camera service is currently running
     */
    fun isRunning(): Boolean = isServiceRunning
    
    /**
     * Get the current service instance (for direct method calls)
     */
    fun getInstance(): CameraService? = serviceInstance
    
    /**
     * Safely start the camera service
     */
    fun safeStart(context: Context): Boolean {
      return try {
        val intent = Intent(context, CameraService::class.java)
        context.startForegroundService(intent)
        true
      } catch (e: Exception) {
        Log.e(TAG, "Failed to start camera service", e)
        false
      }
    }
  }
}