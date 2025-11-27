package com.senthapps.slagrimarket.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.theme.CornerRadius
import com.senthapps.slagrimarket.ui.theme.Spacing

/**
 * Shimmer effect brush for loading skeletons
 */
@Composable
fun shimmerBrush(): Brush {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim)
    )
}

/**
 * Basic shimmer box for custom skeleton layouts
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = CornerRadius.Small
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(shimmerBrush())
    )
}

/**
 * Circular shimmer for avatars and icons
 */
@Composable
fun ShimmerCircle(
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(shimmerBrush())
    )
}

/**
 * Listing card skeleton for loading state
 */
@Composable
fun ListingCardSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadius.Large))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small)
                ) {
                    ShimmerCircle(size = 40.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ShimmerBox(
                            modifier = Modifier
                                .width(120.dp)
                                .height(16.dp)
                        )
                        ShimmerBox(
                            modifier = Modifier
                                .width(80.dp)
                                .height(12.dp)
                        )
                    }
                }
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp),
                    cornerRadius = CornerRadius.Small
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Medium))

            // Price and quantity row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .width(80.dp)
                            .height(16.dp)
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(100.dp)
                            .height(20.dp)
                    )
                    ShimmerBox(
                        modifier = Modifier
                            .width(60.dp)
                            .height(12.dp)
                    )
                }
            }
        }
    }
}

/**
 * Transaction card skeleton for loading state
 */
@Composable
fun TransactionCardSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CornerRadius.Large))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.Large)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ShimmerBox(modifier = Modifier.width(140.dp).height(16.dp))
                    ShimmerBox(modifier = Modifier.width(100.dp).height(12.dp))
                }
                ShimmerBox(
                    modifier = Modifier.width(80.dp).height(28.dp),
                    cornerRadius = CornerRadius.Full
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Medium))

            // Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ShimmerBox(modifier = Modifier.width(60.dp).height(12.dp))
                    ShimmerBox(modifier = Modifier.width(80.dp).height(14.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    ShimmerBox(modifier = Modifier.width(80.dp).height(12.dp))
                    ShimmerBox(modifier = Modifier.width(100.dp).height(14.dp))
                }
            }
        }
    }
}

/**
 * Market price card skeleton for loading state
 */
@Composable
fun MarketPriceCardSkeleton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 180.dp, height = 160.dp)
            .clip(RoundedCornerShape(CornerRadius.Large))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.Large),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Small)) {
                ShimmerCircle(size = 24.dp)
                ShimmerBox(modifier = Modifier.width(80.dp).height(16.dp))
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ShimmerBox(modifier = Modifier.width(100.dp).height(20.dp))
                ShimmerBox(modifier = Modifier.width(60.dp).height(12.dp))
            }
            ShimmerBox(
                modifier = Modifier.fillMaxWidth().height(24.dp),
                cornerRadius = CornerRadius.Small
            )
        }
    }
}

