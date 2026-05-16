package com.senthapps.slagrimarket.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import java.io.File

/**
 * Image picker component with camera and gallery support
 * Supports multiple images with preview and removal
 */
@Composable
fun ImagePicker(
    images: List<Uri>,
    onImagesSelected: (List<Uri>) -> Unit,
    onImageRemoved: (Uri) -> Unit,
    maxImages: Int = 5,
    currentLanguage: String = "en",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            val newImages = (images + uris).take(maxImages)
            onImagesSelected(newImages)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && tempCameraUri != null) {
            // Image was captured successfully, add it to the list
            val newImages = (images + tempCameraUri!!).take(maxImages)
            onImagesSelected(newImages)
        }
    }

    // Function to create a temp file and get URI for camera
    fun createTempImageUri(): Uri? {
        return try {
            val tempFile = File.createTempFile(
                "camera_photo_${System.currentTimeMillis()}",
                ".jpg",
                context.cacheDir
            ).apply {
                createNewFile()
                deleteOnExit()
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                tempFile
            )
        } catch (e: Exception) {
            null
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Photos (${images.size}/$maxImages)"
                    "ta" -> "புகைப்படங்கள் (${images.size}/$maxImages)"
                    "si" -> "ඡායාරූප (${images.size}/$maxImages)"
                    else -> "Photos (${images.size}/$maxImages)"
                },
                style = MaterialTheme.typography.titleMedium
            )

            if (images.size < maxImages) {
                TextButton(
                    onClick = { showImageSourceDialog = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Add Photo"
                            "ta" -> "புகைப்படம் சேர்க்க"
                            "si" -> "ඡායාරූපය එක් කරන්න"
                            else -> "Add Photo"
                        }
                    )
                }
            }
        }

        // Image grid
        if (images.isEmpty()) {
            EmptyImageState(
                onAddClick = { showImageSourceDialog = true },
                currentLanguage = currentLanguage
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(images) { imageUri ->
                    ImagePreviewCard(
                        imageUri = imageUri,
                        onRemove = { onImageRemoved(imageUri) }
                    )
                }

                // Add more button
                if (images.size < maxImages) {
                    item {
                        AddImageCard(
                            onClick = { showImageSourceDialog = true },
                            currentLanguage = currentLanguage
                        )
                    }
                }
            }
        }
    }

    // Image source dialog
    if (showImageSourceDialog) {
        ImageSourceDialog(
            onDismiss = { showImageSourceDialog = false },
            onCameraClick = {
                showImageSourceDialog = false
                // Create temp file and launch camera
                val uri = createTempImageUri()
                if (uri != null) {
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                } else {
                    // If camera file creation fails, fallback to gallery
                    galleryLauncher.launch("image/*")
                }
            },
            onGalleryClick = {
                showImageSourceDialog = false
                galleryLauncher.launch("image/*")
            },
            currentLanguage = currentLanguage
        )
    }
}

@Composable
private fun EmptyImageState(
    onAddClick: () -> Unit,
    currentLanguage: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        onClick = onAddClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = when (currentLanguage) {
                    "en" -> "Add photos of your product"
                    "ta" -> "உங்கள் பொருளின் புகைப்படங்களைச் சேர்க்கவும்"
                    "si" -> "ඔබේ නිෂ්පාදනයේ ඡායාරූප එක් කරන්න"
                    else -> "Add photos of your product"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImagePreviewCard(
    imageUri: Uri,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(120.dp)
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(imageUri)
                .crossfade(true)
                .build(),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Remove button
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(32.dp)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun AddImageCard(
    onClick: () -> Unit,
    currentLanguage: String
) {
    Card(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when (currentLanguage) {
                    "en" -> "Add"
                    "ta" -> "சேர்க்க"
                    "si" -> "එක් කරන්න"
                    else -> "Add"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImageSourceDialog(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    currentLanguage: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = when (currentLanguage) {
                    "en" -> "Add Photo"
                    "ta" -> "புகைப்படம் சேர்க்க"
                    "si" -> "ඡායාරූපය එක් කරන්න"
                    else -> "Add Photo"
                }
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    onClick = onCameraClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Take Photo"
                                "ta" -> "புகைப்படம் எடுக்க"
                                "si" -> "ඡායාරූපයක් ගන්න"
                                else -> "Take Photo"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Card(
                    onClick = onGalleryClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = when (currentLanguage) {
                                "en" -> "Choose from Gallery"
                                "ta" -> "கேலரியிலிருந்து தேர்வு செய்க"
                                "si" -> "ගැලරියෙන් තෝරන්න"
                                else -> "Choose from Gallery"
                            },
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Cancel"
                        "ta" -> "ரத்துசெய்"
                        "si" -> "අවලංගු කරන්න"
                        else -> "Cancel"
                    }
                )
            }
        }
    )
}
