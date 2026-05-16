package com.senthapps.slagrimarket.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Shine effect animation for listing images
 * Creates a diagonal shimmer effect similar to the HTML mockup
 */
@Composable
fun Modifier.shineEffect(
    enabled: Boolean = true,
    durationMillis: Int = 2000,
    delayMillis: Int = 0
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "shine_effect")
    
    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                delayMillis = delayMillis,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine_offset"
    )

    val shimmerColors = listOf(
        Color.Transparent,
        Color.White.copy(alpha = 0.3f),
        Color.White.copy(alpha = 0.5f),
        Color.White.copy(alpha = 0.3f),
        Color.Transparent
    )

    return this.drawWithContent {
        drawContent()
        val shimmerWidth = size.width
        drawRect(
            brush = Brush.linearGradient(
                colors = shimmerColors,
                start = Offset(offset * shimmerWidth, offset * shimmerWidth),
                end = Offset((offset + 0.5f) * shimmerWidth, (offset + 0.5f) * shimmerWidth)
            )
        )
    }
}

/**
 * Pulse animation modifier for urgent badges and important elements
 */
@Composable
fun Modifier.pulseAnimation(
    enabled: Boolean = true,
    minScale: Float = 1f,
    maxScale: Float = 1.1f,
    durationMillis: Int = 800
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Fade in animation for list items
 */
@Composable
fun Modifier.fadeInAnimation(
    delayMillis: Int = 0,
    durationMillis: Int = 300
): Modifier {
    val alpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "fade_in_alpha"
    )

    return this.graphicsLayer {
        this.alpha = alpha
    }
}

/**
 * Slide in from bottom animation
 */
@Composable
fun Modifier.slideInFromBottom(
    delayMillis: Int = 0,
    durationMillis: Int = 300,
    offsetY: Float = 50f
): Modifier {
    val translationY by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = FastOutSlowInEasing
        ),
        label = "slide_in_y"
    )

    return this.graphicsLayer {
        this.translationY = translationY
    }
}

/**
 * Bounce animation for buttons and interactive elements
 */
@Composable
fun Modifier.bounceAnimation(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 600
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "bounce_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_scale"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Glow effect for important elements
 */
@Composable
fun Modifier.glowEffect(
    enabled: Boolean = true,
    glowColor: Color = Color.White,
    durationMillis: Int = 1500
): Modifier {
    if (!enabled) return this

    val infiniteTransition = rememberInfiniteTransition(label = "glow_effect")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = durationMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    return this.background(
        brush = Brush.radialGradient(
            colors = listOf(
                glowColor.copy(alpha = alpha),
                Color.Transparent
            )
        )
    )
}

