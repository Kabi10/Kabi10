package com.senthapps.slagrimarket.ui.listings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import com.senthapps.slagrimarket.data.model.CropTypes
import com.senthapps.slagrimarket.ui.components.IndustrialFormDropdown
import com.senthapps.slagrimarket.ui.components.IndustrialFormField
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
// Step 2: Pick crop type + set price
// Step 3: Submit
// ============================================================================

@Composable
fun QuickListingScreen(
    onNavigateBack: () -> Unit,
    onListingCreated: () -> Unit,
    viewModel: CreateListingViewModel = hiltViewModel()
) {
    val language = LocalAppLanguage.current
    val textScale = LocalTextScale.current
    val uiState by viewModel.uiState.collectAsState()
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

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            photoUri = uri
            viewModel.updateImages(listOf(uri))
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
            // Camera not available, show gallery option
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
                        // Fallback to gallery
                        galleryLauncher.launch("image/*")
                    }
                },
                onPickFromGallery = {
                    galleryLauncher.launch("image/*")
                },
                onNext = { step = 2 }
            )

            2 -> DetailsStep(
                language = language,
                photoUri = photoUri,
                viewModel = viewModel,
                uiState = uiState,
                onSubmit = {
                    // Set defaults for quick mode
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
    onTakePhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
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
            // Show taken photo
            AsyncImage(
                model = photoUri,
                contentDescription = "Product photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
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
            // No photo yet - show options
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

            Spacer(modifier = Modifier.height(Spacing.md.dp))

            SecondaryButton(
                text = when (language) {
                    AppLanguage.SINHALA -> "ගැලරිය"
                    AppLanguage.TAMIL -> "கேலரி"
                    AppLanguage.ENGLISH -> "GALLERY"
                },
                onClick = onPickFromGallery
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
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.lg.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.md.dp)
    ) {
        // Small photo preview
        if (photoUri != null) {
            AsyncImage(
                model = photoUri,
                contentDescription = "Product photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Crop type dropdown
        IndustrialFormDropdown(
            label = when (language) {
                AppLanguage.SINHALA -> "බෝගය"
                AppLanguage.TAMIL -> "பயிர்"
                AppLanguage.ENGLISH -> "CROP"
            },
            selectedOption = uiState.cropType,
            options = CropTypes.ALL_CROPS,
            onOptionSelected = viewModel::updateCropType
        )

        // Price
        IndustrialFormField(
            label = when (language) {
                AppLanguage.SINHALA -> "මිල (රු/කිලෝ)"
                AppLanguage.TAMIL -> "விலை (ரூ/கிலோ)"
                AppLanguage.ENGLISH -> "PRICE (RS/KG)"
            },
            value = uiState.pricePerUnit,
            onValueChange = viewModel::updatePricePerUnit,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            errorMessage = uiState.priceError
        )

        // Quantity
        IndustrialFormField(
            label = when (language) {
                AppLanguage.SINHALA -> "ප්‍රමාණය (කිලෝ)"
                AppLanguage.TAMIL -> "அளவு (கிலோ)"
                AppLanguage.ENGLISH -> "QUANTITY (KG)"
            },
            value = uiState.quantity,
            onValueChange = viewModel::updateQuantity,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            errorMessage = uiState.quantityError
        )

        // Location
        IndustrialFormField(
            label = when (language) {
                AppLanguage.SINHALA -> "ස්ථානය"
                AppLanguage.TAMIL -> "இடம்"
                AppLanguage.ENGLISH -> "LOCATION"
            },
            value = uiState.location,
            onValueChange = viewModel::updateLocation,
            errorMessage = uiState.locationError
        )

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
                text = when (language) {
                    AppLanguage.SINHALA -> "දැන් විකුණන්න"
                    AppLanguage.TAMIL -> "இப்போது விற்கவும்"
                    AppLanguage.ENGLISH -> "SELL NOW"
                },
                onClick = onSubmit
            )
        }
    }
}
