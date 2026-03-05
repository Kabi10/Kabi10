package com.senthapps.slagrimarket.ui.listings

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.location.LocationServices
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.senthapps.slagrimarket.ui.settings.AccessibilityViewModel
import com.senthapps.slagrimarket.ui.theme.FieldMode
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.ui.components.DistrictPickerDialog
import com.senthapps.slagrimarket.ui.components.IndustrialFormDropdown
import com.senthapps.slagrimarket.ui.components.IndustrialFormField
import com.senthapps.slagrimarket.ui.components.NumericKeypadDialog
import com.senthapps.slagrimarket.ui.components.PrimaryButton
import com.senthapps.slagrimarket.ui.components.SecondaryButton
import com.senthapps.slagrimarket.ui.home.AppLanguage
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.HumanIndustrial
import com.senthapps.slagrimarket.ui.theme.HumanIndustrialType
import com.senthapps.slagrimarket.ui.theme.LocalAppLanguage
import com.senthapps.slagrimarket.ui.theme.LocalTextScale
import com.senthapps.slagrimarket.ui.theme.Spacing
import com.senthapps.slagrimarket.ui.theme.industrialClickable
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// ============================================================================
// QUICK LISTING SCREEN - Photo-First, 3-Step Flow
// Step 1: Take photo (camera opens immediately)
// Step 2: Pick crop type + set price (with keypad + district picker)
// Step 3: Submit
// ============================================================================

@Composable
fun QuickListingScreen(
    onNavigateBack: () -> Unit,
    onListingCreated: () -> Unit,
    viewModel: CreateListingViewModel = hiltViewModel(),
    accessibilityViewModel: AccessibilityViewModel = hiltViewModel()
) {
    val language = LocalAppLanguage.current
    val textScale = LocalTextScale.current
    val uiState by viewModel.uiState.collectAsState()
    val isFieldMode by accessibilityViewModel.isFieldModeEnabled.collectAsState()
    var step by remember { mutableIntStateOf(1) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    // Handle success
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onListingCreated()
        }
    }

    // Camera launcher
    val context = androidx.compose.ui.platform.LocalContext.current
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempPhotoUri != null) {
            photoUri = tempPhotoUri
            viewModel.updateImages(listOf(tempPhotoUri!!))
            step = 2
        }
    }

    // Auto-launch camera on first entry
    LaunchedEffect(Unit) {
        try {
            val photoFile = File.createTempFile("listing_", ".jpg", context.cacheDir)
            tempPhotoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                photoFile
            )
            cameraLauncher.launch(tempPhotoUri!!)
        } catch (e: Exception) {
            // Camera not available, show form directly
            step = 1
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(HumanIndustrial.Rice)
    ) {
        // Header
        QuickListingHeader(
            language = language,
            step = step,
            onBackClick = {
                if (step > 1) step-- else onNavigateBack()
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BorderWidth.Thick)
                .background(HumanIndustrial.Earth)
        )

        when (step) {
            1 -> PhotoStep(
                language = language,
                photoUri = photoUri,
                isFieldMode = isFieldMode,
                onTakePhoto = {
                    try {
                        val photoFile = File.createTempFile("listing_", ".jpg", context.cacheDir)
                        tempPhotoUri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.provider",
                            photoFile
                        )
                        cameraLauncher.launch(tempPhotoUri!!)
                    } catch (e: Exception) {
                        step = 2 // Skip photo if camera unavailable
                    }
                },
                onNext = { step = 2 }
            )

            2 -> DetailsStep(
                language = language,
                photoUri = photoUri,
                viewModel = viewModel,
                uiState = uiState,
                isFieldMode = isFieldMode,
                onSubmit = {
                    if (uiState.unit.isBlank()) viewModel.updateUnit("kg")
                    if (uiState.quality.isBlank()) viewModel.updateQuality("GRADE_A")
                    if (uiState.harvestDate.isBlank()) {
                        viewModel.updateHarvestDate(
                            LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        )
                    }
                    viewModel.createListing()
                }
            )
        }
    }
}

