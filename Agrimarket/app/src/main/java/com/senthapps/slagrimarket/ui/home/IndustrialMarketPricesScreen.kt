package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.data.model.PriceTrend
import com.senthapps.slagrimarket.data.model.getCropEmoji
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.LocalTextScale
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable

// ============================================================================
// HUMAN INDUSTRIAL MARKET PRICES SCREEN v3.0
// "The Government Price Board" - crop emoji, trend arrows, offline badge
// ============================================================================

/**
 * Market price data class — now includes trend and change for display
 */
data class MarketPriceItem(
    val productName: String,
    val productNameSinhala: String = productName,
    val productNameTamil: String = productName,
    val price: Double,
    val unit: String,
    val emoji: String = "🌱",
    val trend: PriceTrend = PriceTrend.STABLE,
    val changeAmount: Double = 0.0
)

/**
 * Sample market price data per UI plan
 */
val SAMPLE_MARKET_PRICES = listOf(
    MarketPriceItem("Tomatoes", "තක්කාලි", "தக்காளி", 280.0, "kg", "🍅", PriceTrend.UP, 12.0),
    MarketPriceItem("Beans", "බෝංචි", "பீன்ஸ்", 320.0, "kg", "🫘", PriceTrend.STABLE, 0.0),
    MarketPriceItem("Eggplant", "වම්බටු", "கத்திரிக்காய்", 150.0, "kg", "🍆", PriceTrend.DOWN, -8.0),
    MarketPriceItem("Cabbage", "ගෝවා", "முட்டைக்கோஸ்", 180.0, "kg", "🥬", PriceTrend.UP, 5.0),
    MarketPriceItem("Carrot", "කැරට්", "கேரட்", 240.0, "kg", "🥕", PriceTrend.STABLE, 0.0),
    MarketPriceItem("Coconut", "පොල්", "தேங்காய்", 85.0, "nut", "🥥", PriceTrend.UP, 3.0),
    MarketPriceItem("Paddy", "වී", "நெல்", 95.0, "kg", "🌾", PriceTrend.STABLE, 0.0),
    MarketPriceItem("Mango", "අඹ", "மாம்பழம்", 350.0, "kg", "🥭", PriceTrend.DOWN, -15.0),
    MarketPriceItem("Red Onion", "රතු ළූණු", "வெங்காயம்", 120.0, "kg", "🧅", PriceTrend.UP, 6.0),
    MarketPriceItem("Chili", "මිරිස්", "மிளகாய்", 280.0, "kg", "🌶️", PriceTrend.DOWN, -33.0)
)

/**
 * Industrial market prices screen — amber header, crop emoji, trend arrows, offline support
 *
 * @param prices List of market prices to display
 * @param language Current language for labels
 * @param lastUpdatedText Last updated timestamp text
 * @param onNavigateBack Callback to navigate back
 * @param isLoading Loading state
 * @param isError Error state
 * @param isOffline True when showing cached data without live network
 * @param onRetry Retry callback
 */
