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
import com.senthapps.slagrimarket.ui.components.VoiceTextField
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
                        VoiceTextField(
                            value = uiState.quantity,
                            onValueChange = { input ->
                                // Convert Tamil/Sinhala numbers to digits
                                val numericValue = convertVoiceToNumber(input)
                                viewModel.updateQuantity(numericValue)
                            },
                            label = when (currentLanguage) {
                                "en" -> "Quantity *"
                                "ta" -> "அளவு *"
                                "si" -> "ප்‍රமාណය *"
                                else -> "Quantity *"
                            },
                            language = currentLanguage,
                            isError = uiState.quantityError != null,
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
                VoiceTextField(
                    value = uiState.location,
                    onValueChange = viewModel::updateLocation,
                    label = when (currentLanguage) {
                        "en" -> "Location *"
                        "ta" -> "இடம் *"
                        "si" -> "ස්ථානය *"
                        else -> "Location *"
                    },
                    language = currentLanguage,
                    isError = uiState.locationError != null,
                    supportingText = if (uiState.locationError != null) {
                        { Text(uiState.locationError!!, color = MaterialTheme.colorScheme.error) }
                    } else null,
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

            // Story / Description (Voice input CRITICAL for elderly farmers)
            Column {
                VoiceTextField(
                    value = uiState.story,
                    onValueChange = viewModel::updateStory,
                    label = when (currentLanguage) {
                        "en" -> "Story / Description (Tap 🎤 to speak)"
                        "ta" -> "கதை / விளக்கம் (🎤 அழுத்தி பேசவும்)"
                        "si" -> "කතාව / විස්තරය (🎤 තට්ටු කර කතා කරන්න)"
                        else -> "Story / Description (Tap 🎤 to speak)"
                    },
                    language = currentLanguage,
                    modifier = Modifier.fillMaxWidth()
                )

                // Helpful hint for elderly farmers
                Text(
                    text = when (currentLanguage) {
                        "en" -> "💡 Tip: Tap the microphone and tell your story in your own words"
                        "ta" -> "💡 குறிப்பு: மைக்கைத் தட்டி உங்கள் சொந்த வார்த்தைகளில் கதையைச் சொல்லுங்கள்"
                        "si" -> "💡 ඉඟිය: මයික්‍රෆෝනය තට්ටු කර ඔබේම වචන වලින් කතාව කියන්න"
                        else -> "💡 Tip: Tap the microphone and tell your story in your own words"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Farming Methods
            Column {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Farming Methods"
                        "ta" -> "விவசாய முறைகள்"
                        "si" -> "ගොවිතැන් ක්‍රම"
                        else -> "Farming Methods"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.getAvailableFarmingMethods().forEach { method ->
                        FilterChip(
                            selected = uiState.farmingMethods.contains(method),
                            onClick = { viewModel.toggleFarmingMethod(method) },
                            label = { Text(method) }
                        )
                    }
                }
            }

            // Sustainability Practices
            Column {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Sustainability Practices"
                        "ta" -> "நிலைத்தன்மை நடைமுறைகள்"
                        "si" -> "තිරසාර පිළිවෙත්"
                        else -> "Sustainability Practices"
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    viewModel.getAvailableSustainabilityPractices().forEach { practice ->
                        FilterChip(
                            selected = uiState.sustainabilityPractices.contains(practice),
                            onClick = { viewModel.toggleSustainabilityPractice(practice) },
                            label = { Text(practice) }
                        )
                    }
                }
            }
            
            // Image picker
            com.senthapps.slagrimarket.ui.common.ImagePicker(
                images = uiState.images,
                onImagesSelected = viewModel::updateImages,
                onImageRemoved = viewModel::removeImage,
                maxImages = 5,
                currentLanguage = currentLanguage
            )
            
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

/**
 * Convert voice input to numbers
 * Handles Tamil, Sinhala, and English number words
 */
private fun convertVoiceToNumber(input: String): String {
    // If already a number, return as-is
    if (input.toDoubleOrNull() != null) return input

    val lowerInput = input.lowercase().trim()

    // English number words
    val englishNumbers = mapOf(
        "zero" to "0", "one" to "1", "two" to "2", "three" to "3", "four" to "4",
        "five" to "5", "six" to "6", "seven" to "7", "eight" to "8", "nine" to "9",
        "ten" to "10", "eleven" to "11", "twelve" to "12", "thirteen" to "13",
        "fourteen" to "14", "fifteen" to "15", "sixteen" to "16", "seventeen" to "17",
        "eighteen" to "18", "nineteen" to "19", "twenty" to "20", "thirty" to "30",
        "forty" to "40", "fifty" to "50", "sixty" to "60", "seventy" to "70",
        "eighty" to "80", "ninety" to "90", "hundred" to "100", "thousand" to "1000"
    )

    // Tamil number words (common ones)
    val tamilNumbers = mapOf(
        "பூஜ்ஜியம்" to "0", "ஒன்று" to "1", "இரண்டு" to "2", "மூன்று" to "3",
        "நான்கு" to "4", "ஐந்து" to "5", "ஆறு" to "6", "ஏழு" to "7",
        "எட்டு" to "8", "ஒன்பது" to "9", "பத்து" to "10", "நூறு" to "100"
    )

    // Sinhala number words (common ones)
    val sinhalaNumbers = mapOf(
        "බින්ද" to "0", "එක" to "1", "දෙක" to "2", "තුන" to "3",
        "හතර" to "4", "පහ" to "5", "හය" to "6", "හත" to "7",
        "අට" to "8", "නවය" to "9", "දහය" to "10", "සියය" to "100"
    )

    // Try English first
    englishNumbers[lowerInput]?.let { return it }

    // Try Tamil
    tamilNumbers[lowerInput]?.let { return it }

    // Try Sinhala
    sinhalaNumbers[lowerInput]?.let { return it }

    // If no match found, return original input (user will need to correct it)
    return input
}
