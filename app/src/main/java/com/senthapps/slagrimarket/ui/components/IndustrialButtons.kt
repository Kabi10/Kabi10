package com.senthapps.slagrimarket.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.TouchTargets

// ============================================================================
// HUMAN INDUSTRIAL BUTTON COMPONENTS v1.0
// "The Sturdy Work Buttons" - Bold, tactile, no decoration
// Gold for primary action, Earth borders for secondary
// ============================================================================

/**
 * Primary action button - Gold background, Rice text
 * Used for main CTAs like "CALL SELLER", "SUBMIT LISTING"
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(TouchTargets.button.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = HumanIndustrial.Gold,
            contentColor = HumanIndustrial.Rice,
            disabledContainerColor = HumanIndustrial.Gold.copy(alpha = 0.4f),
            disabledContentColor = HumanIndustrial.Rice.copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Text(
            text = text.uppercase(),
            style = HumanIndustrialType.button,
            color = HumanIndustrial.Rice
        )
    }
}

/**
 * Secondary button - Rice background, Earth border and text
 * Used for secondary actions like "EDIT", "CANCEL"
 */
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(TouchTargets.button.dp),
        shape = RectangleShape,
        border = BorderStroke(BorderWidth.Standard, HumanIndustrial.Earth),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = HumanIndustrial.Rice,
            contentColor = HumanIndustrial.Earth,
            disabledContainerColor = HumanIndustrial.Rice,
            disabledContentColor = HumanIndustrial.Earth.copy(alpha = 0.4f)
        ),
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Text(
            text = text.uppercase(),
            style = HumanIndustrialType.button,
            color = HumanIndustrial.Earth
        )
    }
}

/**
 * Danger button - Urgent (brick red) background, Rice text
 * Used for destructive actions like "DELETE"
 */
@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(TouchTargets.button.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = HumanIndustrial.Urgent,
            contentColor = HumanIndustrial.Rice,
            disabledContainerColor = HumanIndustrial.Urgent.copy(alpha = 0.4f),
            disabledContentColor = HumanIndustrial.Rice.copy(alpha = 0.4f)
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            focusedElevation = 0.dp,
            hoveredElevation = 0.dp,
            disabledElevation = 0.dp
        ),
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Text(
            text = text.uppercase(),
            style = HumanIndustrialType.button,
            color = HumanIndustrial.Rice
        )
    }
}

/**
 * Unified industrial button with primary/secondary toggle
 * Convenience wrapper for PrimaryButton and SecondaryButton
 *
 * @param text Button text (will be uppercased)
 * @param onClick Click handler
 * @param modifier Modifier for the button
 * @param enabled Whether button is enabled
 * @param isPrimary True for orange primary button, false for outlined secondary
 */
@Composable
fun IndustrialButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true
) {
    if (isPrimary) {
        PrimaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        )
    } else {
        SecondaryButton(
            text = text,
            onClick = onClick,
            modifier = modifier,
            enabled = enabled
        )
    }
}
