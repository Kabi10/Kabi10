package com.senthapps.slagrimarket.ui.listings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.unit.dp
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.TouchTargets

// ============================================================================
// HUMAN INDUSTRIAL LISTINGS LIST SCREEN v1.0
// "The Market Stall Row" - Scan: product → price → location → next
// Earth accent bar, Rice/Dust alternating rows, Gold prices
// ============================================================================

/**
 * Data class representing a listing in the list view
 */
data class ListingPreview(
    val id: String,
    val productName: String,
    val price: Double,
    val unit: String,
    val location: String,
    val quantity: Int = 0,
    val farmerName: String = ""
)

/**
 * Industrial listings list screen - browse products in a category
 * Header: Earth background with Rice text
 * Rows: Earth accent bar, product/price layout, Rice/Dust alternating
 */
@Composable
fun IndustrialListingsListScreen(
    categoryName: String,
    listings: List<ListingPreview>,
    onListingClick: (String) -> Unit,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false,
    isError: Boolean = false,
    onRetry: () -> Unit = {}
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = HumanIndustrial.Rice,
        topBar = {
            // Header: Earth background, Rice text
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(TouchTargets.button.dp)
                    .background(HumanIndustrial.Earth)
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = HumanIndustrial.Rice
                    )
                }

                Text(
                    text = categoryName.uppercase(),
                    style = HumanIndustrialType.screenTitle,
                    color = HumanIndustrial.Rice,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...",
                        style = HumanIndustrialType.emptyState,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            isError -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp),
                        modifier = Modifier.padding(Spacing.md.dp)
                    ) {
                        Text(
                            text = "Could not connect",
                            style = HumanIndustrialType.emptyState,
                            color = HumanIndustrial.Stone
                        )
                        androidx.compose.material3.Button(
                            onClick = onRetry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(TouchTargets.button.dp)
                                .border(
                                    width = BorderWidth.Standard,
                                    color = HumanIndustrial.Earth,
                                    shape = RectangleShape
                                ),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                containerColor = HumanIndustrial.Rice,
                                contentColor = HumanIndustrial.Earth
                            ),
                            shape = RectangleShape
                        ) {
                            Text(
                                text = "TRY AGAIN",
                                style = HumanIndustrialType.button,
                                color = HumanIndustrial.Earth
                            )
                        }
                    }
                }
            }
            listings.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No ${categoryName.lowercase()} today",
                        style = HumanIndustrialType.emptyState,
                        color = HumanIndustrial.Stone
                    )
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
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
    }
}

/**
 * Listing row per Human Industrial spec:
 * - 4dp Earth accent bar on left
 * - Product name: 20sp Bold UPPERCASE Ink
 * - Farmer · Location: 16sp Regular Stone
 * - Price: 24sp Bold Gold, right-aligned
 * - Unit: 14sp Regular Stone
 * - Quantity: 14sp Bold UPPERCASE Stone
 * - Rice/Dust alternating backgrounds
 */
@Composable
private fun ListingRow(
    listing: ListingPreview,
    useAlternateBackground: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (useAlternateBackground) HumanIndustrial.Dust else HumanIndustrial.Rice

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .background(backgroundColor)
    ) {
        // Earth accent bar (4dp)
        Box(
            modifier = Modifier
                .width(BorderWidth.Accent)
                .height(88.dp)
                .background(HumanIndustrial.Earth)
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md.dp, vertical = Spacing.md.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left: Product name, farmer · location, quantity
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
            ) {
                Text(
                    text = listing.productName.uppercase(),
                    style = HumanIndustrialType.productName,
                    color = HumanIndustrial.Ink
                )
                Text(
                    text = if (listing.farmerName.isNotEmpty()) {
                        "${listing.farmerName} · ${listing.location}"
                    } else {
                        listing.location
                    },
                    style = HumanIndustrialType.body,
                    color = HumanIndustrial.Stone
                )
                if (listing.quantity > 0) {
                    Spacer(modifier = Modifier.height(Spacing.xs.dp))
                    Text(
                        text = "${listing.quantity} ${listing.unit.uppercase()} AVAILABLE",
                        style = HumanIndustrialType.quantity,
                        color = HumanIndustrial.Stone
                    )
                }
            }

            // Right: Price + unit
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(Spacing.xs.dp)
            ) {
                Text(
                    text = listing.price.toInt().toString(),
                    style = HumanIndustrialType.priceSmall,
                    color = HumanIndustrial.Gold
                )
                Text(
                    text = "per ${listing.unit.lowercase()}",
                    style = HumanIndustrialType.unit,
                    color = HumanIndustrial.Stone
                )
            }
        }
    }
}
