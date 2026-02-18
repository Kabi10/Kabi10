package com.senthapps.slagrimarket.ui.components

import android.Manifest
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.*

/**
 * Voice input button for Tamil, Sinhala, and English speech recognition
 * Critical for elderly farmers with low literacy
 *
 * @param onVoiceResult Called with recognized speech text
 * @param language Language code: "ta", "si", or "en"
 * @param enabled Whether the button is interactive
 * @param onShowNumericKeypad Optional callback — if provided, shows a "123" fallback link below the mic
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun VoiceInputButton(
    onVoiceResult: (String) -> Unit,
    language: String = "ta", // Default to Tamil for Jaffna farmers
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    onShowNumericKeypad: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }
    var showPermissionDenied by remember { mutableStateOf(false) }

    // Map language codes to Android locale codes
    val localeMap = mapOf(
        "ta" to "ta-IN",  // Tamil (India/Sri Lanka)
        "si" to "si-LK",  // Sinhala (Sri Lanka)
        "en" to "en-US"   // English
    )

    // Request microphone permission
    val permissionState = rememberPermissionState(
        Manifest.permission.RECORD_AUDIO
    )

    // Speech recognition launcher
    val speechRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            if (!matches.isNullOrEmpty()) {
                // Return the best match
                onVoiceResult(matches[0])
            }
        }
    }

    // Permission request launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchSpeechRecognition(
                context = context,
                language = localeMap[language] ?: "ta-IN",
                launcher = speechRecognitionLauncher,
                onListeningChanged = { isListening = it }
            )
        } else {
            showPermissionDenied = true
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(
            onClick = {
                when {
                    !permissionState.status.isGranted -> {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                    else -> {
                        launchSpeechRecognition(
                            context = context,
                            language = localeMap[language] ?: "ta-IN",
                            launcher = speechRecognitionLauncher,
                            onListeningChanged = { isListening = it }
                        )
                    }
                }
            },
            enabled = enabled
        ) {
            if (isListening) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "🎤",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (enabled) MaterialTheme.colorScheme.primary
                           else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                )
            }
        }

        // "123" keypad fallback link — shown only when caller provides the callback
        if (onShowNumericKeypad != null) {
            TextButton(
                onClick = onShowNumericKeypad,
                enabled = enabled,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
            ) {
                Text(
                    text = "123",
                    fontSize = 11.sp,
                    color = if (enabled) Color(0xFF6B6B6B) else Color(0xFF6B6B6B).copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    // Permission denied snackbar
    if (showPermissionDenied) {
        AlertDialog(
            onDismissRequest = { showPermissionDenied = false },
            title = {
                Text(
                    text = when (language) {
                        "ta" -> "மைக்ரோஃபோன் அனுமதி தேவை"
                        "si" -> "මයික්‍රෆෝන අවසරය අවශ්‍යයි"
                        else -> "Microphone Permission Required"
                    }
                )
            },
            text = {
                Text(
                    text = when (language) {
                        "ta" -> "குரல் உள்ளீட்டைப் பயன்படுத்த மைக்ரோஃபோன் அனுமதி தேவை"
                        "si" -> "හඬ ආදානය භාවිතා කිරීමට මයික්‍රෆෝන අවසරය අවශ්‍යයි"
                        else -> "Microphone permission is required to use voice input"
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showPermissionDenied = false }) {
                    Text(
                        when (language) {
                            "ta" -> "சரி"
                            "si" -> "හරි"
                            else -> "OK"
                        }
                    )
                }
            }
        )
    }
}

/**
 * Launch speech recognition intent
 */
private fun launchSpeechRecognition(
    context: android.content.Context,
    language: String,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onListeningChanged: (Boolean) -> Unit
) {
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
        putExtra(RecognizerIntent.EXTRA_PROMPT, when (language) {
            "ta-IN" -> "தயவுசெய்து பேசுங்கள்..." // Please speak (Tamil)
            "si-LK" -> "කරුණාකර කතා කරන්න..." // Please speak (Sinhala)
            else -> "Please speak..."
        })
        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    try {
        onListeningChanged(true)
        launcher.launch(intent)
    } catch (e: Exception) {
        onListeningChanged(false)
        // Speech recognition not available
        android.util.Log.e("VoiceInput", "Speech recognition failed", e)
    }
}

/**
 * Voice input text field - combines regular text input with voice button
 */
@Composable
fun VoiceTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    language: String = "ta",
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isError: Boolean = false,
    supportingText: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        enabled = enabled,
        isError = isError,
        supportingText = supportingText,
        trailingIcon = {
            VoiceInputButton(
                onVoiceResult = onValueChange,
                language = language,
                enabled = enabled
            )
        },
        modifier = modifier.fillMaxWidth()
    )
}
