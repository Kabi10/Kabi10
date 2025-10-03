package com.senthapps.slagrimarket.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.common.LanguageToggleButton
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneInputScreen(
    onNavigateToOtpVerification: (String, String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var phoneNumber by remember { mutableStateOf("") }

    LaunchedEffect(uiState.otpSent) {
        if (uiState.otpSent && uiState.otpId != null) {
            onNavigateToOtpVerification(phoneNumber, uiState.otpId!!)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Welcome"
                            "ta" -> "வரவேற்கிறோம்"
                            "si" -> "ආයුබෝවන්"
                            else -> "Welcome"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    LanguageToggleButton(
                        currentLanguage = currentLanguage,
                        onLanguageChange = languageViewModel::setLanguage,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // App logo/icon placeholder
            Surface(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Title
            Text(
                text = when (currentLanguage) {
                    "en" -> "Jaffna Farmers Marketplace"
                    "ta" -> "யாழ்ப்பாணம் விவசாயிகள் சந்தை"
                    "si" -> "යාපනය ගොවි වෙළඳපොළ"
                    else -> "Jaffna Farmers Marketplace"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Subtitle
            Text(
                text = when (currentLanguage) {
                    "en" -> "Connect farmers and buyers directly"
                    "ta" -> "விவசாயிகளையும் வாங்குபவர்களையும் நேரடியாக இணைக்கவும்"
                    "si" -> "ගොවීන් සහ ගැනුම්කරුවන් සෘජුවම සම්බන්ධ කරන්න"
                    else -> "Connect farmers and buyers directly"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Phone input card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Enter your phone number"
                            "ta" -> "உங்கள் தொலைபேசி எண்ணை உள்ளிடவும்"
                            "si" -> "ඔබේ දුරකථන අංකය ඇතුළත් කරන්න"
                            else -> "Enter your phone number"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = when (currentLanguage) {
                            "en" -> "We'll send you a verification code"
                            "ta" -> "நாங்கள் உங்களுக்கு சரிபார்ப்பு குறியீட்டை அனுப்புவோம்"
                            "si" -> "අපි ඔබට සත්‍යාපන කේතයක් එවන්නෙමු"
                            else -> "We'll send you a verification code"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { 
                            // Only allow digits and limit to reasonable length
                            if (it.length <= 15 && it.all { char -> char.isDigit() || char == '+' }) {
                                phoneNumber = it
                            }
                        },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    "en" -> "Phone Number"
                                    "ta" -> "தொலைபேசி எண்"
                                    "si" -> "දුරකථන අංකය"
                                    else -> "Phone Number"
                                }
                            )
                        },
                        placeholder = { Text("+94771234567") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Phone,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (isValidPhoneNumber(phoneNumber)) {
                                    viewModel.sendOtp(phoneNumber)
                                }
                            }
                        ),
                        singleLine = true,
                        isError = phoneNumber.isNotEmpty() && !isValidPhoneNumber(phoneNumber),
                        supportingText = {
                            if (phoneNumber.isNotEmpty() && !isValidPhoneNumber(phoneNumber)) {
                                Text(
                                    text = when (currentLanguage) {
                                        "en" -> "Please enter a valid phone number"
                                        "ta" -> "சரியான தொலைபேசி எண்ணை உள்ளிடவும்"
                                        "si" -> "කරුණාකර වලංගු දුරකථන අංකයක් ඇතුළත් කරන්න"
                                        else -> "Please enter a valid phone number"
                                    },
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    // Error message
                    if (uiState.error != null) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = uiState.error!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            keyboardController?.hide()
                            viewModel.sendOtp(phoneNumber)
                        },
                        enabled = isValidPhoneNumber(phoneNumber) && !uiState.isLoading,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = when (currentLanguage) {
                                "en" -> if (uiState.isLoading) "Sending..." else "Send Code"
                                "ta" -> if (uiState.isLoading) "அனுப்புகிறது..." else "குறியீட்டை அனுப்பவும்"
                                "si" -> if (uiState.isLoading) "යවමින්..." else "කේතය යවන්න"
                                else -> if (uiState.isLoading) "Sending..." else "Send Code"
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Terms and privacy
            Text(
                text = when (currentLanguage) {
                    "en" -> "By continuing, you agree to our Terms of Service and Privacy Policy"
                    "ta" -> "தொடர்வதன் மூலம், எங்கள் சேவை விதிமுறைகள் மற்றும் தனியுரிமைக் கொள்கையை ஏற்கிறீர்கள்"
                    "si" -> "ඉදිරියට යාමෙන්, ඔබ අපගේ සේවා කොන්දේසි සහ රහස්‍යතා ප්‍රතිපත්තියට එකඟ වේ"
                    else -> "By continuing, you agree to our Terms of Service and Privacy Policy"
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

private fun isValidPhoneNumber(phone: String): Boolean {
    // Remove all non-digit characters except +
    val cleaned = phone.replace(Regex("[^0-9+]"), "")
    
    // Check if it's a valid Sri Lankan number
    // Format: +94XXXXXXXXX or 0XXXXXXXXX (10 digits after 0)
    return when {
        cleaned.startsWith("+94") -> cleaned.length >= 12 // +94 + 9 digits minimum
        cleaned.startsWith("0") -> cleaned.length == 10 // 0 + 9 digits
        cleaned.length >= 9 -> true // At least 9 digits
        else -> false
    }
}
