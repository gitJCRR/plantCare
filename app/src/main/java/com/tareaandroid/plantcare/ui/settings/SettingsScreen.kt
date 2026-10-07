package com.tareaandroid.plantcare.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.theme.PlantCareTheme

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(uiState = uiState, onSignOut = viewModel::signOut, onTestReminder = viewModel::testReminder)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(uiState: SettingsUiState, onSignOut: () -> Unit, onTestReminder: () -> Unit) {
    var showSignOutDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(Modifier.fillMaxWidth()) {
                    ListItem(
                        leadingContent = {
                            Icon(
                                Icons.Outlined.AccountCircle,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        },
                        overlineContent = { Text(stringResource(R.string.settings_account)) },
                        headlineContent = { Text(uiState.email.orEmpty()) },
                    )
                }
                Card(Modifier.fillMaxWidth()) {
                    ListItem(
                        leadingContent = {
                            Icon(Icons.Outlined.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        },
                        headlineContent = { Text(stringResource(R.string.settings_reminders)) },
                        supportingContent = { Text(stringResource(R.string.settings_reminders_summary)) },
                    )
                    TextButton(onClick = onTestReminder, modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                        Text(stringResource(R.string.settings_test_reminder))
                    }
                }
                OutlinedButton(onClick = { showSignOutDialog = true }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(stringResource(R.string.settings_logout), modifier = Modifier.padding(start = 8.dp))
                }
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            title = { Text(stringResource(R.string.settings_logout_title)) },
            text = { Text(stringResource(R.string.settings_logout_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showSignOutDialog = false
                    onSignOut()
                }) { Text(stringResource(R.string.settings_logout)) }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SettingsPreview() {
    PlantCareTheme {
        SettingsContent(uiState = SettingsUiState(email = "ana@ejemplo.com"), onSignOut = {}, onTestReminder = {})
    }
}