@Composable
private fun QuickListingHeader(
    language: AppLanguage,
    step: Int,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(HumanIndustrial.Rice)
            .padding(horizontal = Spacing.lg.dp, vertical = Spacing.md.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .height(48.dp)
                .industrialClickable(onClick = onBackClick),
            contentAlignment = Alignment.CenterStart
        ) {
            Text(
                text = when (language) {
                    AppLanguage.SINHALA -> "ආපසු"
                    AppLanguage.TAMIL -> "பின்"
                    AppLanguage.ENGLISH -> "BACK"
                },
                style = HumanIndustrialType.sectionLabel,
                color = HumanIndustrial.Earth
            )
        }

        Text(
            text = when (language) {
                AppLanguage.SINHALA -> "ඉක්මන් විකුණීම"
                AppLanguage.TAMIL -> "விரைவு விற்பனை"
                AppLanguage.ENGLISH -> "QUICK SELL"
            },
            style = HumanIndustrialType.screenTitle,
            color = HumanIndustrial.Ink,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun PhotoStep(
    language: AppLanguage,
    photoUri: Uri?,
    isFieldMode: Boolean = false,
    onTakePhoto: () -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (photoUri != null) {
            // Show taken photo — half-screen in field mode for easier review
            AsyncImage(
                model = photoUri,
                contentDescription = "Product photo",
                modifier = if (isFieldMode)
                    Modifier.fillMaxWidth().fillMaxHeight(0.5f)
                else
                    Modifier.fillMaxWidth().height(300.dp),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(Spacing.lg.dp))

            PrimaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "ඉදිරියට"
                    AppLanguage.TAMIL -> "அடுத்து"
                    AppLanguage.ENGLISH -> "NEXT"
                },
                onClick = onNext
            )

            Spacer(modifier = Modifier.height(Spacing.md.dp))

            SecondaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "නැවත ගන්න"
                    AppLanguage.TAMIL -> "மீண்டும் எடு"
                    AppLanguage.ENGLISH -> "RETAKE"
                },
                onClick = onTakePhoto
            )
        } else {
            // No photo yet — camera may still be launching
            Text(
                text = when (language) {
                    AppLanguage.SINHALA -> "ඔබේ අස්වැන්නේ\nඡායාරූපයක් ගන්න"
                    AppLanguage.TAMIL -> "உங்கள் விளைச்சலின்\nபுகைப்படம் எடுக்கவும்"
                    AppLanguage.ENGLISH -> "TAKE A PHOTO\nOF YOUR HARVEST"
                },
                style = HumanIndustrialType.screenTitle,
                color = HumanIndustrial.Ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xl.dp))

            PrimaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "කැමරාව"
                    AppLanguage.TAMIL -> "கேமரா"
                    AppLanguage.ENGLISH -> "CAMERA"
                },
                onClick = onTakePhoto
            )
        }
    }
}