@Composable
fun IndustrialMarketPricesScreen(
    prices: List<MarketPriceItem> = SAMPLE_MARKET_PRICES,
    language: AppLanguage = AppLanguage.SINHALA,
    lastUpdatedText: String? = null,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    isError: Boolean = false,
    isOffline: Boolean = false,
    onRetry: () -> Unit = {}
) {
    val updatedText = lastUpdatedText ?: when (language) {
        AppLanguage.SINHALA -> "අද උදේ 6:00 ට යාවත්කාලීන විය"
        AppLanguage.TAMIL -> "இன்று காலை 6:00 மணிக்கு புதுப்பிக்கப்பட்டது"
        AppLanguage.ENGLISH -> "UPDATED TODAY 6:00 AM"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .background(HumanIndustrial.Rice)
    ) {
        // Header — amber/harvest gradient feel
        PricesHeader(
            backText = when (language) {
                AppLanguage.SINHALA -> "ආපසු"
                AppLanguage.TAMIL -> "பின்"
                AppLanguage.ENGLISH -> "BACK"
            },
            title = when (language) {
                AppLanguage.SINHALA -> "🏪 මිල ගණන්"
                AppLanguage.TAMIL -> "🏪 விலைகள்"
                AppLanguage.ENGLISH -> "🏪 PRICES"
            },
            onBackClick = onNavigateBack
        )

        // 4dp Earth divider
        EarthDivider()

        // Updated bar — shows LIVE dot or OFFLINE badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(if (isOffline) Color(0xFFD4A84B) else HumanIndustrial.Dust)
                .padding(horizontal = Spacing.lg.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isOffline) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm.dp)
                ) {
                    Text(
                        text = "📵",
                        fontSize = 14.sp
                    )
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "OFFLINE — පෙර දත්ත"
                            AppLanguage.TAMIL -> "OFFLINE — தற்காலிக தரவு"
                            AppLanguage.ENGLISH -> "OFFLINE — Cached data"
                        },
                        style = HumanIndustrialType.sectionLabel,
                        color = HumanIndustrial.Ink
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF22c55e))
                    )
                    Text(
                        text = updatedText,
                        style = HumanIndustrialType.timestamp,
                        color = HumanIndustrial.Stone,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 4dp Earth divider
        EarthDivider()

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "රැඳී සිටින්න"
                            AppLanguage.TAMIL -> "காத்திருங்கள்"
                            AppLanguage.ENGLISH -> "WAIT"
                        },
                        style = HumanIndustrialType.body,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            isError -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg.dp)
                    ) {
                        Text(
                            text = when (language) {
                                AppLanguage.SINHALA -> "සම්බන්ධ වීමට නොහැකි විය"
                                AppLanguage.TAMIL -> "இணைக்க முடியவில்லை"
                                AppLanguage.ENGLISH -> "COULD NOT CONNECT"
                            },
                            style = HumanIndustrialType.body,
                            color = HumanIndustrial.Stone
                        )
                        SecondaryButton(
                            text = when (language) {
                                AppLanguage.SINHALA -> "නැවත උත්සාහ කරන්න"
                                AppLanguage.TAMIL -> "மீண்டும் முயற்சிக்கவும்"
                                AppLanguage.ENGLISH -> "TRY AGAIN"
                            },
                            onClick = onRetry
                        )
                    }
                }
            }
            prices.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "මෙහි තවම කිසිවක් නැත"
                            AppLanguage.TAMIL -> "இங்கே இன்னும் எதுவும் இல்லை"
                            AppLanguage.ENGLISH -> "NOTHING HERE YET"
                        },
                        style = HumanIndustrialType.body,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(prices) { index, price ->
                        PriceRow(
                            price = price,
                            language = language,
                            useAlternateBackground = index % 2 == 1
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// HEADER
// ============================================================================

@Composable
private fun PricesHeader(
    backText: String,
    title: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(Color(0xFF5C3317)) // Dark earth header matching home
            .padding(horizontal = Spacing.lg.dp, vertical = Spacing.md.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .height(48.dp)
                .industrialClickable(onClick = onBackClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = backText,
                style = HumanIndustrialType.sectionLabel,
                color = Color(0xFFD4A84B)
            )
        }

        Text(
            text = title,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Rice,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

// ============================================================================
// DIVIDER
// ============================================================================

@Composable
private fun EarthDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BorderWidth.Thick)
            .background(HumanIndustrial.Earth)
    )
}

// ============================================================================
// PRICE ROW — emoji left, name + Sinhala subtitle, trend + change right
// ============================================================================

@Composable
private fun PriceRow(
    price: MarketPriceItem,
    language: AppLanguage,
    useAlternateBackground: Boolean
) {
    val backgroundColor = if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice
    val textScale = LocalTextScale.current

    val productName = when (language) {
        AppLanguage.SINHALA -> price.productNameSinhala
        AppLanguage.TAMIL -> price.productNameTamil
        AppLanguage.ENGLISH -> price.productName
    }
    // Always show Sinhala subtitle (unless current language is Sinhala — then skip)
    val subtitle = when (language) {
        AppLanguage.SINHALA -> null
        AppLanguage.TAMIL -> price.productNameSinhala
        AppLanguage.ENGLISH -> price.productNameSinhala
    }

    val currencySymbol = when (language) {
        AppLanguage.SINHALA -> "රු"
        AppLanguage.TAMIL -> "ரூ"
        AppLanguage.ENGLISH -> "Rs"
    }
    val priceText = "$currencySymbol ${price.price.toInt()}/${price.unit}"

    val trendArrow = when (price.trend) {
        PriceTrend.UP -> "▲"
        PriceTrend.DOWN -> "▼"
        PriceTrend.STABLE -> "➡"
    }
    val trendColor = when (price.trend) {
        PriceTrend.UP -> Color(0xFF22c55e)
        PriceTrend.DOWN -> Color(0xFFef4444)
        PriceTrend.STABLE -> HumanIndustrial.Stone
    }

    val changeText = if (price.changeAmount != 0.0) {
        val sign = if (price.changeAmount > 0) "+" else ""
        " $sign${price.changeAmount.toInt()}"
    } else ""

    val scaledStyle = HumanIndustrialType.productName.copy(
        fontSize = HumanIndustrialType.productName.fontSize * textScale
    )
    val rowHeight = (if (subtitle != null) 88 else 72) * textScale

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(rowHeight.dp)
            .background(backgroundColor)
            .padding(horizontal = Spacing.lg.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left: emoji + name column
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm.dp),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = price.emoji,
                fontSize = (30 * textScale).sp
            )
            Column {
                Text(
                    text = productName,
                    style = scaledStyle,
                    color = HumanIndustrial.Ink
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = HumanIndustrialType.body.copy(
                            fontSize = HumanIndustrialType.body.fontSize * textScale
                        ),
                        color = HumanIndustrial.Stone
                    )
                }
            }
        }

        // Right: trend arrow + change + price
        Column(horizontalAlignment = Alignment.End) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = trendArrow,
                    style = scaledStyle.copy(fontSize = 16.sp * textScale),
                    color = trendColor
                )
                if (changeText.isNotEmpty()) {
                    Text(
                        text = changeText,
                        style = HumanIndustrialType.sectionLabel.copy(
                            fontSize = HumanIndustrialType.sectionLabel.fontSize * textScale
                        ),
                        color = trendColor
                    )
                }
            }
            Text(
                text = priceText,
                style = scaledStyle,
                color = HumanIndustrial.Gold
            )
        }
    }
}

