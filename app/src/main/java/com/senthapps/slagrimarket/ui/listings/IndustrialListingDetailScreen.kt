package com.senthapps.slagrimarket.ui.listings

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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.senthapps.slagrimarket.ui.components.DangerButton
import com.senthapps.slagrimarket.ui.components.IndustrialConfirmationBottomSheet
import com.senthapps.slagrimarket.ui.components.PrimaryButton
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketGray
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.Spacing
import kotlinx.coroutines.launch

// ============================================================================
// INDUSTRIAL LISTING DETAIL SCREEN
// Image → Price (huge) → Metadata → Action buttons
// ============================================================================

/**
 * Industrial listing detail screen
 *
 * @param productName Product name (e.g., "Fresh Tomatoes")
 * @param price Price amount
 * @param unit Unit of measurement (e.g., "KG")
 * @param quantity Available quantity
 * @param location Seller location
 * @param sellerName Seller's name
 * @param imageUrl Optional image URL
 * @param isOwnListing If true, shows EDIT/DELETE buttons; otherwise shows CALL SELLER
 * @param onNavigateBack Callback to navigate back
 * @param onCallSeller Callback when user taps CALL SELLER
 * @param onEdit Callback when user taps EDIT
 * @param onDelete Callback when user confirms deletion
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndustrialListingDetailScreen(
    productName: String,
    price: Double,
    unit: String,
    quantity: Double,
    location: String,
    sellerName: String,
    imageUrl: String? = null,
    isOwnListing: Boolean = false,
    onNavigateBack: () -> Unit,
    onCallSeller: () -> Unit = {},
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AgrimarketWhite,
        topBar = {
            // Top bar with back button
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
        },
        bottomBar = {
            // Action buttons
            if (isOwnListing) {
                // EDIT and DELETE buttons for own listing
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AgrimarketWhite)
                        .padding(Spacing.Base),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Base)
                ) {
                    SecondaryButton(
                        text = "EDIT",
                        onClick = onEdit,
                        modifier = Modifier.weight(1f)
                    )
                    DangerButton(
                        text = "DELETE",
                        onClick = { showDeleteConfirmation = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                // CALL SELLER button for other listings
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AgrimarketWhite)
                        .padding(Spacing.Base)
                ) {
                    PrimaryButton(
                        text = "CALL SELLER",
                        onClick = onCallSeller,
                        modifier = Modifier.fillMaxWidth()
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
            // Image (if available)
            imageUrl?.let { url ->
                item {
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .background(AgrimarketBlack),
                        contentScale = ContentScale.FillWidth
                    )
                }
            }

            // Product title
            item {
                Text(
                    text = productName.uppercase(),
                    style = TextStyle(
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = AgrimarketBlack,
                        letterSpacing = 0.sp
                    ),
                    modifier = Modifier.padding(horizontal = Spacing.Base, vertical = Spacing.Base)
                )
            }

            // Price (huge)
            item {
                Text(
                    text = price.toString(),
                    style = TextStyle(
                        fontSize = 56.sp,
                        fontWeight = FontWeight.Black,
                        color = AgrimarketBlack,
                        lineHeight = 56.sp,
                        letterSpacing = 0.sp
                    ),
                    modifier = Modifier.padding(horizontal = Spacing.Base)
                )
            }

            // Unit
            item {
                Text(
                    text = "PER ${unit.uppercase()}",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgrimarketGray,
                        letterSpacing = 0.sp
                    ),
                    modifier = Modifier.padding(horizontal = Spacing.Base, vertical = 4.dp)
                )
            }

            // Spacer before metadata
            item {
                Spacer(modifier = Modifier.height(Spacing.Base))
            }

            // Metadata stack
            item {
                Column(
                    modifier = Modifier.padding(horizontal = Spacing.Base),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "QUANTITY: $quantity ${unit.uppercase()}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AgrimarketBlack,
                            letterSpacing = 0.sp
                        )
                    )
                    Text(
                        text = "LOCATION: ${location.uppercase()}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AgrimarketBlack,
                            letterSpacing = 0.sp
                        )
                    )
                    Text(
                        text = "SELLER: ${sellerName.uppercase()}",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = AgrimarketBlack,
                            letterSpacing = 0.sp
                        )
                    )
                }
            }

            // Bottom spacer to ensure content doesn't overlap bottom bar
            item {
                Spacer(modifier = Modifier.height(Spacing.Base))
            }
        }
    }

    // Delete confirmation bottom sheet
    if (showDeleteConfirmation) {
        IndustrialConfirmationBottomSheet(
            title = "DELETE LISTING?",
            confirmText = "DELETE",
            cancelText = "CANCEL",
            onConfirm = {
                scope.launch {
                    sheetState.hide()
                    showDeleteConfirmation = false
                    onDelete()
                }
            },
            onCancel = {
                scope.launch {
                    sheetState.hide()
                    showDeleteConfirmation = false
                }
            },
            isDangerous = true,
            sheetState = sheetState
        )
    }
}
