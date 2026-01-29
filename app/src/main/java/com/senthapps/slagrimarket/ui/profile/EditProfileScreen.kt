package com.senthapps.slagrimarket.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.data.model.UserType
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditProfileViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.loadUserProfile()
    }

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Edit Profile"
                            "ta" -> "சுயவிவரத்தைத் திருத்து"
                            "si" -> "පැතිකඩ සංස්කරණය කරන්න"
                            else -> "Edit Profile"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Profile photo section
                    ProfilePhotoSection(
                        currentLanguage = currentLanguage
                    )

                    // Name field
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::updateName,
                        label = {
                            Text(when (currentLanguage) {
                                "en" -> "Name *"
                                "ta" -> "பெயர் *"
                                "si" -> "නම *"
                                else -> "Name *"
                            })
                        },
                        isError = uiState.nameError != null,
                        supportingText = uiState.nameError?.let { { Text(it) } },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Phone number (read-only)
                    OutlinedTextField(
                        value = uiState.phoneNumber,
                        onValueChange = {},
                        label = {
                            Text(when (currentLanguage) {
                                "en" -> "Phone Number"
                                "ta" -> "தொலைபேசி எண்"
                                "si" -> "දුරකථන අංකය"
                                else -> "Phone Number"
                            })
                        },
                        enabled = false,
                        leadingIcon = {
                            Icon(Icons.Default.Phone, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // User type (read-only)
                    OutlinedTextField(
                        value = when (uiState.userType) {
                            UserType.FARMER -> when (currentLanguage) {
                                "ta" -> "விவசாயி"
                                "si" -> "ගොවියා"
                                else -> "Farmer"
                            }
                            UserType.BUYER -> when (currentLanguage) {
                                "ta" -> "வாங்குபவர்"
                                "si" -> "ගැනුම්කරු"
                                else -> "Buyer"
                            }
                            else -> ""
                        },
                        onValueChange = {},
                        label = {
                            Text(when (currentLanguage) {
                                "en" -> "Account Type"
                                "ta" -> "கணக்கு வகை"
                                "si" -> "ගිණුම් වර්ගය"
                                else -> "Account Type"
                            })
                        },
                        enabled = false,
                        leadingIcon = {
                            Icon(Icons.Default.Person, null)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Location field
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
                        isError = uiState.locationError != null,
                        supportingText = uiState.locationError?.let { { Text(it) } },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, null)
                        },
                        placeholder = {
                            Text(when (currentLanguage) {
                                "en" -> "e.g., Chavakachcheri, Jaffna"
                                "ta" -> "எ.கா., சாவகச்சேரி, யாழ்ப்பாணம்"
                                "si" -> "උදා., චාවකච්චේරි, යාපනය"
                                else -> "e.g., Chavakachcheri, Jaffna"
                            })
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Error message
                    uiState.error?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = error,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Save button
                    Button(
                        onClick = { viewModel.saveProfile() },
                        enabled = !uiState.isLoading && viewModel.isFormValid(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text(when (currentLanguage) {
                                "en" -> "Save Changes"
                                "ta" -> "மாற்றங்களைச் சேமி"
                                "si" -> "වෙනස්කම් සුරකින්න"
                                else -> "Save Changes"
                            })
                        }
                    }

                    // Cancel button
                    OutlinedButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(when (currentLanguage) {
                            "en" -> "Cancel"
                            "ta" -> "ரத்துசெய்"
                            "si" -> "අවලංගු කරන්න"
                            else -> "Cancel"
                        })
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfilePhotoSection(
    currentLanguage: String
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        TextButton(
            onClick = {
                // Photo upload functionality coming in a future update
                Toast.makeText(context, "Photo upload coming soon", Toast.LENGTH_SHORT).show()
            }
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(when (currentLanguage) {
                "en" -> "Change Photo"
                "ta" -> "புகைப்படத்தை மாற்று"
                "si" -> "ඡායාරූපය වෙනස් කරන්න"
                else -> "Change Photo"
            })
        }
    }
}
