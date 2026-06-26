package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.ChatGptGreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.PremiumGold
import com.example.ui.theme.TextGray
import com.example.ui.theme.TextWhite
import com.example.ui.viewmodel.ChatViewModel

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var customDomain by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    
    var errorText by remember { mutableStateOf<String?>(null) }
    var successText by remember { mutableStateOf<String?>(null) }
    
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Elegant glowing logo container
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(ChatGptGreen.copy(alpha = 0.1f), RoundedCornerShape(24.dp))
                    .border(1.dp, ChatGptGreen.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_nova_logo),
                    contentDescription = "Nova AI Logo",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = when (authMode) {
                    AuthMode.SIGN_IN -> "Nova AI Workspace"
                    AuthMode.SIGN_UP -> "Create Account"
                    AuthMode.FORGOT_PASSWORD -> "Reset Password"
                },
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = TextWhite,
                    letterSpacing = 0.5.sp
                )
            )

            Text(
                text = when (authMode) {
                    AuthMode.SIGN_IN -> "Connect to Nova Cloud or your custom domain server"
                    AuthMode.SIGN_UP -> "Sign up to track limits and subscription across sessions"
                    AuthMode.FORGOT_PASSWORD -> "Recover access to your local workspace securely"
                },
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = TextGray,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Auth Tab Switcher (Only show for Sign In or Sign Up modes)
            if (authMode != AuthMode.FORGOT_PASSWORD) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .background(DarkSurface, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (authMode == AuthMode.SIGN_IN) ChatGptGreen else Color.Transparent)
                            .clickable {
                                authMode = AuthMode.SIGN_IN
                                errorText = null
                                successText = null
                            }
                            .testTag("signin_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (authMode == AuthMode.SIGN_IN) Color.White else TextGray
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (authMode == AuthMode.SIGN_UP) ChatGptGreen else Color.Transparent)
                            .clickable {
                                authMode = AuthMode.SIGN_UP
                                errorText = null
                                successText = null
                            }
                            .testTag("signup_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Sign Up",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (authMode == AuthMode.SIGN_UP) Color.White else TextGray
                            )
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorText = null
                    successText = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("email_field"),
                label = { Text("Email Address") },
                placeholder = { Text("name@workspace.com") },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = ChatGptGreen)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChatGptGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedLabelColor = ChatGptGreen,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorText = null
                    successText = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("password_field"),
                label = { 
                    Text(
                        if (authMode == AuthMode.FORGOT_PASSWORD) "New Password" else "Password"
                    ) 
                },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = ChatGptGreen)
                },
                trailingIcon = {
                    Text(
                        text = if (showPassword) "HIDE" else "SHOW",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ChatGptGreen,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier
                            .clickable { showPassword = !showPassword }
                            .padding(end = 12.dp)
                    )
                },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (authMode == AuthMode.FORGOT_PASSWORD) ImeAction.Done else ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ChatGptGreen,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                    focusedLabelColor = ChatGptGreen,
                    unfocusedLabelColor = TextGray,
                    focusedTextColor = TextWhite,
                    unfocusedTextColor = TextWhite
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Forgot Password Trigger (Only in Sign In mode)
            if (authMode == AuthMode.SIGN_IN) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, end = 4.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        text = "Forgot Password?",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = PremiumGold,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier
                            .clickable {
                                authMode = AuthMode.FORGOT_PASSWORD
                                errorText = null
                                successText = null
                            }
                            .testTag("forgot_password_button")
                    )
                }
            }

            // Custom Domain Field (Only in Sign In or Sign Up modes)
            if (authMode != AuthMode.FORGOT_PASSWORD) {
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customDomain,
                    onValueChange = {
                        customDomain = it
                        errorText = null
                        successText = null
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_domain_field"),
                    label = { Text("Custom Domain (Optional)") },
                    placeholder = { Text("e.g. api.nova.yourcompany.com") },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = PremiumGold)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    ),
                    helperText = "Connect directly to your private enterprise domain or proxy.",
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PremiumGold,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        focusedLabelColor = PremiumGold,
                        unfocusedLabelColor = TextGray,
                        focusedTextColor = TextWhite,
                        unfocusedTextColor = TextWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Text(
                    text = "Secure custom domains automatically route private corporate LLM queries safely inside your own enterprise cloud perimeter.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = TextGray.copy(alpha = 0.6f),
                        lineHeight = 15.sp
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp, start = 4.dp, end = 4.dp)
                )
            }

            // Success & Error Indicators
            if (successText != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = successText ?: "",
                    color = ChatGptGreen,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (errorText != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorText ?: "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Button
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (email.isBlank()) {
                        errorText = "Email address is required."
                        return@Button
                    }
                    if (password.length < 6) {
                        errorText = "Password must be at least 6 characters."
                        return@Button
                    }

                    when (authMode) {
                        AuthMode.SIGN_IN -> {
                            viewModel.signIn(
                                email = email,
                                psw = password,
                                domain = customDomain,
                                onSuccess = {
                                    errorText = null
                                    successText = null
                                },
                                onFailure = {
                                    errorText = it
                                }
                            )
                        }
                        AuthMode.SIGN_UP -> {
                            viewModel.signUp(
                                email = email,
                                psw = password,
                                domain = customDomain,
                                onSuccess = {
                                    errorText = null
                                    successText = null
                                },
                                onFailure = {
                                    errorText = it
                                }
                            )
                        }
                        AuthMode.FORGOT_PASSWORD -> {
                            viewModel.resetPassword(
                                email = email,
                                psw = password,
                                onSuccess = {
                                    successText = "Password updated successfully! You can now Sign In."
                                    errorText = null
                                    authMode = AuthMode.SIGN_IN
                                    password = "" // clear password field for entry
                                },
                                onFailure = {
                                    errorText = it
                                    successText = null
                                }
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("auth_submit_button"),
                colors = ButtonDefaults.buttonColors(containerColor = ChatGptGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = when (authMode) {
                        AuthMode.SIGN_IN -> "Secure Sign In"
                        AuthMode.SIGN_UP -> "Create Corporate Workspace Account"
                        AuthMode.FORGOT_PASSWORD -> "Reset & Update Password"
                    },
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Secondary option (either back to login or global guest)
            if (authMode == AuthMode.FORGOT_PASSWORD) {
                Text(
                    text = "Back to Sign In",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier
                        .clickable {
                            authMode = AuthMode.SIGN_IN
                            errorText = null
                            successText = null
                        }
                        .padding(8.dp)
                        .testTag("back_to_signin_button")
                )
            } else {
                Text(
                    text = "Continue with Global Shared Server",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = PremiumGold,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    modifier = Modifier
                        .clickable {
                            viewModel.signIn(
                                email = "guest@nova-shared.ai",
                                psw = "default-guest-pass",
                                domain = "",
                                onSuccess = {
                                    errorText = null
                                    successText = null
                                },
                                onFailure = {
                                    errorText = it
                                }
                            )
                        }
                        .padding(8.dp)
                        .testTag("auth_guest_button")
                )
            }
        }
    }
}

// Simple adapter for helper text support
@Composable
fun OutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    helperText: String? = null,
    colors: androidx.compose.material3.TextFieldColors = OutlinedTextFieldDefaults.colors(),
    shape: androidx.compose.ui.graphics.Shape = OutlinedTextFieldDefaults.shape
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            label = label,
            placeholder = placeholder,
            singleLine = singleLine,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            colors = colors,
            shape = shape
        )
        if (helperText != null) {
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = TextGray.copy(alpha = 0.5f)
                ),
                modifier = Modifier.padding(start = 12.dp, top = 4.dp)
            )
        }
    }
}
