package com.tareaandroid.plantcare.ui.auth

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.components.PlaceholderScreen

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onGoToRegister: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.login_title)) {
        Button(onClick = onLoginSuccess) { Text(stringResource(R.string.login_action)) }
        TextButton(onClick = onGoToRegister) { Text(stringResource(R.string.login_go_register)) }
    }
}
