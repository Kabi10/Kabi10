package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType

// ============================================================================
// HUMAN INDUSTRIAL HOME SCREEN v1.0
// "The Village Square" - Four clear directions, four clear purposes
// Rice background, Earth borders, bold centered text
// ============================================================================

/**
 * User mode - determines tile layout order
 */
enum class UserMode {
    BUYER,  // BUY first (top-left)
    SELLER  // SELL first (top-left)
}

/**
 * Human Industrial home screen - warm 2×2 grid
 * Four equal-weight tiles with mode-dependent order
 *
 * BUYER mode:
 *   BUY    | PRICES
 *   ORDERS | SELL
 *
 * SELLER mode:
 *   SELL   | PRICES
 *   ORDERS | BUY
 *
 * Long-press any tile to toggle mode.
 */
@Composable
fun IndustrialHomeScreen(
    onNavigateToSell: () -> Unit,
    onNavigateToBuy: () -> Unit,
    onNavigateToPrices: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    var userMode by remember { mutableStateOf(UserMode.BUYER) }

    val toggleMode: () -> Unit = {
        userMode = if (userMode == UserMode.BUYER) UserMode.SELLER else UserMode.BUYER
    }

    // Outer container: Earth color shows as outer border
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Earth)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Inner Rice content with Earth border
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = BorderWidth.Thick,
                    color = HumanIndustrial.Earth,
                    shape = RectangleShape
                )
                .background(HumanIndustrial.Rice)
        ) {
            // Top row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Top-left tile
                if (userMode == UserMode.BUYER) {
                    HomeTile(
                        text = "BUY",
                        onClick = onNavigateToBuy,
                        onLongClick = toggleMode,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else {
                    HomeTile(
                        text = "SELL",
                        onClick = onNavigateToSell,
                        onLongClick = toggleMode,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                // Top-right tile
                HomeTile(
                    text = "PRICES",
                    onClick = onNavigateToPrices,
                    onLongClick = toggleMode,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }

            // Bottom row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Bottom-left tile
                HomeTile(
                    text = "ORDERS",
                    onClick = onNavigateToOrders,
                    onLongClick = toggleMode,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )

                // Bottom-right tile
                if (userMode == UserMode.BUYER) {
                    HomeTile(
                        text = "SELL",
                        onClick = onNavigateToSell,
                        onLongClick = toggleMode,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                } else {
                    HomeTile(
                        text = "BUY",
                        onClick = onNavigateToBuy,
                        onLongClick = toggleMode,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

/**
 * Home tile: Rice background, Earth border, Ink text
 * 24sp Bold UPPERCASE centered text
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeTile(
    text: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = BorderWidth.Thick,
                color = HumanIndustrial.Earth,
                shape = RectangleShape
            )
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(HumanIndustrial.Rice),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = HumanIndustrialType.homeTile,
            color = HumanIndustrial.Ink,
            textAlign = TextAlign.Center
        )
    }
}
