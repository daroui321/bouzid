package com.bouzid.player.ui.activation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bouzid.player.ui.theme.*

@Composable
fun ActivationScreen(
    viewModel: ActivationViewModel,
    onActivated: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var email by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(state) {
        if (state is ActivationState.NotRequired || state is ActivationState.Success) {
            onActivated()
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        BackgroundDark,
                        Color(0xFF0D0D1A),
                        BackgroundDark
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        when (val s = state) {
            is ActivationState.Loading -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = GoldPrimary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Checking configuration\u2026",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            is ActivationState.Ready -> {
                ActivationCard(
                    configMessage = s.configMessage,
                    email = email,
                    onEmailChange = { if (!s.isSubmitting) email = it },
                    onSubmit = {
                        focusManager.clearFocus()
                        viewModel.submitEmail(email)
                    },
                    isLoading = s.isSubmitting,
                    error = s.error,
                    focusRequester = focusRequester
                )
            }
            is ActivationState.NotRequired, is ActivationState.Success -> {}
        }
    }
}

@Composable
private fun ActivationCard(
    configMessage: String,
    email: String,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean,
    error: String?,
    focusRequester: FocusRequester
) {
    Card(
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(64.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Bouzid",
                style = MaterialTheme.typography.displayLarge,
                color = GoldPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "IPTV Player",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary
            )
            Spacer(Modifier.height(24.dp))
            if (configMessage.isNotBlank()) {
                Text(
                    text = configMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
            }
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email address") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = GoldPrimary)
                },
                singleLine = true,
                enabled = !isLoading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldPrimary,
                    unfocusedBorderColor = SurfaceLight,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = GoldPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
            )
            if (error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = error,
                    color = ErrorRed,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onSubmit()
                },
                enabled = !isLoading && email.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = BackgroundDark,
                    disabledContainerColor = SurfaceLight
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = BackgroundDark,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Activate",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
