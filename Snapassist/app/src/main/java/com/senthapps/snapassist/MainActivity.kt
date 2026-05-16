package com.senthapps.snapassist

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import com.senthapps.snapassist.service.CameraService
import com.senthapps.snapassist.util.FCMTokenUtil
import com.senthapps.snapassist.util.BatteryOptimizationUtil
import com.senthapps.snapassist.ui.theme.SnapassistTheme

class MainActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Check if we came from a re-arm notification
        val showRearmMessage = intent.getBooleanExtra("show_rearm_message", false)
        if (showRearmMessage) {
            Log.d(TAG, "Opened from re-arm notification")
        }
        
        setContent {
            SnapassistTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CameraControlScreen(
                        modifier = Modifier.padding(innerPadding),
                        showRearmMessage = showRearmMessage
                    )
                }
            }
        }
    }
}

@Composable
fun CameraControlScreen(
    modifier: Modifier = Modifier,
    showRearmMessage: Boolean = false
) {
    val context = LocalContext.current
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true // Not required on API < 33
            }
        )
    }
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var isServiceRunning by remember { mutableStateOf(CameraService.isRunning()) }
    var isLoading by remember { mutableStateOf(false) }
    var showNotificationRationale by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }
    var showDebugInfo by remember { mutableStateOf(false) }
    var fcmToken by remember { mutableStateOf("Loading...") }
    var batteryOptimized by remember { mutableStateOf(true) }
    
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasNotificationPermission = granted
            if (!granted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                showNotificationRationale = true
            }
        }
    )
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasPermission = granted
            if (!granted) {
                showPermissionRationale = true
            }
        }
    )
    
    // Get FCM token for debugging and save to Firestore
    LaunchedEffect(Unit) {
        try {
            // Update token in Firestore (requires authentication)
            val savedToken = FCMTokenUtil.updateTokenInFirestore(context)
            if (savedToken != null) {
                fcmToken = savedToken
                Log.d("MainActivity", "FCM Token saved to Firestore: $fcmToken")
            } else {
                // Fallback to local token display
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        fcmToken = task.result
                        Log.d("MainActivity", "FCM Token (local only): $fcmToken")
                    } else {
                        fcmToken = "Failed to get token"
                        Log.w("MainActivity", "Fetching FCM registration token failed", task.exception)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error handling FCM token", e)
            fcmToken = "Error: ${e.message}"
        }
    }
    
    // Check battery optimization status
    LaunchedEffect(Unit) {
        batteryOptimized = !BatteryOptimizationUtil.isIgnoringBatteryOptimizations(context)
        BatteryOptimizationUtil.logBatteryOptimizationStatus(context)
    }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000) // Check every second
        isServiceRunning = CameraService.isRunning()
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Title
        Text(
            text = "Snapassist",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Remote Camera Control",
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Re-arm message if shown
        if (showRearmMessage) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = "Remote capture failed. Camera service was not armed.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Service Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Status Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status dot
                    Card(
                        modifier = Modifier.size(12.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isServiceRunning) {
                                Color(0xFF4CAF50) // Green
                            } else {
                                Color(0xFFF44336) // Red
                            }
                        )
                    ) {}
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Text(
                        text = if (isServiceRunning) "Armed" else "Disarmed",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isServiceRunning) {
                            Color(0xFF4CAF50)
                        } else {
                            Color(0xFFF44336)
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = if (isServiceRunning) {
                        "Ready to capture photos remotely"
                    } else {
                        "Camera service is not running"
                    },
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Control Button
        if (!hasPermission) {
            // Camera Permission Request Button
            Button(
                onClick = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading
            ) {
                Text(
                    text = "Grant Camera Permission",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (showPermissionRationale) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera permission is required to capture photos remotely.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Notification Permission Request Button (Android 13+)
            Button(
                onClick = {
                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = "Grant Notification Permission",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (showNotificationRationale) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Notification permission is required to show the armed status and alert you when remote capture fails.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            // Service Toggle Button
            Button(
                onClick = {
                    isLoading = true
                    
                    if (isServiceRunning) {
                        // Stop service
                        val intent = Intent(context, CameraService::class.java)
                        context.stopService(intent)
                        isServiceRunning = false
                        Log.d("MainActivity", "Camera service stopped")
                    } else {
                        // Start service with proper error handling
                        val success = CameraService.safeStart(context)
                        if (success) {
                            isServiceRunning = true
                            Log.d("MainActivity", "Camera service started successfully")
                        } else {
                            Log.e("MainActivity", "Failed to start camera service - may be restricted from background")
                            // Show user feedback about the failure
                        }
                    }
                    
                    isLoading = false
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isServiceRunning) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (isServiceRunning) "Disarm Camera" else "Arm Camera",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "How it works:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Arm the camera service\n" +
                            "2. Send FCM message with \"cmd\": \"SNAP\"\n" +
                            "3. Photo captured and uploaded to Firebase",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Debug Section Toggle
        Button(
            onClick = { showDebugInfo = !showDebugInfo },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Text(
                text = if (showDebugInfo) "Hide Debug Info" else "Show Debug Info",
                fontSize = 14.sp
            )
        }
        
        if (showDebugInfo) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Debug Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Debug Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // FCM Token Display
                    Text(
                        text = "FCM Token:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Text(
                            text = fcmToken,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simulate SNAP Button
                    Button(
                        onClick = {
                            if (CameraService.isRunning()) {
                                Log.d("MainActivity", "Simulating SNAP command")
                                // TODO: Implement actual simulation
                            } else {
                                Log.w("MainActivity", "Cannot simulate SNAP - service not running")
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = CameraService.isRunning(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "Simulate SNAP Command",
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "API Level: ${Build.VERSION.SDK_INT}\n" +
                                "Device: ${Build.MANUFACTURER} ${Build.MODEL}\n" +
                                "Android: ${Build.VERSION.RELEASE}\n" +
                                "Battery Optimized: ${if (batteryOptimized) "Yes (may affect FCM)" else "No (optimal)"}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CameraControlScreenPreview() {
    SnapassistTheme {
        CameraControlScreen()
    }
}