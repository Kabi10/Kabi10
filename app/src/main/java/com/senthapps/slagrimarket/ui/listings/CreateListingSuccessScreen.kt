package com.senthapps.slagrimarket.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.components.PrimaryButton
import com.senthapps.slagrimarket.ui.home.AppLanguage
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing

// ============================================================================
// CREATE LISTING SUCCESS SCREEN (Screen I)
// "Listing Posted" confirmation screen
// Success message centered, DONE button below
// ============================================================================

/**
 * Create listing success screen - confirms listing was posted
 *
 * Layout:
 * - Success message centered ~200dp from top
 * - DONE button centered below message with 32dp gap
 *
 * @param language Current language for labels
 * @param onDone Callback when DONE is clicked (should navigate to home and clear backstack)
 */
@Composable
fun CreateListingSuccessScreen(
    language: AppLanguage = AppLanguage.SINHALA,
    onDone: () -> Unit
) {
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
            // Success message
            Text(
                text = when (language) {
                    AppLanguage.SINHALA -> "දැන්වීම පළ විය"
                    AppLanguage.TAMIL -> "பதிவு இடப்பட்டது"
                    AppLanguage.ENGLISH -> "LISTING POSTED"
                },
                style = HumanIndustrialType.screenTitle,
                color = HumanIndustrial.Green,
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
    CreateListingSuccessScreen(
        language = AppLanguage.SINHALA,
        onDone = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateListingSuccessEnglishPreview() {
    CreateListingSuccessScreen(
        language = AppLanguage.ENGLISH,
        onDone = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun CreateListingSuccessTamilPreview() {
    CreateListingSuccessScreen(
        language = AppLanguage.TAMIL,
        onDone = {}
    )
}
