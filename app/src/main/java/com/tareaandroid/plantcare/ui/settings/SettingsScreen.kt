package com.tareaandroid.plantcare.ui.settings

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.components.PlaceholderScreen

@Composable
fun SettingsScreen(onLogout: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.settings_title)) {
        Button(onClick = onLogout) { Text(stringResource(R.string.settings_logout)) }
    }
}
