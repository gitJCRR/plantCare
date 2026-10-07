package com.tareaandroid.plantcare.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.theme.PlantCareTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.loggedIn) {
        if (uiState.loggedIn) onLoginSuccess()
    }

    LoginContent(uiState = uiState, onEvent = viewModel::onEvent, onGoToRegister = onGoToRegister)
}

@Composable
fun LoginContent(
    uiState: LoginUiState,
    onEvent: (LoginEvent) -> Unit,
    onGoToRegister: () -> Unit,
) {
    var showResetDialog by rememberSaveable { mutableStateOf(false) }

    if (showResetDialog) {
        PasswordResetDialog(
            initialEmail = uiState.email,
            onSend = { email ->
                showResetDialog = false
                onEvent(LoginEvent.SendPasswordReset(email))
            },
            onDismiss = { showResetDialog = false },
        )
    }

    Surface(Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                AuthHeader(subtitle = R.string.login_subtitle)

                EmailField(
                    value = uiState.email,
                    onValueChange = { onEvent(LoginEvent.EmailChanged(it)) },
                    error = uiState.emailError,
                    enabled = !uiState.isLoading,
                )
                PasswordField(
                    value = uiState.password,
                    onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                    label = R.string.auth_password,
                    error = uiState.passwordError,
                    enabled = !uiState.isLoading,
                    imeAction = ImeAction.Done,
                    onImeAction = { onEvent(LoginEvent.Submit) },
                )

                TextButton(
                    onClick = { showResetDialog = true },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.align(Alignment.End),
                ) {
                    Text(stringResource(R.string.login_forgot_password))
                }

                uiState.generalError?.let {
                    Text(
                        stringResource(it),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                uiState.infoMessage?.let {
                    Text(
                        stringResource(it),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                    )
                }

                Button(
                    onClick = { onEvent(LoginEvent.Submit) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.login_action))
                    }
                }
                TextButton(onClick = onGoToRegister, enabled = !uiState.isLoading) {
                    Text(stringResource(R.string.login_go_register))
                }
            }
        }
    }
}

/** Diálogo que pide el correo al que enviar el enlace para restablecer la contraseña. */
@Composable
private fun PasswordResetDialog(initialEmail: String, onSend: (String) -> Unit, onDismiss: () -> Unit) {
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reset_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.reset_message))
                EmailField(value = email, onValueChange = { email = it }, error = null, enabled = true)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSend(email) }) { Text(stringResource(R.string.reset_action)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginPreview() {
    PlantCareTheme {
        LoginContent(
            uiState = LoginUiState(email = "ana@ejemplo.com", generalError = R.string.auth_error_invalid_credentials),
            onEvent = {},
            onGoToRegister = {},
        )
    }
}
