package com.senthapps.slagrimarket.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.senthapps.slagrimarket.ui.components.IndustrialButton
import com.senthapps.slagrimarket.ui.theme.AgrimarketBlack
import com.senthapps.slagrimarket.ui.theme.AgrimarketGray
import com.senthapps.slagrimarket.ui.theme.AgrimarketRed
import com.senthapps.slagrimarket.ui.theme.AgrimarketWhite
import com.senthapps.slagrimarket.ui.theme.BorderWidth
import com.senthapps.slagrimarket.ui.theme.Spacing

// ============================================================================
// INDUSTRIAL PHONE INPUT SCREEN
// Entry point for auth flow - brutal industrial styling
// No cards, no icons, no rounded corners, thick borders
// ============================================================================

/**
 * Industrial phone input screen for authentication
 * First screen users see in release mode
 *
 * @param onNavigateToOtpVerification Callback with phone number and OTP ID
 * @param viewModel Auth view model for OTP operations
 */
@Composable
fun IndustrialPhoneInputScreen(
    onNavigateToOtpVerification: (String, String) -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var phoneNumber by remember { mutableStateOf("") }

    // Navigate to OTP screen when OTP is sent
    LaunchedEffect(uiState.otpSent) {
        if (uiState.otpSent && uiState.otpId != null) {
            onNavigateToOtpVerification(phoneNumber, uiState.otpId!!)
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
                .padding(Spacing.Double)
        ) {
            // Title section
            Text(
                text = "JAFFNA FARMERS",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                )
            )
            Text(
                text = "MARKETPLACE",
                style = TextStyle(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                )
            )

            Spacer(modifier = Modifier.height(Spacing.Quad))

            // Phone input label
            Text(
                text = "ENTER PHONE NUMBER",
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Phone input field with thick border
            BasicTextField(
                value = phoneNumber,
                onValueChange = { newValue ->
                    // Only allow digits and + sign, max 15 chars
                    if (newValue.length <= 15 && newValue.all { it.isDigit() || it == '+' }) {
                        phoneNumber = newValue
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
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = AgrimarketBlack,
                    letterSpacing = 0.sp
                ),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        keyboardController?.hide()
                        if (isValidSriLankanPhone(phoneNumber)) {
                            viewModel.sendOtp(phoneNumber)
                        }
                    }
                ),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (phoneNumber.isEmpty()) {
                            Text(
                                text = "+94 7X XXX XXXX",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AgrimarketGray,
                                    letterSpacing = 0.sp
                                )
                            )
                        }
                        innerTextField()
                    }
                }
            )

            // Validation hint
            Text(
                text = "SRI LANKA MOBILE NUMBER",
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = AgrimarketGray,
                    letterSpacing = 0.sp
                ),
                modifier = Modifier.padding(top = 4.dp)
            )

            // Error message (if any)
            if (uiState.error != null) {
                Spacer(modifier = Modifier.height(Spacing.Base))
                Text(
                    text = uiState.error!!.uppercase(),
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgrimarketRed,
                        letterSpacing = 0.sp
                    )
                )
            }

            // Validation error for invalid phone
            if (phoneNumber.isNotEmpty() && !isValidSriLankanPhone(phoneNumber)) {
                Spacer(modifier = Modifier.height(Spacing.Base))
                Text(
                    text = "INVALID PHONE NUMBER",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = AgrimarketRed,
                        letterSpacing = 0.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(Spacing.Double))

            // Send OTP button
            IndustrialButton(
                text = if (uiState.isLoading) "SENDING OTP..." else "SEND CODE",
                onClick = {
                    keyboardController?.hide()
                    viewModel.sendOtp(phoneNumber)
                },
                enabled = isValidSriLankanPhone(phoneNumber) && !uiState.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer text
            Text(
                text = "BY CONTINUING YOU AGREE TO OUR TERMS OF SERVICE",
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

/**
 * Validate Sri Lankan phone number
 * Accepts: +94XXXXXXXXX, 0XXXXXXXXX, or 9+ digits
 */
private fun isValidSriLankanPhone(phone: String): Boolean {
    val cleaned = phone.replace(Regex("[^0-9+]"), "")
    return when {
        cleaned.startsWith("+94") -> cleaned.length >= 12
        cleaned.startsWith("0") -> cleaned.length == 10
        cleaned.length >= 9 -> true
        else -> false
    }
}
