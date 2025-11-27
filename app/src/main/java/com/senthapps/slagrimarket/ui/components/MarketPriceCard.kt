package com.senthapps.slagrimarket.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.data.model.PriceTrend
import com.senthapps.slagrimarket.data.model.SampleMarketPrices
import com.senthapps.slagrimarket.ui.theme.*
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Enhanced Market Price Card with Material Design 3 styling
 * Features trend indicators, animations, and trilingual support
 * Includes accessibility support for TalkBack
 */
@Composable
fun EnhancedMarketPriceCard(
    marketPrice: MarketPrice,
    currentLanguage: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    width: Dp = 180.dp,
    height: Dp = 160.dp
) {
    // Build accessibility description for TalkBack
    val cropName = SampleMarketPrices.getCropName(marketPrice, currentLanguage)
    val trendLabel = when (marketPrice.trend) {
        PriceTrend.UP -> when (currentLanguage) {
            "ta" -> "விலை உயர்வு"
            "si" -> "මිල ඉහළ"
            else -> "price up"
        }
        PriceTrend.DOWN -> when (currentLanguage) {
            "ta" -> "விலை குறைவு"
            "si" -> "මිල පහළ"
            else -> "price down"
        }
        PriceTrend.STABLE -> when (currentLanguage) {
            "ta" -> "விலை நிலையானது"
            "si" -> "මිල ස්ථාවර"
            else -> "price stable"
        }
    }
    val cardDescription = when (currentLanguage) {
        "ta" -> "$cropName, LKR ${String.format("%.0f", marketPrice.pricePerKg)} ஒரு ${marketPrice.unit}, $trendLabel ${String.format("%.1f", marketPrice.changePercentage)}%"
        "si" -> "$cropName, LKR ${String.format("%.0f", marketPrice.pricePerKg)} එක් ${marketPrice.unit}, $trendLabel ${String.format("%.1f", marketPrice.changePercentage)}%"
        else -> "$cropName, LKR ${String.format("%.0f", marketPrice.pricePerKg)} per ${marketPrice.unit}, $trendLabel ${String.format("%.1f", marketPrice.changePercentage)}%"
    }

    Card(
        onClick = onClick ?: {},
        modifier = modifier
            .size(width = width, height = height)
            .semantics { contentDescription = cardDescription },
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.Large),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Crop name with emoji
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
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

            // Price and trend
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    Text(
                        text = "LKR ${String.format("%.0f", marketPrice.pricePerKg)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Green600
                    )
                    Text(
                        text = "/${marketPrice.unit}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Trend indicator with percentage
                PriceTrendIndicator(
                    trend = marketPrice.trend,
                    changePercentage = marketPrice.changePercentage,
                    currentLanguage = currentLanguage
                )
            }

            // Location with freshness indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = getLocation(marketPrice, currentLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                FreshnessIndicator(lastUpdated = marketPrice.lastUpdated)
            }
        }
    }
}

@Composable
private fun PriceTrendIndicator(
    trend: PriceTrend,
    changePercentage: Double,
    currentLanguage: String
) {
    val trendColor = trend.getColor()
    val icon = when (trend) {
        PriceTrend.UP -> Icons.Default.KeyboardArrowUp
        PriceTrend.DOWN -> Icons.Default.KeyboardArrowDown
        PriceTrend.STABLE -> Icons.Default.Star
    }
    
    Surface(
        shape = RoundedCornerShape(CornerRadius.Small),
        color = trendColor.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.Small, vertical = Spacing.ExtraSmall),
            horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = trendColor,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "${if (changePercentage >= 0) "+" else ""}${String.format("%.1f", changePercentage)}%",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = trendColor
            )
        }
    }
}

@Composable
private fun FreshnessIndicator(lastUpdated: String) {
    val isRecent = try {
        val updateTime = Instant.parse(lastUpdated)
        ChronoUnit.HOURS.between(updateTime, Instant.now()) < 24
    } catch (e: Exception) {
        true
    }

    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(if (isRecent) Success600 else Warning500)
    )
}

private fun getLocation(marketPrice: MarketPrice, language: String): String = when (language) {
    "ta" -> marketPrice.locationTamil.ifEmpty { marketPrice.location }
    "si" -> marketPrice.locationSinhala.ifEmpty { marketPrice.location }
    else -> marketPrice.location
}

/**
 * Compact Market Price Card for grid layouts
 */
@Composable
fun CompactMarketPriceCard(
    marketPrice: MarketPrice,
    currentLanguage: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        enabled = onClick != null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Crop info
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = SampleMarketPrices.getCropEmoji(marketPrice.cropType),
                    style = MaterialTheme.typography.titleMedium
                )
                Column {
                    Text(
                        text = SampleMarketPrices.getCropName(marketPrice, currentLanguage),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = getLocation(marketPrice, currentLanguage),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Price and trend
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "LKR ${String.format("%.0f", marketPrice.pricePerKg)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )
                PriceTrendIndicator(
                    trend = marketPrice.trend,
                    changePercentage = marketPrice.changePercentage,
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

/**
 * Featured Market Price Card with larger display
 */
@Composable
fun FeaturedMarketPriceCard(
    marketPrice: MarketPrice,
    currentLanguage: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick ?: {},
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        enabled = onClick != null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Large),
            verticalArrangement = Arrangement.spacedBy(Spacing.Medium)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SampleMarketPrices.getCropEmoji(marketPrice.cropType),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Column {
                        Text(
                            text = SampleMarketPrices.getCropName(marketPrice, currentLanguage),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(Spacing.ExtraSmall),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = getLocation(marketPrice, currentLanguage),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                FreshnessIndicator(lastUpdated = marketPrice.lastUpdated)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

            // Price section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = when (currentLanguage) {
                            "ta" -> "தற்போதைய விலை"
                            "si" -> "වත්මන් මිල"
                            else -> "Current Price"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = "LKR ${String.format("%.2f", marketPrice.pricePerKg)}/${marketPrice.unit}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                PriceTrendIndicator(
                    trend = marketPrice.trend,
                    changePercentage = marketPrice.changePercentage,
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

