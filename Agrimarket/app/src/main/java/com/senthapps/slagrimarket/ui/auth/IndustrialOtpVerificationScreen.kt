package com.senthapps.slagrimarket.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.R
import com.senthapps.slagrimarket.ui.components.IndustrialButton
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketGray
import com.senthapps.slagrimarket.ui.theme.AgrimarketRed
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.Spacing
import kotlinx.coroutines.delay

// ============================================================================
// INDUSTRIAL OTP VERIFICATION SCREEN
// 6-digit OTP entry with countdown timer
// No cards, no icons (except back), thick borders
// ============================================================================

/**
 * Industrial OTP verification screen
 * User enters 6-digit code sent to their phone
 *
 * @param phoneNumber The phone number OTP was sent to
 * @param otpId The OTP session ID from backend
 * @param onNavigateBack Navigate back to phone input
 * @param onVerificationSuccess Navigate to home on success
 * @param viewModel Auth view model
 */
@Composable
fun IndustrialOtpVerificationScreen(
    phoneNumber: String,
    otpId: String,
    onNavigateBack: () -> Unit,
    onVerificationSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var otp by remember { mutableStateOf("") }
    var timeLeft by remember { mutableIntStateOf(300) } // 5 minutes — matches backend OTP expiry

    // Countdown timer
    LaunchedEffect(Unit) {
        while (timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
    }

    // Navigate on successful verification
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onVerificationSuccess()
        }
    }

    // Outer container with safe drawing insets
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgrimarketBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        // Main content with outer border
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(
                    width = BorderWidth.Thick,
                    color = AgrimarketBlack,
                    shape = RectangleShape
                )
                .background(AgrimarketWhite)
        ) {
            // Header with back button - Industrial Design (no icons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(
                        width = BorderWidth.Thin,
                        color = AgrimarketBlack,
                        shape = RectangleShape
                    )
                    .padding(horizontal = Spacing.Base),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .height(56.dp)
                        .clickable(
                            onClick = onNavigateBack,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        )
                        .padding(end = Spacing.Base),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BACK",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgrimarketBlack,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
                Text(
                    text = "VERIFY CODE",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = AgrimarketBlack,
                        letterSpacing = 0.sp
                    )
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.Double)
            ) {
                // Phone number display
                Text(
                    text = "CODE SENT TO",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgrimarketGray,
                        letterSpacing = 0.sp
                    )
                )
                Text(
                    text = phoneNumber.uppercase(),
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = AgrimarketBlack,
                        letterSpacing = 0.sp
                    )
                )

                Spacer(modifier = Modifier.height(Spacing.Double))

                // OTP input label
                Text(
                    text = "ENTER 6-DIGIT CODE",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = AgrimarketBlack,
                        letterSpacing = 0.sp
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // OTP input field
                BasicTextField(
                    value = otp,
                    onValueChange = { newValue ->
                        if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                            otp = newValue
                            // Auto-verify when 6 digits entered
                            if (newValue.length == 6) {
                                keyboardController?.hide()
                                viewModel.verifyOtp(phoneNumber, newValue, otpId)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .border(
                            width = BorderWidth.Standard,
                            color = if (uiState.error != null) AgrimarketRed else AgrimarketBlack,
                            shape = RectangleShape
                        )
                        .background(AgrimarketWhite)
                        .padding(horizontal = Spacing.Base),
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = AgrimarketBlack,
                        letterSpacing = 8.sp
                    ),
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
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (otp.isEmpty()) {
                                Text(
                                    text = "------",
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        color = AgrimarketGray,
                                        letterSpacing = 8.sp
                                    )
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                // Timer display
                Spacer(modifier = Modifier.height(Spacing.Base))
                if (timeLeft > 0) {
                    val minutes = timeLeft / 60
                    val seconds = timeLeft % 60
                    Text(
                        text = "CODE EXPIRES IN ${minutes}:${String.format("%02d", seconds)}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (timeLeft < 30) AgrimarketRed else AgrimarketGray,
                            letterSpacing = 0.sp
                        )
                    )
                } else {
                    Text(
                        text = "CODE EXPIRED",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgrimarketRed,
                            letterSpacing = 0.sp
                        )
                    )
                }

                // Error message - display as-is from backend (no uppercase transformation)
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(Spacing.Base))
                    Text(
                        text = uiState.error!!,
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgrimarketRed,
                            letterSpacing = 0.sp
                        )
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Double))

                // Verify button
                IndustrialButton(
                    text = if (uiState.isLoading) "VERIFYING..." else "VERIFY",
                    onClick = {
                        keyboardController?.hide()
                        viewModel.verifyOtp(phoneNumber, otp, otpId)
                    },
                    enabled = otp.length == 6 && !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.Base))

                // Resend button (only when timer expired)
                if (timeLeft == 0 && !uiState.isLoading) {
                    IndustrialButton(
                        text = "RESEND CODE",
                        onClick = {
                            otp = ""
                            viewModel.resetOtpState()
                            viewModel.sendOtp(phoneNumber)
                            timeLeft = 300
                        },
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Help text
                Text(
                    text = "HAVING TROUBLE? CHECK YOUR PHONE NUMBER",
                    style = TextStyle(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = AgrimarketGray,
                        letterSpacing = 0.sp
                    )
                )
            }
        }
    }
}
