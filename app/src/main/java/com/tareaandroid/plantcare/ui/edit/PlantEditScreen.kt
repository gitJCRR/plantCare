package com.tareaandroid.plantcare.ui.edit

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.components.PlaceholderScreen

/** @param plantId id de la planta a editar, o `null` para crear una nueva. */
@Composable
fun PlantEditScreen(plantId: Long?, onDone: () -> Unit) {
    val title = if (plantId == null) {
        stringResource(R.string.edit_title_new)
    } else {
        stringResource(R.string.edit_title_edit, plantId)
    }
    PlaceholderScreen(title = title) {
        Button(onClick = onDone) { Text(stringResource(R.string.action_save)) }
    }
}
