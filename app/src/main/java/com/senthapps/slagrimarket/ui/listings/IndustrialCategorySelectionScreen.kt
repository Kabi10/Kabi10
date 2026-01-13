package com.senthapps.slagrimarket.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextAlign
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType

// ============================================================================
// HUMAN INDUSTRIAL CATEGORY SELECTION SCREEN v1.0
// "The Market Sections" - Color-coded categories for instant recognition
// Each category carries the color of its contents
// ============================================================================

/**
 * Category selection screen for buyer browsing flow
 * 2×2 grid with color-coded category tiles
 *
 * - VEGETABLES: Green background, Rice text
 * - FRUITS: Gold background, Ink text
 * - GRAINS: Lighter gold background, Ink text
 * - LIVESTOCK: Earth background, Rice text
 */
@Composable
fun IndustrialCategorySelectionScreen(
    onCategorySelected: (String) -> Unit
) {
    // Outer container: Rice shows as grid divider color
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = BorderWidth.Thick,
                    color = HumanIndustrial.Rice,
                    shape = RectangleShape
                )
        ) {
            // Top row: VEGETABLES | FRUITS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                CategoryTile(
                    text = "VEGETABLES",
                    backgroundColor = HumanIndustrial.VegetablesBackground,
                    textColor = HumanIndustrial.Rice,
                    onClick = { onCategorySelected("VEGETABLES") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                CategoryTile(
                    text = "FRUITS",
                    backgroundColor = HumanIndustrial.FruitsBackground,
                    textColor = HumanIndustrial.Ink,
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
                    backgroundColor = HumanIndustrial.GrainsBackground,
                    textColor = HumanIndustrial.Ink,
                    onClick = { onCategorySelected("GRAINS") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
                CategoryTile(
                    text = "LIVESTOCK",
                    backgroundColor = HumanIndustrial.LivestockBackground,
                    textColor = HumanIndustrial.Rice,
                    onClick = { onCategorySelected("LIVESTOCK") },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

/**
 * Category tile with colored background
 * 22sp Bold UPPERCASE centered text
 * 4dp border matching background color (solid block effect)
 */
@Composable
private fun CategoryTile(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .border(
                width = BorderWidth.Thick,
                color = backgroundColor,
                shape = RectangleShape
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = HumanIndustrialType.categoryTile,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}