@Composable
private fun DetailsStep(
    language: AppLanguage,
    photoUri: Uri?,
    viewModel: CreateListingViewModel,
    uiState: CreateListingUiState,
    isFieldMode: Boolean = false,
    onSubmit: () -> Unit
) {
    // Dialog state
    var showPriceKeypad by remember { mutableStateOf(false) }
    var showQuantityKeypad by remember { mutableStateOf(false) }
    var showDistrictPicker by remember { mutableStateOf(false) }
    // Voice crop confirmation
    var voiceCropSuggestion by remember { mutableStateOf<Pair<String, String>?>(null) } // emoji to cropName

    val context = androidx.compose.ui.platform.LocalContext.current

    // GPS permission launcher
    @SuppressLint("MissingPermission")
    fun tryGpsAutoFill() {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                val district = nearestSriLankaDistrict(loc.latitude, loc.longitude)
                viewModel.updateLocation(district)
            }
        }
    }

    val gpsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) tryGpsAutoFill()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Small photo preview — taller in field mode for better visibility
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Product photo",
                modifier = if (isFieldMode)
                    Modifier.fillMaxWidth().fillMaxHeight(0.5f)
                else
                    Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Crop type dropdown — options from DoA registry, fallback to hardcoded list
        val cropOptions by viewModel.cropSuggestions.collectAsState()
        IndustrialFormDropdown(
            label = when (language) {
                AppLanguage.SINHALA -> "බෝගය"
                AppLanguage.TAMIL -> "பயிர்"
                AppLanguage.ENGLISH -> "CROP"
            },
            selectedOption = uiState.cropType,
            options = cropOptions,
            onOptionSelected = viewModel::updateCropType
        )

        // Yield tip banner — static tips for common Sri Lanka crops
        val yieldTips = remember {
            mapOf(
                "tomato" to "Best yield Apr–Jun. Water 2× daily.",
                "red onion" to "Harvest after 90 days. Reduce water last 2 weeks.",
                "chili" to "Space 45 cm apart. Peak yield Aug–Oct.",
                "brinjal" to "Needs well-drained soil. Harvest every 5 days.",
                "okra" to "Germinates best above 25°C. Harvest at 8 cm."
            )
        }
        val tipKey = uiState.cropType.lowercase()
        val tip = yieldTips.entries.firstOrNull { tipKey.contains(it.key) }?.value
        AnimatedVisibility(visible = tip != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🌱", style = MaterialTheme.typography.bodySmall)
                    Text(
                        text = tip ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Grade chips — 72dp tall in field mode for sweaty outdoor fingers
        val chipHeight = if (isFieldMode) 72.dp else 56.dp
        val grades = listOf("GRADE_A" to "A", "GRADE_B" to "B", "GRADE_C" to "C")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm.dp)
        ) {
            grades.forEach { (gradeKey, gradeLabel) ->
                val isSelected = uiState.quality == gradeKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(chipHeight)
                        .background(if (isSelected) HumanIndustrial.Gold else HumanIndustrial.Dust)
                        .industrialClickable(onClick = { viewModel.updateQuality(gradeKey) }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = gradeLabel,
                        style = HumanIndustrialType.sectionLabel,
                        color = if (isSelected) HumanIndustrial.Rice else HumanIndustrial.Stone
                    )
                }
            }
        }

        // Voice crop confirmation chip — shown after fuzzy match
        voiceCropSuggestion?.let { (emoji, cropName) ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HumanIndustrial.Gold.copy(alpha = 0.12f))
                    .padding(Spacing.sm.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm.dp)
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "$emoji $cropName ද?"
                            AppLanguage.TAMIL -> "$emoji $cropName ஆ?"
                            AppLanguage.ENGLISH -> "Did you mean $emoji $cropName?"
                        },
                        style = HumanIndustrialType.body,
                        color = HumanIndustrial.Ink,
                        modifier = Modifier.weight(1f)
                    )
                    Box(
                        modifier = Modifier
                            .background(HumanIndustrial.Green)
                            .industrialClickable(onClick = {
                                viewModel.updateCropType(cropName)
                                voiceCropSuggestion = null
                            })
                            .padding(horizontal = Spacing.sm.dp, vertical = Spacing.xs.dp)
                    ) { Text("✅", style = HumanIndustrialType.sectionLabel) }
                    Box(
                        modifier = Modifier
                            .background(HumanIndustrial.Dust)
                            .industrialClickable(onClick = { voiceCropSuggestion = null })
                            .padding(horizontal = Spacing.sm.dp, vertical = Spacing.xs.dp)
                    ) { Text("❌", style = HumanIndustrialType.sectionLabel) }
                }
            }
        }

        // Price — tappable display field, opens keypad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(HumanIndustrial.Dust)
                .industrialClickable(onClick = { showPriceKeypad = true })
                .padding(horizontal = Spacing.lg.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.SINHALA -> "මිල (රු/කිලෝ)"
                        AppLanguage.TAMIL -> "விலை (ரூ/கிலோ)"
                        AppLanguage.ENGLISH -> "PRICE (RS/KG)"
                    },
                    style = HumanIndustrialType.sectionLabel,
                    color = HumanIndustrial.Stone
                )
                Text(
                    text = if (uiState.pricePerUnit.isBlank()) "—" else "Rs ${uiState.pricePerUnit}",
                    style = HumanIndustrialType.productName,
                    color = if (uiState.pricePerUnit.isBlank()) HumanIndustrial.Stone else HumanIndustrial.Gold
                )
            }
        }

        // Quantity — tappable display field, opens keypad
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .background(HumanIndustrial.Dust)
                .industrialClickable(onClick = { showQuantityKeypad = true })
                .padding(horizontal = Spacing.lg.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (language) {
                        AppLanguage.SINHALA -> "ප්‍රමාණය (කිලෝ)"
                        AppLanguage.TAMIL -> "அளவு (கிலோ)"
                        AppLanguage.ENGLISH -> "QUANTITY (KG)"
                    },
                    style = HumanIndustrialType.sectionLabel,
                    color = HumanIndustrial.Stone
                )
                Text(
                    text = if (uiState.quantity.isBlank()) "—" else "${uiState.quantity} kg",
                    style = HumanIndustrialType.productName,
                    color = if (uiState.quantity.isBlank()) HumanIndustrial.Stone else HumanIndustrial.Ink
                )
            }
        }

        // Location — district picker + GPS auto-fill button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .background(HumanIndustrial.Dust)
                    .industrialClickable(onClick = { showDistrictPicker = true })
                    .padding(horizontal = Spacing.lg.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (language) {
                            AppLanguage.SINHALA -> "📍 දිස්ත්‍රික්කය"
                            AppLanguage.TAMIL -> "📍 மாவட்டம்"
                            AppLanguage.ENGLISH -> "📍 DISTRICT"
                        },
                        style = HumanIndustrialType.sectionLabel,
                        color = HumanIndustrial.Stone
                    )
                    Text(
                        text = if (uiState.location.isBlank()) "▶ " + when (language) {
                            AppLanguage.SINHALA -> "තෝරන්න"
                            AppLanguage.TAMIL -> "தேர்ந்தெடு"
                            AppLanguage.ENGLISH -> "SELECT"
                        } else uiState.location,
                        style = HumanIndustrialType.sectionLabel,
                        color = if (uiState.location.isBlank()) HumanIndustrial.Earth else HumanIndustrial.Ink
                    )
                }
            }
            // GPS auto-detect button
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .background(HumanIndustrial.Earth)
                    .industrialClickable(onClick = {
                        gpsPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                    })
                    .padding(horizontal = Spacing.md.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🛰", style = HumanIndustrialType.productName)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Error message
        if (uiState.error != null) {
            Text(
                text = uiState.error,
                style = HumanIndustrialType.body,
                color = HumanIndustrial.Earth,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Submit button
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = HumanIndustrial.Green
                )
            }
        } else {
            PrimaryButton(
                text = when {
                    isFieldMode && language == AppLanguage.TAMIL -> "இப்போது அனுப்பு"
                    language == AppLanguage.SINHALA -> "දැන් විකුණන්න"
                    language == AppLanguage.TAMIL -> "இப்போது விற்கவும்"
                    else -> "SELL NOW"
                },
                onClick = onSubmit
            )
        }
    }

    // ── Dialogs ──────────────────────────────────────────────────────────────

    if (showPriceKeypad) {
        NumericKeypadDialog(
            title = when (language) {
                AppLanguage.SINHALA -> "මිල (රු/කිලෝ)"
                AppLanguage.TAMIL -> "விலை (ரூ/கிலோ)"
                AppLanguage.ENGLISH -> "Price (Rs per kg)"
            },
            initialValue = uiState.pricePerUnit,
            onConfirm = { value ->
                viewModel.updatePricePerUnit(value)
                showPriceKeypad = false
            },
            onDismiss = { showPriceKeypad = false },
            language = when (language) {
                AppLanguage.SINHALA -> "si"
                AppLanguage.TAMIL -> "ta"
                AppLanguage.ENGLISH -> "en"
            }
        )
    }

    if (showQuantityKeypad) {
        NumericKeypadDialog(
            title = when (language) {
                AppLanguage.SINHALA -> "ප්‍රමාණය (කිලෝ)"
                AppLanguage.TAMIL -> "அளவு (கிலோ)"
                AppLanguage.ENGLISH -> "Quantity (kg)"
            },
            initialValue = uiState.quantity,
            onConfirm = { value ->
                viewModel.updateQuantity(value)
                showQuantityKeypad = false
            },
            onDismiss = { showQuantityKeypad = false },
            language = when (language) {
                AppLanguage.SINHALA -> "si"
                AppLanguage.TAMIL -> "ta"
                AppLanguage.ENGLISH -> "en"
            }
        )
    }

    if (showDistrictPicker) {
        DistrictPickerDialog(
            language = when (language) {
                AppLanguage.SINHALA -> "si"
                AppLanguage.TAMIL -> "ta"
                AppLanguage.ENGLISH -> "en"
            },
            selectedDistrict = uiState.location,
            onDistrictSelected = { district ->
                viewModel.updateLocation(district)
                showDistrictPicker = false
            },
            onDismiss = { showDistrictPicker = false }
        )
    }
}

