package com.tareaandroid.plantcare.ui.home

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.ui.components.PlaceholderScreen

@Composable
fun HomeScreen(onPlantClick: (Long) -> Unit, onAddPlant: () -> Unit) {
    PlaceholderScreen(title = stringResource(R.string.home_title)) {
        // Provisional: se sustituirá por la rejilla de plantas guardadas en Room
        (1L..3L).forEach { id ->
            OutlinedButton(onClick = { onPlantClick(id) }) {
                Text(stringResource(R.string.detail_title, id))
            }
        }
        Button(onClick = onAddPlant) { Text(stringResource(R.string.home_add_plant)) }
    }
}
