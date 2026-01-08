package com.senthapps.slagrimarket.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.data.model.MarketPrice
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketGray
import com.senthapps.slagrimarket.ui.theme.AgrimarketOffWhite
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.Spacing

// ============================================================================
// INDUSTRIAL MARKET PRICES SCREEN
// Full-screen list with prices. Scan down, find product, done.
// ============================================================================

/**
 * Industrial market prices screen - simple list view
 *
 * @param marketPrices List of market prices to display
 * @param onNavigateBack Callback to navigate back
 * @param lastUpdatedText Text showing last update time (e.g., "UPDATED: 2 HOURS AGO")
 */
@Composable
fun IndustrialMarketPricesScreen(
    marketPrices: List<MarketPrice>,
    onNavigateBack: () -> Unit,
    lastUpdatedText: String = "UPDATED: JUST NOW"
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AgrimarketWhite,
        topBar = {
            // Title bar with back button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(AgrimarketWhite)
                    .border(
                        width = BorderWidth.Thin,
                        color = AgrimarketBlack,
                        shape = RectangleShape
                    )
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AgrimarketBlack
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AgrimarketWhite)
                        .padding(vertical = Spacing.Base),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "MARKET PRICES",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = AgrimarketBlack,
                            letterSpacing = 0.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = lastUpdatedText.uppercase(),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = AgrimarketGray,
                            letterSpacing = 0.sp
                        )
                    )
                }
            }

            // Price list rows
            itemsIndexed(marketPrices) { index, price ->
                MarketPriceRow(
                    marketPrice = price,
                    useAlternateBackground = index % 2 == 1
                )
            }
        }
    }
}

/**
 * Single row in the market prices list
 * Product name on left, price on right, alternating backgrounds
 */
@Composable
private fun MarketPriceRow(
    marketPrice: MarketPrice,
    useAlternateBackground: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(if (useAlternateBackground) AgrimarketOffWhite else AgrimarketWhite)
            .padding(horizontal = Spacing.Base),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Product name (left)
        Text(
            text = marketPrice.cropNameEnglish.uppercase(),
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AgrimarketBlack,
                letterSpacing = 0.sp
            )
        )

        // Price and unit (right)
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = marketPrice.currentPrice.toInt().toString(),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                )
            )
            Text(
                text = marketPrice.unit.uppercase(),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AgrimarketGray,
                    letterSpacing = 0.sp
                )
            )
        }
    }
}
