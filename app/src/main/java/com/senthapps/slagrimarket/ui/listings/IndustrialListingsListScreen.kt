package com.senthapps.slagrimarket.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketGray
import com.senthapps.slagrimarket.ui.theme.AgrimarketOffWhite
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.Spacing

// ============================================================================
// INDUSTRIAL LISTINGS LIST SCREEN
// Category → List of listings → Detail
// ============================================================================

/**
 * Data class representing a listing in the list view
 */
data class ListingPreview(
    val id: String,
    val productName: String,
    val price: Double,
    val unit: String,
    val location: String
)

/**
 * Industrial listings list screen - browse products in a category
 *
 * @param categoryName Category name to display in header
 * @param listings List of listings to display
 * @param onListingClick Callback when user taps a listing
 * @param onNavigateBack Callback to navigate back
 */
@Composable
fun IndustrialListingsListScreen(
    categoryName: String,
    listings: List<ListingPreview>,
    onListingClick: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AgrimarketWhite,
        topBar = {
            // Title bar with category name and back button
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

                Text(
                    text = categoryName.uppercase(),
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = AgrimarketBlack,
                        letterSpacing = 0.sp
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Listing rows
            itemsIndexed(listings) { index, listing ->
                ListingRow(
                    listing = listing,
                    useAlternateBackground = index % 2 == 1,
                    onClick = { onListingClick(listing.id) }
                )
            }
        }
    }
}

/**
 * Single row in the listings list
 * Product name + location on left, price on right
 */
@Composable
private fun ListingRow(
    listing: ListingPreview,
    useAlternateBackground: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(if (useAlternateBackground) AgrimarketOffWhite else AgrimarketWhite)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = Spacing.Base),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Product name + location
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = listing.productName.uppercase(),
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                )
            )
            Text(
                text = listing.location.uppercase(),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AgrimarketGray,
                    letterSpacing = 0.sp
                )
            )
        }

        // Right side: Price + unit
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = listing.price.toString(),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                )
            )
            Text(
                text = listing.unit.uppercase(),
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
