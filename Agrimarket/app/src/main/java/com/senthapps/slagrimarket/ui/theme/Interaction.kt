package com.senthapps.slagrimarket.ui.theme

import androidx.compose.foundation.IndicationNodeFactory
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DelegatableNode
import androidx.compose.ui.node.DrawModifierNode
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// ============================================================================
// HUMAN INDUSTRIAL INTERACTION SYSTEM v1.0
// "Heavy, immediate, no animation"
// Pressed state shows Earth color at 15% opacity overlay
// No ripple, no animation, instant feedback
// ============================================================================

/**
 * Industrial pressed overlay color - Earth at 15% opacity
 * This is the exact color specified in the UI plan
 */
val IndustrialPressedOverlay = Color(0x268B4513)

/**
 * Industrial Indication - shows Earth overlay on press
 * Per UI plan spec: "Earth #8B4513 at 15% opacity overlay on quadrant"
 * "Immediate on touch down, clears on release"
 * "No animation"
 */
object IndustrialIndication : IndicationNodeFactory {
    override fun create(interactionSource: InteractionSource): DelegatableNode {
        return IndustrialIndicationNode(interactionSource)
    }

    override fun hashCode(): Int = -1

    override fun equals(other: Any?) = other === this
}

private class IndustrialIndicationNode(
    private val interactionSource: InteractionSource
) : Modifier.Node(), DrawModifierNode {

    private var isPressed = false

    override fun onAttach() {
        coroutineScope.launch {
            interactionSource.interactions.collectLatest { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> isPressed = true
                    is PressInteraction.Release -> isPressed = false
                    is PressInteraction.Cancel -> isPressed = false
                }
            }
        }
    }

    override fun ContentDrawScope.draw() {
        drawContent()
        if (isPressed) {
            drawRect(
                color = IndustrialPressedOverlay,
                size = size
            )
        }
    }
}

/**
 * Modifier extension for industrial clickable behavior
 * Uses IndustrialIndication for pressed state feedback
 */
fun Modifier.industrialClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = interactionSource,
        indication = IndustrialIndication,
        enabled = enabled,
        onClick = onClick
    )
}

/**
 * Modifier extension for industrial clickable with custom interaction source
 */
@Composable
fun Modifier.industrialClickable(
    interactionSource: MutableInteractionSource,
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier {
    return this.clickable(
        interactionSource = interactionSource,
        indication = IndustrialIndication,
        enabled = enabled,
        onClick = onClick
    )
}
