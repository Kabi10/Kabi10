package com.senthapps.slagrimarket.ui.listings

import android.speech.tts.TextToSpeech
import android.view.HapticFeedbackConstants
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.components.PrimaryButton
import com.senthapps.slagrimarket.ui.home.AppLanguage
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import java.util.Locale

// ============================================================================
// CREATE LISTING SUCCESS SCREEN (Screen I)
// "Listing Posted" confirmation screen
// Large ✅ emoji, haptic feedback, TTS readback, DONE button
// ============================================================================

/**
 * Create listing success screen - confirms listing was posted.
 *
 * Features:
 * - Haptic feedback on appear
 * - TTS reads back what was posted (crop, price, location)
 *
 * @param language Current language for labels and TTS
 * @param onDone Callback when DONE is clicked
 */
@Composable
fun CreateListingSuccessScreen(
    language: AppLanguage = AppLanguage.SINHALA,
    onDone: () -> Unit,
    viewModel: CreateListingSuccessViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val view = LocalView.current
    val cropType by viewModel.cropType.collectAsState()
    val price by viewModel.price.collectAsState()
    val location by viewModel.location.collectAsState()

    // Haptic feedback on appear
    LaunchedEffect(Unit) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    // TTS readback — speaks once, disposed on leave
    DisposableEffect(language) {
        var tts: TextToSpeech? = null
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = when (language) {
                    AppLanguage.SINHALA -> Locale("si", "LK")
                    AppLanguage.TAMIL -> Locale("ta", "IN")
                    AppLanguage.ENGLISH -> Locale.ENGLISH
                }
                tts?.language = locale

                // Build personalised message from last-used prefs (already written on success)
                val message = if (cropType.isNotBlank() && price.isNotBlank()) {
                    when (language) {
                        AppLanguage.SINHALA ->
                            "ඔබේ $cropType රු. $price ට $location සිට පළ විය. ගැනුම්කරුවන් ඔබට ඇමතීමට සූදානම් වෙති."
                        AppLanguage.TAMIL ->
                            "உங்கள் $cropType ரூ. $price க்கு $location இலிருந்து பதிவு இடப்பட்டது. வாங்குபவர்கள் தொடர்பு கொள்வார்கள்."
                        AppLanguage.ENGLISH ->
                            "Your $cropType at Rs $price from $location has been posted. Buyers will contact you soon."
                    }
                } else {
                    when (language) {
                        AppLanguage.SINHALA -> "ඔබේ දැන්වීම පළ විය. ගැනුම්කරුවන් ඔබට ඇමතීමට සූදානම් වෙති."
                        AppLanguage.TAMIL -> "உங்கள் விளம்பரம் வெளியிடப்பட்டது. வாங்குபவர்கள் தொடர்பு கொள்வார்கள்."
                        AppLanguage.ENGLISH -> "Your listing has been posted. Buyers will contact you soon."
                    }
                }
                tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "listing_success")
            }
        }
        onDispose { tts?.shutdown() }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xl.dp),
            modifier = Modifier.padding(bottom = 100.dp) // Offset upward from true center
        ) {
            // Large success tick
            Text(
                text = "✅",
                fontSize = 80.sp,
                textAlign = TextAlign.Center
            )

            // Success message
            Text(
                text = when (language) {
                    AppLanguage.SINHALA -> "දැන්වීම පළ විය!"
                    AppLanguage.TAMIL -> "பதிவு இடப்பட்டது!"
                    AppLanguage.ENGLISH -> "LISTING POSTED!"
                },
                style = HumanIndustrialType.screenTitle,
                color = HumanIndustrial.Green,
                textAlign = TextAlign.Center
            )

            // Motivational sub-message
            Text(
                text = when (language) {
                    AppLanguage.SINHALA -> "ගැනුම්කරුවන් ඔබට ඇමතීමට\nඅපේක්ෂා කරන්න"
                    AppLanguage.TAMIL -> "வாங்குபவர்கள் உங்களை\nதொடர்பு கொள்வார்கள்"
                    AppLanguage.ENGLISH -> "Buyers will contact\nyou soon"
                },
                style = HumanIndustrialType.body,
                color = HumanIndustrial.Stone,
                textAlign = TextAlign.Center
            )

            // DONE button - 200dp width centered
            PrimaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "හරි"
                    AppLanguage.TAMIL -> "சரி"
                    AppLanguage.ENGLISH -> "DONE"
                },
                onClick = onDone,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateListingSuccessPreview() {
    // Preview can't inject ViewModel — shown with default empty state
}
