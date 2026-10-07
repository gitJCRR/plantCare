package com.tareaandroid.plantcare.ui.light

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.model.LightLevel
import com.tareaandroid.plantcare.model.Plant
import com.tareaandroid.plantcare.ui.components.labelRes
import com.tareaandroid.plantcare.ui.theme.PlantCareTheme
import kotlin.math.log10
import kotlin.math.roundToInt

@Composable
fun LightMeterScreen(viewModel: LightMeterViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LightMeterContent(uiState)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LightMeterContent(uiState: LightMeterUiState) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.light_title)) }) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            if (!uiState.sensorAvailable) {
                Text(
                    stringResource(R.string.light_no_sensor),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(32.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.widthIn(max = 720.dp),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item { LuxCard(uiState.lux, uiState.level) }
                    item {
                        Text(
                            stringResource(R.string.light_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (uiState.plants.isNotEmpty()) {
                        item {
                            Text(
                                stringResource(R.string.light_plants_title),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        items(uiState.plants, key = { it.plant.id }) { PlantFitRow(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LuxCard(lux: Float?, level: LightLevel?) {
    // Escala logarítmica: de 1 a 100.000 lux ocupa la barra completa
    val fraction = lux?.let { (log10(it + 1f) / 5f).coerceIn(0f, 1f) } ?: 0f
    val animatedFraction by animateFloatAsState(fraction, label = "lux")

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                if (level == null) Icons.Outlined.DarkMode else Icons.Outlined.WbSunny,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
            )
            Text(
                text = lux?.let { stringResource(R.string.light_lux_value, it.roundToInt()) }
                    ?: stringResource(R.string.light_waiting),
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                text = when {
                    lux == null -> ""
                    level == null -> stringResource(R.string.light_level_too_dark)
                    else -> stringResource(R.string.light_level_measured, stringResource(level.labelRes).lowercase())
                },
                style = MaterialTheme.typography.titleMedium,
            )
            LinearProgressIndicator(
                progress = { animatedFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
            )
        }
    }
}

@Composable
private fun PlantFitRow(item: PlantLightFit) {
    val (icon: ImageVector, text: Int) = when (item.fit) {
        LightFit.GOOD -> Icons.Outlined.CheckCircle to R.string.light_fit_good
        LightFit.NEEDS_MORE -> Icons.Outlined.WbSunny to R.string.light_fit_more
        LightFit.NEEDS_LESS -> Icons.Outlined.DarkMode to R.string.light_fit_less
    }
    val color = if (item.fit == LightFit.GOOD) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    ListItem(
        headlineContent = { Text(item.plant.name) },
        supportingContent = {
            Text(stringResource(R.string.light_plant_needs, stringResource(item.plant.lightLevel.labelRes).lowercase()))
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = color)
                Text(stringResource(text), style = MaterialTheme.typography.labelSmall, color = color)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun LightMeterPreview() {
    PlantCareTheme {
        LightMeterContent(
            LightMeterUiState(
                lux = 2_350f,
                level = LightLevel.MEDIUM,
                plants = listOf(
                    PlantLightFit(Plant(id = 1, name = "Pothos", waterEveryDays = 3), LightFit.GOOD),
                    PlantLightFit(Plant(id = 2, name = "Cactus", waterEveryDays = 14, lightLevel = LightLevel.HIGH), LightFit.NEEDS_MORE),
                ),
            ),
        )
    }
}
