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
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

// ============================================================================
// INDUSTRIAL PASSWORD SCREEN
// Phone + Password auth — no SMS required
// Login mode: password only | Register mode: password + confirm + user type
// ============================================================================

@Composable
fun IndustrialPasswordScreen(
    phoneNumber: String,
    onNavigateBack: () -> Unit,
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val keyboardController = LocalSoftwareKeyboardController.current

    var isLoginMode by remember { mutableStateOf(true) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isFarmer by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthSuccess()
    }

    // Reset error when switching modes
    LaunchedEffect(isLoginMode) {
        viewModel.clearError()
        password = ""
        confirmPassword = ""
    }

    val passwordValid = password.length >= 6
    val canSubmit = if (isLoginMode) {
        passwordValid && !uiState.isLoading
    } else {
        passwordValid && password == confirmPassword && !uiState.isLoading
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AgrimarketBlack)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .border(width = BorderWidth.Thick, color = AgrimarketBlack, shape = RectangleShape)
                .background(AgrimarketWhite)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .border(width = BorderWidth.Thin, color = AgrimarketBlack, shape = RectangleShape)
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
                        text = stringResource(R.string.action_back).uppercase(),
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AgrimarketBlack)
                    )
                }
                Text(
                    text = if (isLoginMode) stringResource(R.string.auth_login).uppercase() else "CREATE ACCOUNT",
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = AgrimarketBlack)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Spacing.Double)
            ) {
                // Phone display
                Text(
                    text = "PHONE NUMBER",
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Medium, color = AgrimarketGray)
                )
                Text(
                    text = phoneNumber,
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Black, color = AgrimarketBlack)
                )

                Spacer(modifier = Modifier.height(Spacing.Double))

                // Password field
                Text(
                    text = stringResource(R.string.auth_password_hint).uppercase(),
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, color = AgrimarketBlack),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                PasswordField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Min. 6 characters",
                    isError = uiState.error != null,
                    imeAction = if (isLoginMode) ImeAction.Done else ImeAction.Next,
                    onDone = {
                        keyboardController?.hide()
                        if (canSubmit) submitAuth(isLoginMode, phoneNumber, password, isFarmer, viewModel)
                    }
                )

                if (!passwordValid && password.isNotEmpty()) {
                    Text(
                        text = "PASSWORD TOO SHORT",
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AgrimarketRed),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Register-only fields
                if (!isLoginMode) {
                    Spacer(modifier = Modifier.height(Spacing.Base))
                    Text(
                        text = "CONFIRM PASSWORD",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, color = AgrimarketBlack),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    PasswordField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        placeholder = "Repeat password",
                        isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                        imeAction = ImeAction.Done,
                        onDone = {
                            keyboardController?.hide()
                            if (canSubmit) submitAuth(isLoginMode, phoneNumber, password, isFarmer, viewModel)
                        }
                    )
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = "PASSWORDS DO NOT MATCH",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AgrimarketRed),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(Spacing.Base))
                    Text(
                        text = "I AM A",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, color = AgrimarketBlack),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row {
                        UserTypeChip(label = "FARMER", selected = isFarmer, onClick = { isFarmer = true })
                        Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                        UserTypeChip(label = "BUYER", selected = !isFarmer, onClick = { isFarmer = false })
                    }
                }

                // Error
                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(Spacing.Base))
                    Text(
                        text = uiState.error!!,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AgrimarketRed)
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.Double))

                // Submit button
                IndustrialButton(
                    text = when {
                        uiState.isLoading -> if (isLoginMode) "LOGGING IN..." else "CREATING ACCOUNT..."
                        isLoginMode -> stringResource(R.string.auth_login).uppercase()
                        else -> "CREATE ACCOUNT"
                    },
                    onClick = {
                        keyboardController?.hide()
                        submitAuth(isLoginMode, phoneNumber, password, isFarmer, viewModel)
                    },
                    enabled = canSubmit,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.Base))

                // Mode toggle
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = { isLoginMode = !isLoginMode }
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isLoginMode) "NEW HERE? CREATE AN ACCOUNT" else "ALREADY REGISTERED? LOGIN",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = AgrimarketBlack
                        )
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = stringResource(R.string.auth_terms_agreement).uppercase(),
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, color = AgrimarketGray)
                )
            }
        }
    }
}

private fun submitAuth(
    isLoginMode: Boolean,
    phoneNumber: String,
    password: String,
    isFarmer: Boolean,
    viewModel: AuthViewModel
) {
    if (isLoginMode) {
        viewModel.loginWithPassword(phoneNumber, password)
    } else {
        viewModel.registerWithPassword(phoneNumber, password, if (isFarmer) "FARMER" else "BUYER")
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isError: Boolean,
    imeAction: ImeAction,
    onDone: () -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .border(
                width = BorderWidth.Standard,
                color = if (isError) AgrimarketRed else AgrimarketBlack,
                shape = RectangleShape
            )
            .background(AgrimarketWhite)
            .padding(horizontal = Spacing.Base),
        textStyle = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = AgrimarketBlack),
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }, onNext = {}),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium, color = AgrimarketGray)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun UserTypeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .border(
                width = if (selected) BorderWidth.Standard else BorderWidth.Thin,
                color = AgrimarketBlack,
                shape = RectangleShape
            )
            .background(if (selected) AgrimarketBlack else AgrimarketWhite)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (selected) AgrimarketWhite else AgrimarketBlack
            )
        )
    }
}
