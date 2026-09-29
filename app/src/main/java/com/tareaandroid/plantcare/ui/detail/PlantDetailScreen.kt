package com.tareaandroid.plantcare.ui.detail

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.components.PlaceholderScreen

@Composable
fun PlantDetailScreen(plantId: Long, onEdit: () -> Unit, onBack: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.detail_title, plantId)) {
        Button(onClick = onEdit) { Text(stringResource(R.string.detail_edit)) }
        TextButton(onClick = onBack) { Text(stringResource(R.string.action_back)) }
    }
}
