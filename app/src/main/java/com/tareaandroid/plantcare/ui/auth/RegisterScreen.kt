package com.tareaandroid.plantcare.ui.auth

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.components.PlaceholderScreen

@Composable
fun RegisterScreen(onRegisterSuccess: () -> Unit, onBack: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.register_title)) {
        Button(onClick = onRegisterSuccess) { Text(stringResource(R.string.register_action)) }
        TextButton(onClick = onBack) { Text(stringResource(R.string.register_go_login)) }
    }
}
