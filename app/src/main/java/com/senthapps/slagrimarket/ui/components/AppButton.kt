package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.theme.Green600
import com.senthapps.slagrimarket.ui.theme.Blue400
import com.senthapps.slagrimarket.ui.theme.CornerRadius
import com.senthapps.slagrimarket.ui.theme.Spacing

/**
 * Primary CTA button with Green-600 (#16a34a) background
 * Follows Material Design 3 guidelines with 16dp corner radius
 * Includes accessibility support for TalkBack
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    loadingContentDescription: String = "Loading"
) {
    // Accessibility: Announce loading state to screen readers
    val buttonDescription = if (isLoading) "$text, $loadingContentDescription" else text

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics { contentDescription = buttonDescription },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = ButtonDefaults.buttonColors(
            containerColor = Green600,
            contentColor = Color.White,
            disabledContainerColor = Green600.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = Spacing.Large, vertical = Spacing.Medium)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icons are decorative when button has text, so contentDescription = null is correct
                leadingIcon?.let {
                    Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                Text(text = text, fontWeight = FontWeight.SemiBold)
                trailingIcon?.let {
                    Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

/**
 * Secondary action button with Blue-400 (#60a5fa) background
 * Follows Material Design 3 guidelines with 16dp corner radius
 * Includes accessibility support for TalkBack
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
    loadingContentDescription: String = "Loading"
) {
    val buttonDescription = if (isLoading) "$text, $loadingContentDescription" else text

    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .semantics { contentDescription = buttonDescription },
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = ButtonDefaults.buttonColors(
            containerColor = Blue400,
            contentColor = Color.White,
            disabledContainerColor = Blue400.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.7f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(horizontal = Spacing.Large, vertical = Spacing.Medium)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon?.let {
                    Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(20.dp))
                }
                Text(text = text, fontWeight = FontWeight.Medium)
            }
        }
    }
}

/**
 * Outlined button with transparent background and primary color border
 */
@Composable
fun OutlinedAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(CornerRadius.Large),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Green600,
            disabledContentColor = Green600.copy(alpha = 0.5f)
        ),
        contentPadding = PaddingValues(horizontal = Spacing.Large, vertical = Spacing.Medium)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            leadingIcon?.let {
                Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(20.dp))
            }
            Text(text = text, fontWeight = FontWeight.Medium)
        }
    }
}

/**
 * Text button for tertiary actions
 */
@Composable
fun TextAppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = Green600
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = ButtonDefaults.textButtonColors(
            contentColor = color,
            disabledContentColor = color.copy(alpha = 0.5f)
        )
    ) {
        Text(text = text, fontWeight = FontWeight.Medium)
    }
}

