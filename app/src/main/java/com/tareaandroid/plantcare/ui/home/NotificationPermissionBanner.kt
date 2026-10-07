package com.tareaandroid.plantcare.ui.home

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.tareaandroid.plantcare.R

/**
 * Tarjeta que pide el permiso de notificaciones (Android 13+) explicando antes para qué
 * sirve. Si el usuario lo deniega definitivamente, ofrece abrir los ajustes de la app.
 */
@Composable
fun NotificationPermissionBanner(modifier: Modifier = Modifier) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    var granted by rememberSaveable { mutableStateOf(context.hasNotificationPermission()) }
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var permanentlyDenied by rememberSaveable { mutableStateOf(false) }

    // Al volver de los ajustes del sistema se comprueba de nuevo el permiso
    LifecycleResumeEffect(Unit) {
        granted = context.hasNotificationPermission()
        onPauseOrDispose { }
    }

    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        granted = isGranted
        // Denegado y Android ya no mostrará el diálogo: solo queda ir a los ajustes
        permanentlyDenied = !isGranted && context.findActivity()
            ?.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) == false
    }

    if (granted || dismissed) return

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(Icons.Outlined.NotificationsActive, contentDescription = null)
                Text(stringResource(R.string.notifications_banner_title), style = MaterialTheme.typography.titleMedium)
            }
            Text(
                stringResource(
                    if (permanentlyDenied) R.string.notifications_permission_denied else R.string.notifications_banner_text,
                ),
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(Modifier.align(Alignment.End), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { dismissed = true }) { Text(stringResource(R.string.notifications_banner_dismiss)) }
                if (permanentlyDenied) {
                    Button(onClick = { context.openAppSettings() }) {
                        Text(stringResource(R.string.permission_open_settings))
                    }
                } else {
                    Button(onClick = { requestPermission.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                        Text(stringResource(R.string.notifications_banner_enable))
                    }
                }
            }
        }
    }
}

private fun Context.hasNotificationPermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

private fun Context.openAppSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri())
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
    )
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
