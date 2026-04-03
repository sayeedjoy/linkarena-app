package com.sayeedjoy.linkarena.ui.auth

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sayeedjoy.linkarena.ui.components.GradientButton
import com.sayeedjoy.linkarena.ui.components.LinkArenaTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SignupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val screenBackgroundColor = if (isDarkTheme) Color.Black else MaterialTheme.colorScheme.background
    val inputContainerColor =
        if (isDarkTheme) Color(0xFF121212) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onSignupSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(screenBackgroundColor)
        )

        Scaffold(
            containerColor = screenBackgroundColor,
            topBar = {
                LinkArenaTopBar(
                    title = { Text("Create Account", style = MaterialTheme.typography.titleLarge) },
                    onNavigationClick = onNavigateToLogin,
                    containerColor = screenBackgroundColor,
                    scrolledContainerColor = screenBackgroundColor,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Name Field
                Text(
                    text = "Full Name",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                TextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    singleLine = true,
                    placeholder = {
                        Text(
                            "The Archivist",
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isDarkTheme) 1.dp else 0.dp,
                            color = if (isDarkTheme) Color(0xFF2A2A2A) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputContainerColor,
                        unfocusedContainerColor = inputContainerColor,
                        disabledContainerColor = inputContainerColor,
                        focusedIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Transparent,
                        unfocusedIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color.Transparent,
                        disabledIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color.Transparent
                    ),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Email Field
                Text(
                    text = "Email Address",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                TextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    singleLine = true,
                    placeholder = {
                        Text(
                            "archivist@folio.com",
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isDarkTheme) 1.dp else 0.dp,
                            color = if (isDarkTheme) Color(0xFF2A2A2A) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputContainerColor,
                        unfocusedContainerColor = inputContainerColor,
                        disabledContainerColor = inputContainerColor,
                        focusedIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Transparent,
                        unfocusedIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color.Transparent,
                        disabledIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color.Transparent
                    ),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Password Field
                Text(
                    text = "Password",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                TextField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    singleLine = true,
                    placeholder = {
                        Text(
                            "••••••••",
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isDarkTheme) 1.dp else 0.dp,
                            color = if (isDarkTheme) Color(0xFF2A2A2A) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputContainerColor,
                        unfocusedContainerColor = inputContainerColor,
                        disabledContainerColor = inputContainerColor,
                        focusedIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Transparent,
                        unfocusedIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color.Transparent,
                        disabledIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color.Transparent
                    ),
                    enabled = !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Confirm Password Field
                Text(
                    text = "Confirm Password",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )

                TextField(
                    value = uiState.confirmPassword,
                    onValueChange = viewModel::onConfirmPasswordChange,
                    singleLine = true,
                    placeholder = {
                        Text(
                            "••••••••",
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.signup()
                        }
                    ),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            width = if (isDarkTheme) 1.dp else 0.dp,
                            color = if (isDarkTheme) Color(0xFF2A2A2A) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = inputContainerColor,
                        unfocusedContainerColor = inputContainerColor,
                        disabledContainerColor = inputContainerColor,
                        focusedIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) else Color.Transparent,
                        unfocusedIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.5f) else Color.Transparent,
                        disabledIndicatorColor = if (isDarkTheme) MaterialTheme.colorScheme.outline.copy(alpha = 0.3f) else Color.Transparent
                    ),
                    enabled = !uiState.isLoading
                )

                if (uiState.confirmPassword.isNotEmpty() && uiState.password != uiState.confirmPassword) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Passwords do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "button_scale")

                GradientButton(
                    onClick = viewModel::signup,
                    isLoading = uiState.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clip(RoundedCornerShape(12.dp)),
                    enabled = uiState.name.isNotBlank() && uiState.email.isNotBlank() &&
                              uiState.password.isNotBlank() && uiState.confirmPassword.isNotBlank() &&
                              uiState.password == uiState.confirmPassword
                ) {
                    Text(
                        text = "Create Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sign In",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable(!uiState.isLoading) { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
