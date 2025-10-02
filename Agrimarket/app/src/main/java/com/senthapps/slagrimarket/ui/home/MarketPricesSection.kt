package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.PriceTrend
import com.senthapps.slagrimarket.data.model.SampleMarketPrices
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@Composable
fun MarketPricesSection(
    onViewAllPrices: () -> Unit,
    modifier: Modifier = Modifier,
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val marketPrices = SampleMarketPrices.SAMPLE_PRICES
    
    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> stringResource(R.string.home_market_prices)
                    "ta" -> stringResource(R.string.home_market_prices)
                    "si" -> stringResource(R.string.home_market_prices)
                    else -> "${stringResource(R.string.home_market_prices)} / Market Prices"
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            TextButton(onClick = onViewAllPrices) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> stringResource(R.string.action_view_all)
                        "ta" -> stringResource(R.string.action_view_all)
                        "si" -> stringResource(R.string.action_view_all)
                        else -> "${stringResource(R.string.action_view_all)} / View All"
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Market prices horizontal scroll
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(marketPrices) { marketPrice ->
                MarketPriceCard(
                    marketPrice = marketPrice,
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

@Composable
private fun MarketPriceCard(
    marketPrice: MarketPrice,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.size(width = 180.dp, height = 160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Crop name with emoji
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = SampleMarketPrices.getCropEmoji(marketPrice.cropType),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = SampleMarketPrices.getCropName(marketPrice, currentLanguage),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Price and trend with enhanced styling
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "LKR ${String.format("%.0f", marketPrice.pricePerKg)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Enhanced trend indicator with background
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = when (marketPrice.trend) {
                            PriceTrend.UP -> Color(0xFF16a34a).copy(alpha = 0.1f)
                            PriceTrend.DOWN -> Color(0xFFdc2626).copy(alpha = 0.1f)
                            PriceTrend.STABLE -> Color(0xFF6b7280).copy(alpha = 0.1f)
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = SampleMarketPrices.getTrendIcon(marketPrice.trend),
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${marketPrice.changePercentage}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = SampleMarketPrices.getTrendColor(marketPrice.trend)
                            )
                        }
                    }
                }

                Text(
                    text = when (currentLanguage) {
                        "en" -> "per kg"
                        "ta" -> "ஒரு கிலோ"
                        "si" -> "කිලෝ ග්‍රාමයකට"
                        else -> "per kg"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Location badge with enhanced styling
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = SampleMarketPrices.getLocationName(marketPrice.location, currentLanguage),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
