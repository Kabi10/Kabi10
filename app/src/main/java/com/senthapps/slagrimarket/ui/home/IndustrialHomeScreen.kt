package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth

// ============================================================================
// INDUSTRIAL HOME SCREEN
// 2×2 grid of large text-only tiles. No header, no decorations.
// ============================================================================

/**
 * Industrial home screen - brutal 2×2 grid
 * Four equal-weight tiles: SELL, BUY, PRICES, ORDERS
 *
 * @param onNavigateToSell Navigate to create listing flow
 * @param onNavigateToBuy Navigate to buyer browsing flow (categories)
 * @param onNavigateToPrices Navigate to market prices list
 * @param onNavigateToOrders Navigate to user's orders/transactions
 */
@Composable
fun IndustrialHomeScreen(
    onNavigateToSell: () -> Unit,
    onNavigateToBuy: () -> Unit,
    onNavigateToPrices: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AgrimarketWhite)
    ) {
        // Top row: SELL | BUY
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HomeTile(
                text = "SELL",
                onClick = onNavigateToSell,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            HomeTile(
                text = "BUY",
                onClick = onNavigateToBuy,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Bottom row: PRICES | ORDERS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            HomeTile(
                text = "PRICES",
                onClick = onNavigateToPrices,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            HomeTile(
                text = "ORDERS",
                onClick = onNavigateToOrders,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Individual tile in the 2×2 grid
 * Hard-edged, centered text, 3dp black border
 */
@Composable
private fun HomeTile(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = BorderWidth.Thick,
                color = AgrimarketBlack,
                shape = RectangleShape
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(AgrimarketWhite),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = AgrimarketBlack,
                letterSpacing = 0.sp
            )
        )
    }
}
