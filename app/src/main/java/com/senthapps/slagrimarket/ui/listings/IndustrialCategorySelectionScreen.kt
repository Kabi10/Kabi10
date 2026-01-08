package com.senthapps.slagrimarket.ui.listings

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
// INDUSTRIAL CATEGORY SELECTION SCREEN
// 2×2 grid identical to home screen, but for product categories
// ============================================================================

/**
 * Category selection screen for buyer browsing flow
 * Identical structure to home screen - 2×2 grid with category names
 *
 * @param onCategorySelected Callback when user selects a category
 */
@Composable
fun IndustrialCategorySelectionScreen(
    onCategorySelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AgrimarketWhite)
    ) {
        // Top row: VEGETABLES | FRUITS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CategoryTile(
                text = "VEGETABLES",
                onClick = { onCategorySelected("VEGETABLES") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CategoryTile(
                text = "FRUITS",
                onClick = { onCategorySelected("FRUITS") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        // Bottom row: GRAINS | LIVESTOCK
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            CategoryTile(
                text = "GRAINS",
                onClick = { onCategorySelected("GRAINS") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            CategoryTile(
                text = "LIVESTOCK",
                onClick = { onCategorySelected("LIVESTOCK") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

/**
 * Individual category tile - identical to home tile
 */
@Composable
private fun CategoryTile(
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
