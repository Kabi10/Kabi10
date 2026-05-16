package com.senthapps.slagrimarket.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.ui.common.LanguageToggleViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpVerificationScreen(
    phoneNumber: String,
    otpId: String,
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    languageViewModel: LanguageToggleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    var otp by remember { mutableStateOf("") }
    var timeLeft by remember { mutableStateOf(300) } // 5 minutes — matches backend OTP expiry

    // Countdown timer
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    // Navigate on success
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onVerificationSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Verify OTP"
                            "ta" -> "OTP சரிபார்க்கவும்"
                            "si" -> "OTP සත්‍යාපනය කරන්න"
                            else -> "Verify OTP"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                actions = {
                    com.senthapps.slagrimarket.ui.common.LanguageToggleButton(
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

            // Lock icon
            Surface(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Title
            Text(
                text = when (currentLanguage) {
                    "en" -> "Enter Verification Code"
                    "ta" -> "சரிபார்ப்பு குறியீட்டை உள்ளிடவும்"
                    "si" -> "සත්‍යාපන කේතය ඇතුළත් කරන්න"
                    else -> "Enter Verification Code"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            // Subtitle with phone number
            Text(
                text = when (currentLanguage) {
                    "en" -> "We sent a code to $phoneNumber"
                    "ta" -> "$phoneNumber க்கு குறியீட்டை அனுப்பினோம்"
                    "si" -> "අපි $phoneNumber වෙත කේතයක් යැව්වෙමු"
                    else -> "We sent a code to $phoneNumber"
                },
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // OTP input card
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { 
                            if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                                otp = it
                                // Auto-verify when 6 digits entered
                                if (it.length == 6) {
                                    keyboardController?.hide()
                                    viewModel.verifyOtp(phoneNumber, it, otpId)
                                }
                            }
                        },
                        label = {
                            Text(
                                when (currentLanguage) {
                                    "en" -> "6-Digit Code"
                                    "ta" -> "6 இலக்க குறியீடு"
                                    "si" -> "ඉලක්කම් 6 කේතය"
                                    else -> "6-Digit Code"
                                }
                            )
                        },
                        placeholder = { Text("000000") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.NumberPassword,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                keyboardController?.hide()
                                if (otp.length == 6) {
                                    viewModel.verifyOtp(phoneNumber, otp, otpId)
                                }
                            }
                        ),
                        singleLine = true,
                        isError = otp.length == 6 && uiState.error != null,
                        supportingText = {
                            if (otp.isNotEmpty() && otp.length < 6) {
                                Text(
                                    text = when (currentLanguage) {
                                        "en" -> "${6 - otp.length} digits remaining"
                                        "ta" -> "${6 - otp.length} இலக்கங்கள் மீதமுள்ளன"
                                        "si" -> "ඉලක්කම් ${6 - otp.length} ක් ඉතිරිය"
                                        else -> "${6 - otp.length} digits remaining"
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )

                    // Timer
                    if (timeLeft > 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    "en" -> "Code expires in ${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}"
                                    "ta" -> "குறியீடு ${timeLeft / 60}:${String.format("%02d", timeLeft % 60)} இல் காலாவதியாகும்"
                                    "si" -> "කේතය ${timeLeft / 60}:${String.format("%02d", timeLeft % 60)} කින් කල් ඉකුත් වේ"
                                    else -> "Code expires in ${timeLeft / 60}:${String.format("%02d", timeLeft % 60)}"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (timeLeft < 30) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

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
                            viewModel.verifyOtp(phoneNumber, otp, otpId)
                        },
                        enabled = otp.length == 6 && !uiState.isLoading,
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
                                "en" -> if (uiState.isLoading) "Verifying..." else "Verify Code"
                                "ta" -> if (uiState.isLoading) "சரிபார்க்கிறது..." else "குறியீட்டை சரிபார்க்கவும்"
                                "si" -> if (uiState.isLoading) "සත්‍යාපනය කරමින්..." else "කේතය සත්‍යාපනය කරන්න"
                                else -> if (uiState.isLoading) "Verifying..." else "Verify Code"
                            }
                        )
                    }
                }
            }

            // Resend code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when (currentLanguage) {
                        "en" -> "Didn't receive the code?"
                        "ta" -> "குறியீட்டைப் பெறவில்லையா?"
                        "si" -> "කේතය ලැබුණේ නැද්ද?"
                        else -> "Didn't receive the code?"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                TextButton(
                    onClick = {
                        otp = ""
                        viewModel.resetOtpState()
                        viewModel.sendOtp(phoneNumber)
                    },
                    enabled = timeLeft == 0 && !uiState.isLoading
                ) {
                    Text(
                        text = when (currentLanguage) {
                            "en" -> "Resend"
                            "ta" -> "மீண்டும் அனுப்பவும்"
                            "si" -> "නැවත යවන්න"
                            else -> "Resend"
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Help text
            Text(
                text = when (currentLanguage) {
                    "en" -> "Having trouble? Make sure you entered the correct phone number"
                    "ta" -> "சிக்கலா? சரியான தொலைபேசி எண்ணை உள்ளிட்டுள்ளீர்களா என்பதை உறுதிப்படுத்தவும்"
                    "si" -> "ගැටලුවක්ද? ඔබ නිවැරදි දුරකථන අංකය ඇතුළත් කර ඇති බව සහතික කරන්න"
                    else -> "Having trouble? Make sure you entered the correct phone number"
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            focusRequester.requestFocus()
        } catch (e: IllegalStateException) {
            // Focus requester may not be attached yet in test environments
            // This is expected and can be safely ignored
        }
    }
}
