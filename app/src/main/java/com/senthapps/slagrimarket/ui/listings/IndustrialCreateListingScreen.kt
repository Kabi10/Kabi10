package com.senthapps.slagrimarket.ui.listings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.senthapps.slagrimarket.ui.components.IndustrialFormDropdown
import com.senthapps.slagrimarket.ui.components.IndustrialFormField
import com.senthapps.slagrimarket.ui.components.PrimaryButton
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.Spacing

// ============================================================================
// INDUSTRIAL CREATE LISTING SCREEN
// Single-scroll form. Fill top to bottom. Submit. Done.
// ============================================================================

/**
 * Industrial create listing screen - single form with vertical scroll
 *
 * NOTE: This is a UI-only demonstration component.
 * In production, wire this to a ViewModel with proper state management.
 */
@Composable
fun IndustrialCreateListingScreen(
    onSubmit: (
        productName: String,
        category: String,
        quantity: String,
        unit: String,
        price: String,
        location: String
    ) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current

    // Form state (in production, move to ViewModel)
    var productName by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("KG") }
    var price by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // Validation errors
    var productNameError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var quantityError by remember { mutableStateOf<String?>(null) }
    var priceError by remember { mutableStateOf<String?>(null) }
    var locationError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = AgrimarketWhite,
        topBar = {
            // Title bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(AgrimarketWhite)
                    .border(
                        width = BorderWidth.Thin,
                        color = AgrimarketBlack,
                        shape = RectangleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "CREATE LISTING",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = AgrimarketBlack,
                        letterSpacing = 0.sp
                    )
                )
            }
        },
        bottomBar = {
            // Submit button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AgrimarketWhite)
                    .padding(Spacing.Base)
            ) {
                PrimaryButton(
                    text = "SUBMIT LISTING",
                    onClick = {
                        // Simple validation
                        var isValid = true

                        if (productName.isBlank()) {
                            productNameError = "REQUIRED"
                            isValid = false
                        } else {
                            productNameError = null
                        }

                        if (category.isBlank()) {
                            categoryError = "REQUIRED"
                            isValid = false
                        } else {
                            categoryError = null
                        }

                        if (quantity.isBlank()) {
                            quantityError = "REQUIRED"
                            isValid = false
                        } else {
                            quantityError = null
                        }

                        if (price.isBlank()) {
                            priceError = "REQUIRED"
                            isValid = false
                        } else {
                            priceError = null
                        }

                        if (location.isBlank()) {
                            locationError = "REQUIRED"
                            isValid = false
                        } else {
                            locationError = null
                        }

                        if (isValid) {
                            onSubmit(productName, category, quantity, selectedUnit, price, location)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = Spacing.Base, vertical = Spacing.Base),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Product Name
            item {
                IndustrialFormField(
                    label = "PRODUCT NAME",
                    value = productName,
                    onValueChange = { productName = it },
                    placeholder = "TOMATOES, CARROT, ETC.",
                    errorMessage = productNameError
                )
            }

            // Category
            item {
                IndustrialFormDropdown(
                    label = "CATEGORY",
                    options = listOf("VEGETABLES", "FRUITS", "GRAINS", "LIVESTOCK"),
                    selectedOption = category,
                    onOptionSelected = { category = it },
                    placeholder = "SELECT CATEGORY"
                )
                categoryError?.let { error ->
                    Text(
                        text = error,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = com.senthapps.slagrimarket.ui.theme.AgrimarketRed,
                            letterSpacing = 0.sp
                        ),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Quantity + Unit
            item {
                Column {
                    Text(
                        text = "QUANTITY",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = AgrimarketBlack,
                            letterSpacing = 0.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.Base)) {
                        IndustrialFormField(
                            label = "",
                            value = quantity,
                            onValueChange = { quantity = it },
                            placeholder = "50",
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            errorMessage = quantityError
                        )
                        IndustrialFormDropdown(
                            label = "",
                            options = listOf("KG", "G", "LITERS", "PIECES"),
                            selectedOption = selectedUnit,
                            onOptionSelected = { selectedUnit = it },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Price
            item {
                IndustrialFormField(
                    label = "PRICE",
                    value = price,
                    onValueChange = { price = it },
                    placeholder = "150",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = "PER $selectedUnit",
                    errorMessage = priceError
                )
            }

            // Location
            item {
                IndustrialFormField(
                    label = "LOCATION",
                    value = location,
                    onValueChange = { location = it },
                    placeholder = "ANURADHAPURA",
                    errorMessage = locationError
                )
            }

            // Photo (optional) - placeholder for future implementation
            item {
                Column {
                    Text(
                        text = "PHOTO (OPTIONAL)",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = AgrimarketBlack,
                            letterSpacing = 0.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    SecondaryButton(
                        text = "ADD PHOTO",
                        onClick = {
                            // Image upload functionality coming in a future update
                            Toast.makeText(context, "Image upload coming soon", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