/**
 * Returns the name of the nearest Sri Lanka district to the given GPS coordinates,
 * using Haversine distance to each district's centroid.
 */
private fun nearestSriLankaDistrict(lat: Double, lng: Double): String {
    // District centroids (lat, lng, name) — all 25 districts
    val centroids = listOf(
        Triple(9.6615, 80.0255, "Jaffna"),
        Triple(9.3803, 80.3770, "Kilinochchi"),
        Triple(8.9778, 80.2114, "Mannar"),
        Triple(9.1685, 80.8718, "Vavuniya"),
        Triple(8.7514, 80.4997, "Mullaitivu"),
        Triple(8.5922, 81.2341, "Trincomalee"),
        Triple(8.3348, 80.8628, "Anuradhapura"),
        Triple(8.0196, 81.0951, "Polonnaruwa"),
        Triple(7.8731, 81.6969, "Batticaloa"),
        Triple(7.2953, 81.6747, "Ampara"),
        Triple(6.8917, 81.3322, "Badulla"),
        Triple(6.9934, 80.7970, "Kandy"),
        Triple(7.4863, 80.3647, "Kurunegala"),
        Triple(7.0579, 79.9020, "Puttalam"),
        Triple(6.9271, 79.8612, "Gampaha"),
        Triple(6.9271, 79.8612, "Colombo"),
        Triple(6.8219, 80.0415, "Kalutara"),
        Triple(7.2906, 80.6337, "Matale"),
        Triple(6.9497, 80.7891, "Nuwara Eliya"),
        Triple(6.0535, 80.2210, "Galle"),
        Triple(6.0411, 80.9716, "Monaragala"),
        Triple(5.9549, 80.5550, "Hambantota"),
        Triple(6.1222, 80.7108, "Matara"),
        Triple(7.8731, 80.6497, "Kegalle"),
        Triple(6.8279, 80.3640, "Ratnapura")
    )

    fun haversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Earth radius km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).let { it * it } +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).let { it * it }
        return r * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    }

    return centroids.minByOrNull { (cLat, cLng, _) ->
        haversine(lat, lng, cLat, cLng)
    }?.third ?: "Colombo"
}
