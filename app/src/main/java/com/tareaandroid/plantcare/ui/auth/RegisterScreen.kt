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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.theme.PlantCareTheme

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.registered) {
        if (uiState.registered) onRegisterSuccess()
    }

    RegisterContent(uiState = uiState, onEvent = viewModel::onEvent, onBack = onBack)
}

@Composable
fun RegisterContent(
    uiState: RegisterUiState,
    onEvent: (RegisterEvent) -> Unit,
    onBack: () -> Unit,
) {
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
                AuthHeader(subtitle = R.string.register_subtitle)

                EmailField(
                    value = uiState.email,
                    onValueChange = { onEvent(RegisterEvent.EmailChanged(it)) },
                    error = uiState.emailError,
                    enabled = !uiState.isLoading,
                )
                PasswordField(
                    value = uiState.password,
                    onValueChange = { onEvent(RegisterEvent.PasswordChanged(it)) },
                    label = R.string.auth_password,
                    error = uiState.passwordError,
                    enabled = !uiState.isLoading,
                    imeAction = ImeAction.Next,
                )
                PasswordField(
                    value = uiState.confirmPassword,
                    onValueChange = { onEvent(RegisterEvent.ConfirmPasswordChanged(it)) },
                    label = R.string.auth_confirm_password,
                    error = uiState.confirmError,
                    enabled = !uiState.isLoading,
                    onImeAction = { onEvent(RegisterEvent.Submit) },
                )

                uiState.generalError?.let {
                    Text(stringResource(it), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }

                Button(
                    onClick = { onEvent(RegisterEvent.Submit) },
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text(stringResource(R.string.register_action))
                    }
                }
                TextButton(onClick = onBack, enabled = !uiState.isLoading) {
                    Text(stringResource(R.string.register_go_login))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterPreview() {
    PlantCareTheme {
        RegisterContent(
            uiState = RegisterUiState(email = "ana@ejemplo.com", confirmError = R.string.auth_error_passwords_mismatch),
            onEvent = {},
            onBack = {},
        )
    }
}
