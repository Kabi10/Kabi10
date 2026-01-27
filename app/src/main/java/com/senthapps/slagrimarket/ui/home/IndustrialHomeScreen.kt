package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import com.senthapps.slagrimarket.ui.theme.Spacing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.industrialClickable

// ============================================================================
// HUMAN INDUSTRIAL HOME SCREEN v2.0
// "The Village Square" - Four clear directions, four clear purposes
// Per UI Plan: BUY|SELL on top, PRICES|ORDERS on bottom
// Rice background, Earth dividers, bold centered text with Sinhala labels
// ============================================================================

/**
 * Supported languages for UI text
 */
enum class AppLanguage {
    SINHALA,
    TAMIL,
    ENGLISH
}

/**
 * Human Industrial home screen - warm 2x2 grid layout
 * Fixed layout per UI Plan:
 *
 *   [SETTINGS header - 48dp]
 *   -----------------------
 *   BUY        |    SELL
 *   මිලට ගන්න   |   විකුණන්න
 *   -----------+-----------
 *   PRICES     |   ORDERS
 *   මිල ගණන්    |    ගනුදෙනු
 *
 * @param onNavigateToBuy Navigate to buy flow (category selection)
 * @param onNavigateToSell Navigate to sell flow (category selection)
 * @param onNavigateToPrices Navigate to market prices screen
 * @param onNavigateToOrders Navigate to transactions/orders screen
 * @param onNavigateToSettings Navigate to language settings screen
 * @param language Current language for labels (default: SINHALA)
 */
@Composable
fun IndustrialHomeScreen(
    onNavigateToBuy: () -> Unit,
    onNavigateToSell: () -> Unit,
    onNavigateToPrices: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    language: AppLanguage = AppLanguage.SINHALA
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Settings header - 56dp height (minimum touch target per Industrial spec)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(HumanIndustrial.Rice)
                    .padding(horizontal = Spacing.lg.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .industrialClickable(onClick = onNavigateToSettings)
                        .padding(horizontal = Spacing.md.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "සැකසුම්"
                            AppLanguage.TAMIL -> "அமைப்புகள்"
                            AppLanguage.ENGLISH -> "SETTINGS"
                        },
                        style = HumanIndustrialType.sectionLabel,
                        color = HumanIndustrial.Earth
                    )
                }
            }

            // Top row: BUY | SELL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // BUY quadrant (top-left)
                HomeQuadrant(
                    primaryText = when (language) {
                        AppLanguage.SINHALA -> "BUY"
                        AppLanguage.TAMIL -> "BUY"
                        AppLanguage.ENGLISH -> "BUY"
                    },
                    secondaryText = when (language) {
                        AppLanguage.SINHALA -> "මිලට ගන්න"
                        AppLanguage.TAMIL -> "வாங்க"
                        AppLanguage.ENGLISH -> null
                    },
                    onClick = onNavigateToBuy,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(BorderWidth.Thick)
                        .fillMaxHeight()
                        .background(HumanIndustrial.Earth)
                )

                // SELL quadrant (top-right)
                HomeQuadrant(
                    primaryText = when (language) {
                        AppLanguage.SINHALA -> "SELL"
                        AppLanguage.TAMIL -> "SELL"
                        AppLanguage.ENGLISH -> "SELL"
                    },
                    secondaryText = when (language) {
                        AppLanguage.SINHALA -> "විකුණන්න"
                        AppLanguage.TAMIL -> "விற்க"
                        AppLanguage.ENGLISH -> null
                    },
                    onClick = onNavigateToSell,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            // Horizontal divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BorderWidth.Thick)
                    .background(HumanIndustrial.Earth)
            )

            // Bottom row: PRICES | ORDERS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // PRICES quadrant (bottom-left)
                HomeQuadrant(
                    primaryText = when (language) {
                        AppLanguage.SINHALA -> "PRICES"
                        AppLanguage.TAMIL -> "PRICES"
                        AppLanguage.ENGLISH -> "PRICES"
                    },
                    secondaryText = when (language) {
                        AppLanguage.SINHALA -> "මිල ගණන්"
                        AppLanguage.TAMIL -> "விலைகள்"
                        AppLanguage.ENGLISH -> null
                    },
                    onClick = onNavigateToPrices,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(BorderWidth.Thick)
                        .fillMaxHeight()
                        .background(HumanIndustrial.Earth)
                )

                // ORDERS quadrant (bottom-right)
                HomeQuadrant(
                    primaryText = when (language) {
                        AppLanguage.SINHALA -> "ORDERS"
                        AppLanguage.TAMIL -> "ORDERS"
                        AppLanguage.ENGLISH -> "ORDERS"
                    },
                    secondaryText = when (language) {
                        AppLanguage.SINHALA -> "ගනුදෙනු"
                        AppLanguage.TAMIL -> "ஆர்டர்கள்"
                        AppLanguage.ENGLISH -> null
                    },
                    onClick = onNavigateToOrders,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Home quadrant tile - Rice background, Ink text
 * 24sp Bold centered text with optional secondary text below
 * Uses IndustrialIndication for pressed state (Earth 15% overlay)
 */
@Composable
private fun HomeQuadrant(
    primaryText: String,
    secondaryText: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .industrialClickable(onClick = onClick)
            .background(HumanIndustrial.Rice),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = primaryText,
                style = HumanIndustrialType.homeTile,
                color = HumanIndustrial.Ink,
                textAlign = TextAlign.Center
            )
            secondaryText?.let { secondary ->
                Text(
                    text = secondary,
                    style = HumanIndustrialType.homeTile,
                    color = HumanIndustrial.Ink,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun IndustrialHomeScreenPreview() {
    IndustrialHomeScreen(
        onNavigateToBuy = {},
        onNavigateToSell = {},
        onNavigateToPrices = {},
        onNavigateToOrders = {},
        language = AppLanguage.SINHALA
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun IndustrialHomeScreenEnglishPreview() {
    IndustrialHomeScreen(
        onNavigateToBuy = {},
        onNavigateToSell = {},
        onNavigateToPrices = {},
        onNavigateToOrders = {},
        language = AppLanguage.ENGLISH
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun IndustrialHomeScreenTamilPreview() {
    IndustrialHomeScreen(
        onNavigateToBuy = {},
        onNavigateToSell = {},
        onNavigateToPrices = {},
        onNavigateToOrders = {},
        language = AppLanguage.TAMIL
    )
}
