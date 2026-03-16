package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import com.senthapps.slagrimarket.ui.theme.Spacing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.FieldMode
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.industrialClickable
import com.senthapps.slagrimarket.data.model.PriceTrend
import com.senthapps.slagrimarket.ui.settings.AccessibilityViewModel
import com.senthapps.slagrimarket.ui.sync.SyncStatusDot

// ============================================================================
// HUMAN INDUSTRIAL HOME SCREEN v3.0
// "The Village Square" - Greeting + ticker + four clear directions
// Per UI Plan: Greeting header, price ticker, BUY|SELL / PRICES|ORDERS grid
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
 * Human Industrial home screen - warm greeting + 2x2 grid layout
 *
 *   [GREETING header - ~88dp: farmer name, district, AA toggle]
 *   [PRICE TICKER - 44dp: scrolling crop prices]
 *   ─────────────────────────────────────────
 *   🛒 BUY        |   🌾 SELL [badge]
 *   මිලට ගන්න     |   විකුණන්න
 *   ──────────────+──────────────────────────
 *   📊 PRICES     |   📦 ORDERS
 *   මිල ගණන්      |   ගනුදෙනු
 *
 * @param farmerName Farmer's display name (from auth user)
 * @param farmerDistrict Farmer's district for location display
 * @param marketPrices Top prices for ticker strip
 * @param activeListingCount Count of seller's active listings (SELL badge)
 * @param onToggleTextSize Callback for AA text size cycle button
 */
@Composable
fun IndustrialHomeScreen(
    onNavigateToBuy: () -> Unit,
    onNavigateToSell: () -> Unit,
    onNavigateToPrices: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    language: AppLanguage = AppLanguage.SINHALA,
    farmerName: String = "",
    farmerDistrict: String = "",
    marketPrices: List<TickerPriceItem> = emptyList(),
    activeListingCount: Int = 0,
    onToggleTextSize: () -> Unit = {},
    accessibilityViewModel: AccessibilityViewModel = hiltViewModel()
) {
    val isFieldMode by accessibilityViewModel.isFieldModeEnabled.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isFieldMode) FieldMode.Background else HumanIndustrial.Rice)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ─── Greeting Header ──────────────────────────────────────────
            GreetingHeader(
                language = language,
                farmerName = farmerName,
                farmerDistrict = farmerDistrict,
                onNavigateToSettings = onNavigateToSettings,
                onToggleTextSize = onToggleTextSize,
                isFieldMode = isFieldMode,
                onToggleFieldMode = { accessibilityViewModel.toggleFieldMode(!isFieldMode) }
            )

            // Thick Earth divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BorderWidth.Thick)
                    .background(HumanIndustrial.Earth)
            )

            // ─── Price Ticker Strip ───────────────────────────────────────
            if (marketPrices.isNotEmpty()) {
                PriceTickerStrip(prices = marketPrices, language = language)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(BorderWidth.Thick)
                        .background(HumanIndustrial.Earth)
                )
            }

            // ─── 2×2 Grid ─────────────────────────────────────────────────
            // Top row: BUY | SELL
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // BUY quadrant (top-left)
                HomeQuadrant(
                    emoji = "🛒",
                    primaryText = when (language) {
                        AppLanguage.SINHALA -> "BUY"
                        AppLanguage.TAMIL -> "BUY"
                        AppLanguage.ENGLISH -> "BUY"
                    },
                    secondaryText = when (language) {
                        AppLanguage.SINHALA -> "මිලට ගන්න"
                        AppLanguage.TAMIL -> "வாங்கு"
                        AppLanguage.ENGLISH -> null
                    },
                    onClick = onNavigateToBuy,
                    isFieldMode = isFieldMode,
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

                // SELL quadrant (top-right) with active listing badge
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {
                    HomeQuadrant(
                        emoji = "🌾",
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
                        isFieldMode = isFieldMode,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Active listing count badge
                    if (activeListingCount > 0) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(Spacing.sm.dp)
                                .background(HumanIndustrial.Gold)
                                .padding(horizontal = Spacing.sm.dp, vertical = Spacing.xs.dp)
                        ) {
                            Text(
                                text = "$activeListingCount",
                                style = HumanIndustrialType.statusBadge,
                                color = HumanIndustrial.Rice
                            )
                        }
                    }
                }
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
                    emoji = "📊",
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
                    isFieldMode = isFieldMode,
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
                    emoji = "📦",
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
                    isFieldMode = isFieldMode,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

