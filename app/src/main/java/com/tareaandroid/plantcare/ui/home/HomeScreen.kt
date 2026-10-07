package com.tareaandroid.plantcare.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.model.Plant
import com.tareaandroid.plantcare.ui.theme.PlantCareTheme
import java.time.LocalDate

/** Punto de entrada de la pantalla: obtiene el ViewModel y observa su estado. */
@Composable
fun HomeScreen(
    onPlantClick: (Long) -> Unit,
    onAddPlant: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeContent(uiState = uiState, onPlantClick = onPlantClick, onAddPlant = onAddPlant)
}

/** Contenido sin estado propio: solo pinta [uiState] y emite eventos. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    uiState: HomeUiState,
    onPlantClick: (Long) -> Unit,
    onAddPlant: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_title)) }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPlant,
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.home_add_plant)) },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when (uiState) {
                HomeUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                is HomeUiState.Success ->
                    if (uiState.plants.isEmpty()) {
                        EmptyPlants(Modifier.align(Alignment.Center))
                    } else {
                        PlantGrid(uiState.plants, uiState.today, onPlantClick)
                    }
            }
        }
    }
}

/**
 * Rejilla adaptativa: cada columna mide al menos 160 dp, así que en un móvil
 * caben 2 y en una tablet o en horizontal caben más.
 */
@Composable
private fun PlantGrid(plants: List<Plant>, today: LocalDate, onPlantClick: (Long) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(plants, key = { it.id }) { plant ->
            PlantCard(plant = plant, today = today, onClick = { onPlantClick(plant.id) })
        }
    }
}

@Composable
private fun EmptyPlants(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.LocalFlorist,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(Modifier.height(16.dp))
        Text(
            stringResource(R.string.home_empty_title),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.home_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeContentPreview() {
    val today = LocalDate.of(2026, 10, 7)
    PlantCareTheme {
        HomeContent(
            uiState = HomeUiState.Success(
                plants = listOf(
                    Plant(id = 1, name = "Pothos", species = "Epipremnum aureum", waterEveryDays = 3, lastWatered = today.minusDays(4)),
                    Plant(id = 2, name = "Monstera", species = "Monstera deliciosa", waterEveryDays = 7, lastWatered = today.minusDays(2)),
                    Plant(id = 3, name = "Cactus", waterEveryDays = 14, lastWatered = today.minusDays(13)),
                ),
                today = today,
            ),
            onPlantClick = {},
            onAddPlant = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeEmptyPreview() {
    PlantCareTheme {
        HomeContent(uiState = HomeUiState.Success(emptyList(), LocalDate.now()), onPlantClick = {}, onAddPlant = {})
    }
}
