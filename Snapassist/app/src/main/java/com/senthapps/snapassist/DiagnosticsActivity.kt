package com.senthapps.snapassist

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.Timestamp
import com.senthapps.snapassist.service.CameraService
import com.senthapps.snapassist.util.UsageAccess
import com.senthapps.snapassist.util.AppVisibility
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import java.io.File

class DiagnosticsActivity : ComponentActivity() {
    private val notifPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { UI() }
        // Ensure token is stored at launch
        Firebase.messaging.token.addOnSuccessListener { t ->
            Firebase.firestore.collection("devices").document("primary")
                .set(mapOf("token" to t, "updatedAt" to Timestamp.now()), SetOptions.merge())
        }
    }

    @Composable
    private fun UI() {
        val scope = rememberCoroutineScope()
        var token by remember { mutableStateOf("…") }
        var armed by remember { mutableStateOf(false) }
        var lastResult by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            token = Firebase.messaging.token.await()
        }

        Scaffold { pad ->
            Column(
                Modifier.padding(pad).padding(16.dp).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Snapassist Diagnostics", style = MaterialTheme.typography.titleLarge)
                Text("FCM token:\n$token", style = MaterialTheme.typography.bodySmall)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        ContextCompat.startForegroundService(
                            this@DiagnosticsActivity,
                            Intent(this@DiagnosticsActivity, CameraService::class.java)
                        )
                        armed = true
                    }) { Text("Arm (start service)") }

                    Button(onClick = {
                        stopService(Intent(this@DiagnosticsActivity, CameraService::class.java))
                        armed = false
                    }) { Text("Disarm (stop service)") }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        if (Build.VERSION.SDK_INT >= 33) {
                            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }) { Text("Request notifications (API 33+)") }

                    Button(onClick = {
                        UsageAccess.ensureGranted(this@DiagnosticsActivity)
                    }) { Text("Open Usage Access") }
                }

                Button(onClick = {
                    val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
                    if (Build.VERSION.SDK_INT >= 23 && !pm.isIgnoringBatteryOptimizations(packageName)) {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                        intent.data = Uri.parse("package:$packageName")
                        startActivity(intent)
                    }
                }) { Text("Request ignore battery optimizations") }

                Button(onClick = {
                    // Simulate SNAP locally (no FCM): capture to temp file and upload
                    scope.launch {
                        try {
                            val f = withContext(Dispatchers.IO) {
                                val tmp = File(externalCacheDir, "snap-${System.currentTimeMillis()}.jpg")
                                tmp
                            }
                            // Ask the running service to take a picture via a broadcast
                            val i = Intent("com.senthapps.snapassist.SNAP_LOCAL")
                            i.setPackage(packageName)
                            i.putExtra("path", f.absolutePath)
                            sendBroadcast(i)
                            lastResult = "Requested local SNAP → ${f.name}"
                        } catch (t: Throwable) {
                            lastResult = "SNAP failed: ${t.message}"
                        }
                    }
                }) { Text("Simulate SNAP (local)") }

                Text("Armed: $armed  |  App foreground: ${AppVisibility.isForeground(this@DiagnosticsActivity)}")
                Text(lastResult, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}