// ============================================================================
// GREETING HEADER
// ============================================================================

@Composable
private fun GreetingHeader(
    language: AppLanguage,
    farmerName: String,
    farmerDistrict: String,
    onNavigateToSettings: () -> Unit,
    onToggleTextSize: () -> Unit,
    isFieldMode: Boolean = false,
    onToggleFieldMode: () -> Unit = {}
) {
    val greeting = when (language) {
        AppLanguage.SINHALA -> "ආයුබෝවන්! 👋"
        AppLanguage.TAMIL -> "வணக்கம்! 👋"
        AppLanguage.ENGLISH -> "WELCOME! 👋"
    }
    val displayName = farmerName.ifBlank {
        when (language) {
            AppLanguage.SINHALA -> "ගොවියා"
            AppLanguage.TAMIL -> "விவசாயி"
            AppLanguage.ENGLISH -> "Farmer"
        }
    }
    val settingsLabel = when (language) {
        AppLanguage.SINHALA -> "සැකසුම්"
        AppLanguage.TAMIL -> "அமைப்புகள்"
        AppLanguage.ENGLISH -> "SETTINGS"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF5C3317)) // Dark earth — rich soil header
            .padding(horizontal = Spacing.lg.dp, vertical = Spacing.md.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greeting,
                        style = HumanIndustrialType.sectionLabel,
                        color = Color(0xFFD4A84B) // Warm gold on dark earth
                    )
                    Text(
                        text = "👨‍🌾 $displayName",
                        style = HumanIndustrialType.screenTitle,
                        color = HumanIndustrial.Rice
                    )
                    if (farmerDistrict.isNotBlank()) {
                        Text(
                            text = "📍 $farmerDistrict",
                            style = HumanIndustrialType.body,
                            color = Color(0xFFD4A84B)
                        )
                    }
                }

                // Right-side controls: sync dot + ☀️ Field Mode + AA + Settings
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
                ) {
                    // Sync status dot — shows live sync state
                    SyncStatusDot(modifier = Modifier.align(Alignment.End))

                    // ☀️ Field mode toggle — 72dp for sweaty outdoor fingers
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(if (isFieldMode) FieldMode.Accent else HumanIndustrial.Dust)
                            .industrialClickable(onClick = onToggleFieldMode),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("☀️", fontSize = 28.sp)
                    }
                    // AA text size cycle button
                    Box(
                        modifier = Modifier
                            .background(HumanIndustrial.Rice)
                            .industrialClickable(onClick = onToggleTextSize)
                            .padding(horizontal = Spacing.sm.dp, vertical = Spacing.xs.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "AA",
                            style = HumanIndustrialType.sectionLabel,
                            color = HumanIndustrial.Earth
                        )
                    }
                    // Settings link
                    Box(
                        modifier = Modifier
                            .industrialClickable(onClick = onNavigateToSettings)
                            .padding(Spacing.xs.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = settingsLabel,
                            style = HumanIndustrialType.timestamp,
                            color = Color(0xFFD4A84B)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// PRICE TICKER STRIP
// ============================================================================

/**
 * Lightweight price item for the ticker strip
 */
data class TickerPriceItem(
    val emoji: String,
    val cropName: String,     // In current language
    val price: Double,
    val unit: String,
    val trend: PriceTrend = PriceTrend.STABLE
)

@Composable
private fun PriceTickerStrip(
    prices: List<TickerPriceItem>,
    language: AppLanguage
) {
    val currencySymbol = when (language) {
        AppLanguage.SINHALA -> "රු"
        AppLanguage.TAMIL -> "ரூ"
        AppLanguage.ENGLISH -> "Rs"
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(Color(0xFFF5C518)) // Sun yellow ticker
            .padding(horizontal = Spacing.sm.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        LazyRow(
            contentPadding = PaddingValues(horizontal = Spacing.xs.dp),
            horizontalArrangement = Arrangement.spacedBy(Spacing.lg.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(prices) { item ->
                val trendArrow = when (item.trend) {
                    PriceTrend.UP -> "▲"
                    PriceTrend.DOWN -> "▼"
                    PriceTrend.STABLE -> "—"
                }
                val trendColor = when (item.trend) {
                    PriceTrend.UP -> Color(0xFF22c55e)
                    PriceTrend.DOWN -> Color(0xFFef4444)
                    PriceTrend.STABLE -> HumanIndustrial.Ink
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = item.emoji, fontSize = 16.sp)
                    Text(
                        text = item.cropName,
                        style = HumanIndustrialType.sectionLabel,
                        color = HumanIndustrial.Ink
                    )
                    Text(
                        text = "$currencySymbol${item.price.toInt()}",
                        style = HumanIndustrialType.sectionLabel,
                        color = HumanIndustrial.Earth
                    )
                    Text(
                        text = trendArrow,
                        style = HumanIndustrialType.sectionLabel,
                        color = trendColor
                    )
                }
            }
        }
    }
}

// ============================================================================
// HOME QUADRANT TILE
// ============================================================================

/**
 * Home quadrant tile - Rice background, emoji + Ink text + local-language label
 */
@Composable
private fun HomeQuadrant(
    emoji: String,
    primaryText: String,
    secondaryText: String?,
    onClick: () -> Unit,
    isFieldMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isFieldMode) FieldMode.Surface else HumanIndustrial.Rice
    val textColor = if (isFieldMode) FieldMode.Text else HumanIndustrial.Ink

    Box(
        modifier = modifier
            .industrialClickable(onClick = onClick)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = primaryText,
                style = HumanIndustrialType.homeTile,
                color = textColor,
                textAlign = TextAlign.Center
            )
            secondaryText?.let { secondary ->
                Text(
                    text = secondary,
                    style = HumanIndustrialType.homeTile,
                    color = textColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// PREVIEWS
// ============================================================================

private val sampleTicker = listOf(
    TickerPriceItem("🧅", "ළූණු", 120.0, "kg", PriceTrend.UP),
    TickerPriceItem("🌶️", "මිරිස්", 280.0, "kg", PriceTrend.DOWN),
    TickerPriceItem("🍅", "තක්කාලි", 95.0, "kg", PriceTrend.STABLE),
    TickerPriceItem("🥥", "පොල්", 45.0, "piece", PriceTrend.UP),
    TickerPriceItem("🌾", "සහල්", 220.0, "kg", PriceTrend.UP)
)

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun IndustrialHomeScreenPreview() {
    IndustrialHomeScreen(
        onNavigateToBuy = {},
        onNavigateToSell = {},
        onNavigateToPrices = {},
        onNavigateToOrders = {},
        language = AppLanguage.SINHALA,
        farmerName = "Suresh Kumar",
        farmerDistrict = "Jaffna",
        marketPrices = sampleTicker,
        activeListingCount = 3
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
        language = AppLanguage.TAMIL,
        farmerName = "Selvam",
        farmerDistrict = "யாழ்ப்பாணம்",
        marketPrices = listOf(
            TickerPriceItem("🧅", "வெங்காயம்", 120.0, "kg", PriceTrend.UP),
            TickerPriceItem("🌶️", "மிளகாய்", 280.0, "kg", PriceTrend.DOWN)
        ),
        activeListingCount = 0
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
        language = AppLanguage.ENGLISH,
        farmerName = "",
        farmerDistrict = "",
        marketPrices = emptyList(),
        activeListingCount = 1
    )
}
