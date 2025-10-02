package com.senthapps.slagrimarket.ui.listings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    onNavigateBack: () -> Unit,
    onListingCreated: () -> Unit,
    viewModel: CreateListingViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val focusManager = LocalFocusManager.current

    var showCropTypeDropdown by remember { mutableStateOf(false) }
    var showUnitDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onListingCreated()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Create Listing"
                            "ta" -> "பட்டியல் உருவாக்கவும்"
                            "si" -> "ලැයිස්තුව සාදන්න"
                            else -> "Create Listing"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = when (currentLanguage) {
                                "en" -> "Back"
                                "ta" -> "பின்செல்"
                                "si" -> "ආපසු"
                                else -> "Back"
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Crop Type
            Column {
                ExposedDropdownMenuBox(
                    expanded = showCropTypeDropdown,
                    onExpandedChange = { showCropTypeDropdown = it }
                ) {
                    OutlinedTextField(
                        value = if (uiState.cropType.isNotEmpty()) {
                            CropTypes.getCropName(uiState.cropType, currentLanguage)
                        } else "",
                        onValueChange = { },
                        label = {
                            Text(when (currentLanguage) {
                                "en" -> "Crop Type *"
                                "ta" -> "பயிர் வகை *"
                                "si" -> "බෝග වර්ගය *"
                                else -> "Crop Type *"
                            })
                        },
                        readOnly = true,
                        isError = uiState.cropTypeError != null,
                        trailingIcon = {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = showCropTypeDropdown,
                        onDismissRequest = { showCropTypeDropdown = false }
                    ) {
                        viewModel.getAvailableCropTypes().forEach { cropType ->
                            DropdownMenuItem(
                                text = { Text(CropTypes.getCropName(cropType, currentLanguage)) },
                                onClick = {
                                    viewModel.updateCropType(cropType)
                                    showCropTypeDropdown = false
                                }
                            )
                        }
                    }
                }

                // Error message
                uiState.cropTypeError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            // Quantity and Unit
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        OutlinedTextField(
                            value = uiState.quantity,
                            onValueChange = viewModel::updateQuantity,
                            label = {
                                Text(when (currentLanguage) {
                                    "en" -> "Quantity *"
                                    "ta" -> "அளவு *"
                                    "si" -> "ප්‍රමාණය *"
                                    else -> "Quantity *"
                                })
                            },
                            isError = uiState.quantityError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Next) }
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        ExposedDropdownMenuBox(
                            expanded = showUnitDropdown,
                            onExpandedChange = { showUnitDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = uiState.unit,
                                onValueChange = { },
                                label = {
                                    Text(when (currentLanguage) {
                                        "en" -> "Unit *"
                                        "ta" -> "அலகு *"
                                        "si" -> "ඒකකය *"
                                        else -> "Unit *"
                                    })
                                },
                                readOnly = true,
                                isError = uiState.unitError != null,
                                trailingIcon = {
                                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = showUnitDropdown,
                                onDismissRequest = { showUnitDropdown = false }
                            ) {
                                viewModel.getAvailableUnits().forEach { unit ->
                                    DropdownMenuItem(
                                        text = { Text(unit) },
                                        onClick = {
                                            viewModel.updateUnit(unit)
                                            showUnitDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Error messages
                uiState.quantityError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
                uiState.unitError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            // Price per unit
            Column {
                OutlinedTextField(
                    value = uiState.pricePerUnit,
                    onValueChange = viewModel::updatePricePerUnit,
                    label = {
                        Text(when (currentLanguage) {
                            "en" -> "Price per unit (LKR) *"
                            "ta" -> "அலகுக்கான விலை (LKR) *"
                            "si" -> "ඒකක මිල (LKR) *"
                            else -> "Price per unit (LKR) *"
                        })
                    },
                    isError = uiState.priceError != null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Next) }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                uiState.priceError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            // Quality Grade - Toggle Buttons
            Column {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Quality Grade *"
                        "ta" -> "தர நிலை *"
                        "si" -> "ගුණාත්මක ශ්‍රේණිය *"
                        else -> "Quality Grade *"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (uiState.qualityError != null) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    viewModel.getAvailableQualityGrades().forEach { grade ->
                        FilterChip(
                            selected = uiState.quality == grade,
                            onClick = { viewModel.updateQuality(grade) },
                            label = {
                                Text(
                                    text = when (currentLanguage) {
                                        "en" -> "Grade $grade"
                                        "ta" -> "தரம் $grade"
                                        "si" -> "ශ්‍රේණිය $grade"
                                        else -> "Grade $grade"
                                    },
                                    fontWeight = if (uiState.quality == grade) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.weight(1f),
                            border = if (uiState.qualityError != null && uiState.quality != grade) {
                                BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                            } else null
                        )
                    }
                }

                // Error message
                uiState.qualityError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            // Harvest Date with Date Picker
            Column {
                OutlinedTextField(
                    value = uiState.harvestDate,
                    onValueChange = { },
                    label = {
                        Text(when (currentLanguage) {
                            "en" -> "Harvest Date *"
                            "ta" -> "அறுவடை தேதி *"
                            "si" -> "අස්වනු නෙලන දිනය *"
                            else -> "Harvest Date *"
                        })
                    },
                    readOnly = true,
                    isError = uiState.harvestDateError != null,
                    placeholder = { Text(viewModel.getTodayDate()) },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = when (currentLanguage) {
                                    "en" -> "Select date"
                                    "ta" -> "தேதியைத் தேர்ந்தெடுக்கவும்"
                                    "si" -> "දිනය තෝරන්න"
                                    else -> "Select date"
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                uiState.harvestDateError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }

            // Date Picker Dialog
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = System.currentTimeMillis()
                )

                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                                    val today = LocalDate.now()

                                    // Validate date is not in future
                                    if (date.isAfter(today)) {
                                        viewModel.setHarvestDateError(
                                            when (currentLanguage) {
                                                "en" -> "Harvest date cannot be in the future"
                                                "ta" -> "அறுவடை தேதி எதிர்காலத்தில் இருக்க முடியாது"
                                                "si" -> "අස්වනු නෙලන දිනය අනාගතයේ විය නොහැක"
                                                else -> "Harvest date cannot be in the future"
                                            }
                                        )
                                    } else {
                                        viewModel.updateHarvestDate(date.format(DateTimeFormatter.ISO_LOCAL_DATE))
                                        showDatePicker = false
                                    }
                                }
                            }
                        ) {
                            Text(when (currentLanguage) {
                                "en" -> "OK"
                                "ta" -> "சரி"
                                "si" -> "හරි"
                                else -> "OK"
                            })
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(when (currentLanguage) {
                                "en" -> "Cancel"
                                "ta" -> "ரத்துசெய்"
                                "si" -> "අවලංගු කරන්න"
                                else -> "Cancel"
                            })
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
            
            // Location
            Column {
                OutlinedTextField(
                    value = uiState.location,
                    onValueChange = viewModel::updateLocation,
                    label = {
                        Text(when (currentLanguage) {
                            "en" -> "Location *"
                            "ta" -> "இடம் *"
                            "si" -> "ස්ථානය *"
                            else -> "Location *"
                        })
                    },
                    placeholder = { Text("e.g., Chavakachcheri, Jaffna") },
                    isError = uiState.locationError != null,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (viewModel.isFormValid()) {
                                viewModel.createListing()
                            }
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Error message
                uiState.locationError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
            
            // Error message
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            
            // Create button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    viewModel.clearError()
                    viewModel.createListing()
                },
                enabled = !uiState.isLoading && viewModel.isFormValid(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Create Listing"
                            "ta" -> "பட்டியல் உருவாக்கவும்"
                            "si" -> "ලැයිස්තුව සාදන්න"
                            else -> "Create Listing"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