// ============================================================================
// BACKWARD COMPATIBILITY — existing NavGraph passes List<MarketPrice>
// ============================================================================

@Composable
fun IndustrialMarketPricesScreen(
    marketPrices: List<com.senthapps.slagrimarket.data.model.MarketPrice>,
    onNavigateBack: () -> Unit,
    language: AppLanguage = AppLanguage.ENGLISH,
    lastUpdatedText: String = "Updated: Just now",
    isLoading: Boolean = false,
    isError: Boolean = false,
    isOffline: Boolean = false,
    onRetry: () -> Unit = {}
) {
    val convertedPrices = marketPrices.map { mp ->
        MarketPriceItem(
            productName = mp.cropNameEnglish,
            productNameSinhala = mp.cropNameSinhala,
            productNameTamil = mp.cropNameTamil,
            price = mp.currentPrice,
            unit = mp.unit,
            emoji = mp.getCropEmoji(),
            trend = mp.trend,
            changeAmount = mp.changeAmount
        )
    }

    IndustrialMarketPricesScreen(
        prices = convertedPrices,
        language = language,
        lastUpdatedText = lastUpdatedText,
        onNavigateBack = onNavigateBack,
        isLoading = isLoading,
        isError = isError,
        isOffline = isOffline,
        onRetry = onRetry
    )
}

// ============================================================================
// PREVIEWS
// ============================================================================

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MarketPricesPreview() {
    IndustrialMarketPricesScreen(
        prices = SAMPLE_MARKET_PRICES,
        language = AppLanguage.SINHALA,
        onNavigateBack = {}
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MarketPricesOfflinePreview() {
    IndustrialMarketPricesScreen(
        prices = SAMPLE_MARKET_PRICES,
        language = AppLanguage.ENGLISH,
        onNavigateBack = {},
        isOffline = true
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun MarketPricesLoadingPreview() {
    IndustrialMarketPricesScreen(
        prices = emptyList(),
        language = AppLanguage.ENGLISH,
        onNavigateBack = {},
        isLoading = true
    )
}
