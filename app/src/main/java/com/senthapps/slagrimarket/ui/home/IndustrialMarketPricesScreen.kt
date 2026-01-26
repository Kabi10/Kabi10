package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable

// ============================================================================
// HUMAN INDUSTRIAL MARKET PRICES SCREEN v2.0
// "The Government Price Board" - Today's going rates
// 72dp header, 40dp updated bar, 72dp rows with price/unit
// ============================================================================

/**
 * Market price data class
 */
data class MarketPriceItem(
    val productName: String,
    val productNameSinhala: String = productName,
    val productNameTamil: String = productName,
    val price: Double,
    val unit: String
)

/**
 * Sample market price data per UI plan
 */
val SAMPLE_MARKET_PRICES = listOf(
    MarketPriceItem("Tomatoes", "තක්කාලි", "தக்காளி", 280.0, "kg"),
    MarketPriceItem("Beans", "බෝංචි", "பீன்ஸ்", 320.0, "kg"),
    MarketPriceItem("Eggplant", "වම්බටු", "கத்திரிக்காய்", 150.0, "kg"),
    MarketPriceItem("Cabbage", "ගෝවා", "முட்டைக்கோஸ்", 180.0, "kg"),
    MarketPriceItem("Carrot", "කැරට්", "கேரட்", 240.0, "kg"),
    MarketPriceItem("Coconut", "පොල්", "தேங்காய்", 85.0, "nut"),
    MarketPriceItem("Paddy", "වී", "நெல்", 95.0, "kg"),
    MarketPriceItem("Mango", "අඹ", "மாம்பழம்", 350.0, "kg"),
    MarketPriceItem("Fish", "මාළු", "மீன்", 800.0, "kg"),
    MarketPriceItem("Chicken", "කුකුළු", "கோழி", 750.0, "kg")
)

/**
 * Industrial market prices screen - daily price reference
 *
 * Header: 72dp Rice with back text and title
 * Updated bar: 40dp Dust with timestamp
 * Rows: 72dp with product name left, price/unit right
 *
 * @param prices List of market prices
 * @param language Current language for labels
 * @param lastUpdatedText Last updated timestamp text
 * @param onNavigateBack Callback to navigate back
 * @param isLoading Loading state
 * @param isError Error state
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
            .background(HumanIndustrial.Rice)
    ) {
        // Header - 72dp
        PricesHeader(
            backText = when (language) {
                AppLanguage.SINHALA -> "ආපසු"
                AppLanguage.TAMIL -> "பின்"
                AppLanguage.ENGLISH -> "BACK"
            },
            title = when (language) {
                AppLanguage.SINHALA -> "මිල ගණන්"
                AppLanguage.TAMIL -> "விலைகள்"
                AppLanguage.ENGLISH -> "PRICES"
            },
            onBackClick = onNavigateBack
        )

        // 4dp Earth divider
        EarthDivider()

        // Updated bar - 40dp Dust background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(HumanIndustrial.Dust)
                .padding(horizontal = Spacing.lg.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = updatedText,
                style = HumanIndustrialType.timestamp,
                color = HumanIndustrial.Stone,
                textAlign = TextAlign.Center
            )
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

/**
 * Header component - 72dp Rice background
 */
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
            .background(HumanIndustrial.Rice)
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
                color = HumanIndustrial.Earth
            )
        }

        Text(
            text = title,
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

/**
 * 4dp Earth divider
 */
@Composable
private fun EarthDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(BorderWidth.Thick)
            .background(HumanIndustrial.Earth)
    )
}

/**
 * Price row - 72dp height
 * Product name left (20sp Bold), Price/unit right (20sp Bold Gold)
 */
@Composable
private fun PriceRow(
    price: MarketPriceItem,
    language: AppLanguage,
    useAlternateBackground: Boolean
) {
    val backgroundColor = if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice
    val productName = when (language) {
        AppLanguage.SINHALA -> price.productNameSinhala
        AppLanguage.TAMIL -> price.productNameTamil
        AppLanguage.ENGLISH -> price.productName
    }
    val currencySymbol = when (language) {
        AppLanguage.SINHALA -> "රු"
        AppLanguage.TAMIL -> "ரூ"
        AppLanguage.ENGLISH -> "Rs"
    }
    val priceText = "$currencySymbol ${price.price.toInt()}/${price.unit}"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(backgroundColor)
            .padding(horizontal = Spacing.lg.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = productName,
            style = HumanIndustrialType.productName,
            color = HumanIndustrial.Ink
        )
        Text(
            text = priceText,
            style = HumanIndustrialType.productName,
            color = HumanIndustrial.Gold
        )
    }
}

// ============================================================================
// BACKWARD COMPATIBILITY - Keep old function signature with MarketPrice
// ============================================================================

/**
 * Legacy function signature for backward compatibility
 */
@Composable
fun IndustrialMarketPricesScreen(
    marketPrices: List<com.senthapps.slagrimarket.data.model.MarketPrice>,
    onNavigateBack: () -> Unit,
    lastUpdatedText: String = "Updated: Just now",
    isLoading: Boolean = false,
    isError: Boolean = false,
    onRetry: () -> Unit = {}
) {
    val convertedPrices = marketPrices.map { mp ->
        MarketPriceItem(
            productName = mp.cropNameEnglish,
            productNameSinhala = mp.cropNameSinhala,
            productNameTamil = mp.cropNameTamil,
            price = mp.currentPrice,
            unit = "kg"
        )
    }

    IndustrialMarketPricesScreen(
        prices = convertedPrices,
        language = AppLanguage.ENGLISH,
        lastUpdatedText = lastUpdatedText,
        onNavigateBack = onNavigateBack,
        isLoading = isLoading,
        isError = isError,
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
private fun MarketPricesEnglishPreview() {
    IndustrialMarketPricesScreen(
        prices = SAMPLE_MARKET_PRICES,
        language = AppLanguage.ENGLISH,
        onNavigateBack = {}
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
