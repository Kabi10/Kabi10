package com.senthapps.slagrimarket.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.data.model.Listing
import com.senthapps.slagrimarket.data.model.QualityGrade
import com.senthapps.slagrimarket.data.model.getCropName
import com.senthapps.slagrimarket.data.model.needsAttention
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.data.model.Units
import com.senthapps.slagrimarket.ui.theme.Green600
import com.senthapps.slagrimarket.ui.theme.Blue400
import com.senthapps.slagrimarket.util.TranslationUtil
import java.util.Locale

/**
 * Enhanced Listing Card with quality badges, urgent sale indicators, and animations
 * Matches the HTML mockup design with Material Design 3 styling
 */
@Composable
fun EnhancedListingCard(
    listing: Listing,
    onClick: () -> Unit,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier,
    showPriceTrend: Boolean = false,
    marketPrice: Double? = null
) {
    // Get translated values
    val cropName = listing.getCropName(currentLanguage)
    val locationName = TranslationUtil.getLocationName(listing.location, currentLanguage)
    val unitName = Units.getUnitName(listing.unit, currentLanguage)
    
    // Check if listing is urgent (expiring soon or needs attention)
    val isUrgent = listing.needsAttention()
    
    // Pulse animation for urgent badge
    val infiniteTransition = rememberInfiniteTransition(label = "urgent_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // Header row with crop emoji and badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Crop emoji and name
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = CropTypes.getCropEmoji(listing.cropType),
                            fontSize = 32.sp
                        )
                        Column {
                            Text(
                                text = cropName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = locationName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Quality grade badge
                    QualityBadge(
                        quality = listing.quality,
                        currentLanguage = currentLanguage
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price and quantity row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    // Quantity info
                    Column {
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Available"
                                "ta" -> "கிடைக்கும்"
                                "si" -> "ලබා ගත හැකිය"
                                else -> "Available"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${listing.quantity} $unitName",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Price info with optional trend
                    Column(horizontalAlignment = Alignment.End) {
                        if (showPriceTrend && marketPrice != null) {
                            PriceTrendIndicator(
                                listingPrice = listing.pricePerUnit,
                                marketPrice = marketPrice,
                                currentLanguage = currentLanguage
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                        
                        Text(
                            text = "LKR ${String.format(Locale.US, "%.0f", listing.pricePerUnit)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Green600
                        )
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "per $unitName"
                                "ta" -> "ஒரு $unitName"
                                "si" -> "එක් $unitName"
                                else -> "per $unitName"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Urgent sale badge (top-right corner, overlaying the card)
            if (isUrgent) {
                UrgentBadge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .scale(pulseScale),
                    currentLanguage = currentLanguage
                )
            }
        }
    }
}

/**
 * Quality grade badge component
 */
@Composable
private fun QualityBadge(
    quality: QualityGrade,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    val (badgeColor, badgeText) = when (quality) {
        QualityGrade.A -> Color(0xFF16a34a) to when (currentLanguage) {
            "en" -> "Grade A"
            "ta" -> "தரம் A"
            "si" -> "ශ්‍රේණිය A"
            else -> "Grade A"
        }
        QualityGrade.B -> Color(0xFF3b82f6) to when (currentLanguage) {
            "en" -> "Grade B"
            "ta" -> "தரம் B"
            "si" -> "ශ්‍රේණිය B"
            else -> "Grade B"
        }
        QualityGrade.C -> Color(0xFF6b7280) to when (currentLanguage) {
            "en" -> "Grade C"
            "ta" -> "தரம் C"
            "si" -> "ශ්‍රේණිය C"
            else -> "Grade C"
        }
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = badgeColor.copy(alpha = 0.15f)
    ) {
        Text(
            text = badgeText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = badgeColor,
            fontSize = 11.sp
        )
    }
}

/**
 * Urgent sale badge with pulse animation
 */
@Composable
private fun UrgentBadge(
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    val badgeText = when (currentLanguage) {
        "en" -> "⚡ URGENT"
        "ta" -> "⚡ அவசரம்"
        "si" -> "⚡ හදිසි"
        else -> "⚡ URGENT"
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFef4444)
    ) {
        Text(
            text = badgeText,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            fontSize = 10.sp
        )
    }
}

/**
 * Price trend indicator showing comparison with market price
 */
@Composable
private fun PriceTrendIndicator(
    listingPrice: Double,
    marketPrice: Double,
    currentLanguage: String,
    modifier: Modifier = Modifier
) {
    // Guard against invalid market price
    if (marketPrice <= 0.0) {
        val neutralColor = Color(0xFF6b7280)
        val neutralText = when (currentLanguage) {
            "en" -> "Market"
            "ta" -> "சந்தை"
            "si" -> "වෙළඳපොළ"
            else -> "Market"
        }
        Surface(
            modifier = modifier,
            shape = RoundedCornerShape(6.dp),
            color = neutralColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = neutralText,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = neutralColor,
                fontWeight = FontWeight.Medium
            )
        }
        return
    }

    val priceDiff = listingPrice - marketPrice
    val rawPercent = (priceDiff / marketPrice) * 100.0
    val safePercent = rawPercent
        .takeIf { it.isFinite() }
        ?.coerceIn(-99.0, 99.0)
        ?: 0.0
    val percentDiff = safePercent.toInt()

    val (trendIcon, trendColor, trendText) = when {
        percentDiff > 5 -> Triple(
            "↑",
            Color(0xFFef4444),
            when (currentLanguage) {
                "en" -> "+$percentDiff%"
                "ta" -> "+$percentDiff%"
                "si" -> "+$percentDiff%"
                else -> "+$percentDiff%"
            }
        )
        percentDiff < -5 -> Triple(
            "↓",
            Color(0xFF16a34a),
            when (currentLanguage) {
                "en" -> "$percentDiff%"
                "ta" -> "$percentDiff%"
                "si" -> "$percentDiff%"
                else -> "$percentDiff%"
            }
        )
        else -> Triple(
            "→",
            Color(0xFF6b7280),
            when (currentLanguage) {
                "en" -> "Market"
                "ta" -> "சந்தை"
                "si" -> "වෙළඳපොළ"
                else -> "Market"
            }
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = trendColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = trendIcon,
                fontSize = 10.sp,
                color = trendColor
            )
            Text(
                text = trendText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = trendColor,
                fontSize = 10.sp
            )
        }
    }
}

