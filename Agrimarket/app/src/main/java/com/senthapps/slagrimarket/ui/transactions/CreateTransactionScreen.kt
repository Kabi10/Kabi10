package com.senthapps.slagrimarket.ui.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.PickupLocations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTransactionScreen(
    listingId: String,
    onNavigateBack: () -> Unit,
    onTransactionCreated: () -> Unit,
    viewModel: CreateTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(listingId) {
        viewModel.loadListing(listingId)
    }
    
    LaunchedEffect(uiState.isTransactionCreated) {
        if (uiState.isTransactionCreated) {
            onTransactionCreated()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "ஆர்டர் செய்யுங்கள்",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Place Order",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Listing summary
                uiState.listing?.let { listing ->
                    ListingSummaryCard(
                        cropType = listing.cropType,
                        pricePerUnit = listing.pricePerUnit,
                        unit = listing.unit,
                        availableQuantity = listing.quantity,
                        location = listing.location
                    )
                }
                
                // Order form
                OrderForm(
                    quantity = uiState.quantity,
                    onQuantityChange = viewModel::updateQuantity,
                    pickupLocation = uiState.pickupLocation,
                    onPickupLocationChange = viewModel::updatePickupLocation,
                    pickupDate = uiState.pickupDate,
                    onPickupDateChange = viewModel::updatePickupDate,
                    buyerContact = uiState.buyerContact,
                    onBuyerContactChange = viewModel::updateBuyerContact,
                    notes = uiState.notes,
                    onNotesChange = viewModel::updateNotes,
                    totalAmount = uiState.totalAmount,
                    isValid = uiState.isFormValid,
                    onCreateTransaction = viewModel::createTransaction,
                    isCreating = uiState.isCreating
                )
                
                // Error display
                uiState.error?.let { error ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListingSummaryCard(
    cropType: String,
    pricePerUnit: Double,
    unit: String,
    availableQuantity: Double,
    location: String
) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "பொருள் விவரங்கள் / Item Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = cropType.replace("_", " ").capitalize(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = location,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "LKR $pricePerUnit",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "per $unit",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Available: $availableQuantity $unit",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderForm(
    quantity: String,
    onQuantityChange: (String) -> Unit,
    pickupLocation: String,
    onPickupLocationChange: (String) -> Unit,
    pickupDate: String,
    onPickupDateChange: (String) -> Unit,
    buyerContact: String,
    onBuyerContactChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    totalAmount: Double,
    isValid: Boolean,
    onCreateTransaction: () -> Unit,
    isCreating: Boolean
) {
    var showLocationDropdown by remember { mutableStateOf(false) }
    
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "ஆர்டர் விவரங்கள் / Order Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            // Quantity
            OutlinedTextField(
                value = quantity,
                onValueChange = onQuantityChange,
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Pickup Location
            ExposedDropdownMenuBox(
                expanded = showLocationDropdown,
                onExpandedChange = { showLocationDropdown = it }
            ) {
                OutlinedTextField(
                    value = pickupLocation,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Pickup Location") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showLocationDropdown) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                
                ExposedDropdownMenu(
                    expanded = showLocationDropdown,
                    onDismissRequest = { showLocationDropdown = false }
                ) {
                    PickupLocations.ALL_LOCATIONS.forEach { location ->
                        DropdownMenuItem(
                            text = { Text(location) },
                            onClick = {
                                onPickupLocationChange(location)
                                showLocationDropdown = false
                            }
                        )
                    }
                }
            }
            
            // Pickup Date
            OutlinedTextField(
                value = pickupDate,
                onValueChange = onPickupDateChange,
                label = { Text("Pickup Date (YYYY-MM-DD)") },
                trailingIcon = {
                    Icon(Icons.Default.DateRange, contentDescription = "Date")
                },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Buyer Contact
            OutlinedTextField(
                value = buyerContact,
                onValueChange = onBuyerContactChange,
                label = { Text("Your Contact Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = onNotesChange,
                label = { Text("Additional Notes (Optional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Total amount
            if (totalAmount > 0) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "மொத்த தொகை / Total Amount",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "LKR $totalAmount",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Create transaction button
            Button(
                onClick = onCreateTransaction,
                enabled = isValid && !isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(if (isCreating) "Creating Order..." else "Place Order")
            }
        }
    }
}
