package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.TouchTargets

// ============================================================================
// HUMAN INDUSTRIAL MARKET PRICES SCREEN v1.0
// "The Government Price Board" - Authoritative, trustworthy, clear
// Product 18sp left, Price 28sp right, alternating Rice/Dust rows
// ============================================================================

/**
 * Market prices screen - the authoritative price reference
 * Header: Earth background, Rice text, timestamp
 * Rows: Product name left, price right, no unit per row
 */
@Composable
fun IndustrialMarketPricesScreen(
    marketPrices: List<MarketPrice>,
    onNavigateBack: () -> Unit,
    lastUpdatedText: String = "Updated: Just now",
    isLoading: Boolean = false,
    isError: Boolean = false,
    onRetry: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HumanIndustrial.Rice,
        topBar = {
            // Header: Earth background, Rice text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TouchTargets.button.dp)
                    .background(HumanIndustrial.Earth)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HumanIndustrial.Rice
                    )
                }
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading prices...",
                        style = HumanIndustrialType.emptyState,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            isError -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp),
                        modifier = Modifier.padding(Spacing.md.dp)
                    ) {
                        Text(
                            text = "Could not load prices",
                            style = HumanIndustrialType.emptyState,
                            color = HumanIndustrial.Stone
                        )
                        androidx.compose.material3.Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(TouchTargets.button.dp)
                                .border(
                                    width = BorderWidth.Standard,
                                    color = HumanIndustrial.Earth,
                                    shape = RectangleShape
                                ),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = HumanIndustrial.Rice,
                                contentColor = HumanIndustrial.Earth
                            ),
                            shape = RectangleShape
                        ) {
                            Text(
                                text = "TRY AGAIN",
                                style = HumanIndustrialType.button,
                                color = HumanIndustrial.Earth
                            )
                        }
                    }
                }
            }
            marketPrices.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No prices today",
                        style = HumanIndustrialType.emptyState,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Title section with timestamp
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HumanIndustrial.Rice)
                                .padding(vertical = Spacing.lg.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MARKET PRICES",
                                style = HumanIndustrialType.screenTitle,
                                color = HumanIndustrial.Ink,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs.dp))
                            Text(
                                text = lastUpdatedText,
                                style = HumanIndustrialType.timestamp,
                                color = HumanIndustrial.Stone,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(Spacing.xs.dp))
                            Text(
                                text = "Prices per kg",
                                style = HumanIndustrialType.unit,
                                color = HumanIndustrial.Stone,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Price list rows
                    itemsIndexed(marketPrices) { index, price ->
                        MarketPriceRow(
                            marketPrice = price,
                            useAlternateBackground = index % 2 == 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Price row per Human Industrial spec:
 * - Product: 18sp Bold UPPERCASE Ink, left-aligned
 * - Price: 28sp Bold Ink, right-aligned
 * - No unit per row (stated once in header)
 * - Rice/Dust alternating backgrounds
 * - 20dp vertical padding
 */
@Composable
private fun MarketPriceRow(
    marketPrice: MarketPrice,
    useAlternateBackground: Boolean
) {
    val backgroundColor = if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = Spacing.md.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Product name (left)
        Text(
            text = marketPrice.cropNameEnglish.uppercase(),
            style = HumanIndustrialType.priceRowProduct,
            color = HumanIndustrial.Ink
        )

        // Price (right) - no unit, just the number
        Text(
            text = marketPrice.currentPrice.toInt().toString(),
            style = HumanIndustrialType.price,
            color = HumanIndustrial.Ink
        )
    }
}
