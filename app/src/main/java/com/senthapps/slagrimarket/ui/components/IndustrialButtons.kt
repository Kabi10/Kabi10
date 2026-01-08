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
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketOrange
import com.senthapps.slagrimarket.ui.theme.AgrimarketRed
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth

// ============================================================================
// INDUSTRIAL BUTTON COMPONENTS
// Flat, heavy, text-only buttons with no elevation or ripples
// ============================================================================

/**
 * Primary action button - Orange background, white text
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
        modifier = modifier.height(64.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AgrimarketOrange,
            contentColor = AgrimarketWhite,
            disabledContainerColor = AgrimarketOrange.copy(alpha = 0.4f),
            disabledContentColor = AgrimarketWhite.copy(alpha = 0.4f)
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
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp
            )
        )
    }
}

/**
 * Secondary button - White background, black border and text
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
        modifier = modifier.height(64.dp),
        shape = RectangleShape,
        border = BorderStroke(BorderWidth.Thick, AgrimarketBlack),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = AgrimarketWhite,
            contentColor = AgrimarketBlack,
            disabledContainerColor = AgrimarketWhite,
            disabledContentColor = AgrimarketBlack.copy(alpha = 0.4f)
        ),
        enabled = enabled,
        interactionSource = remember { MutableInteractionSource() }
    ) {
        Text(
            text = text.uppercase(),
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp
            )
        )
    }
}

/**
 * Danger button - Red background, white text
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
        modifier = modifier.height(64.dp),
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = AgrimarketRed,
            contentColor = AgrimarketWhite,
            disabledContainerColor = AgrimarketRed.copy(alpha = 0.4f),
            disabledContentColor = AgrimarketWhite.copy(alpha = 0.4f)
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
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.sp
            )
        )
    }
}
