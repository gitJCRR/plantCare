package com.tareaandroid.plantcare.ui.edit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.data.photo.PhotoFile
import com.tareaandroid.plantcare.ui.components.PlantPhoto

/**
 * Foto de la planta en el formulario: hacer una foto (pide el permiso de cámara),
 * elegirla de la galería (no necesita permisos) o quitarla.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PlantPhotoSection(
    photoPath: String?,
    createPhotoFile: () -> PhotoFile,
    onEvent: (PlantEditEvent) -> Unit,
) {
    val context = LocalContext.current
    var pendingPhoto by remember { mutableStateOf<PhotoFile?>(null) }
    var dialog by rememberSaveable { mutableStateOf(PermissionDialog.NONE) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val photo = pendingPhoto
        if (success && photo != null) onEvent(PlantEditEvent.PhotoTaken(photo.path))
        pendingPhoto = null
    }

    fun openCamera() {
        val photo = createPhotoFile()
        pendingPhoto = photo
        takePicture.launch(photo.uri)
    }

    val requestCameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        when {
            granted -> openCamera()
            // Si el usuario marcó «no volver a preguntar», solo queda ir a los ajustes
            !context.shouldShowCameraRationale() -> dialog = PermissionDialog.GO_TO_SETTINGS
        }
    }

    val pickFromGallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onEvent(PlantEditEvent.PhotoPickedFromGallery(uri))
    }

    fun onCameraClick() {
        when {
            context.hasCameraPermission() -> openCamera()
            // Ya lo denegó una vez: se explica para qué hace falta antes de volver a pedirlo
            context.shouldShowCameraRationale() -> dialog = PermissionDialog.RATIONALE
            else -> requestCameraPermission.launch(Manifest.permission.CAMERA)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        PlantPhoto(
            photoPath = photoPath,
            contentDescription = stringResource(R.string.photo_description),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(16.dp)),
            iconSize = 64.dp,
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledTonalButton(onClick = ::onCameraClick) {
                ButtonLabel(Icons.Outlined.PhotoCamera, stringResource(R.string.photo_take))
            }
            OutlinedButton(onClick = {
                pickFromGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }) {
                ButtonLabel(Icons.Outlined.PhotoLibrary, stringResource(R.string.photo_gallery))
            }
            if (photoPath != null) {
                TextButton(onClick = { onEvent(PlantEditEvent.PhotoRemoved) }) {
                    ButtonLabel(Icons.Outlined.Delete, stringResource(R.string.photo_remove))
                }
            }
        }
    }

    when (dialog) {
        PermissionDialog.NONE -> Unit
        PermissionDialog.RATIONALE -> AlertDialog(
            onDismissRequest = { dialog = PermissionDialog.NONE },
            title = { Text(stringResource(R.string.camera_permission_title)) },
            text = { Text(stringResource(R.string.camera_permission_rationale)) },
            confirmButton = {
                TextButton(onClick = {
                    dialog = PermissionDialog.NONE
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }) { Text(stringResource(R.string.permission_allow)) }
            },
            dismissButton = {
                TextButton(onClick = { dialog = PermissionDialog.NONE }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
        PermissionDialog.GO_TO_SETTINGS -> AlertDialog(
            onDismissRequest = { dialog = PermissionDialog.NONE },
            title = { Text(stringResource(R.string.camera_permission_title)) },
            text = { Text(stringResource(R.string.camera_permission_denied)) },
            confirmButton = {
                TextButton(onClick = {
                    dialog = PermissionDialog.NONE
                    context.openAppSettings()
                }) { Text(stringResource(R.string.permission_open_settings)) }
            },
            dismissButton = {
                TextButton(onClick = { dialog = PermissionDialog.NONE }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

private enum class PermissionDialog { NONE, RATIONALE, GO_TO_SETTINGS }

@Composable
private fun ButtonLabel(icon: ImageVector, text: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Text(text, modifier = Modifier.padding(start = 8.dp))
}

private fun Context.hasCameraPermission() =
    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED

private fun Context.shouldShowCameraRationale(): Boolean =
    findActivity()?.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) == true

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
