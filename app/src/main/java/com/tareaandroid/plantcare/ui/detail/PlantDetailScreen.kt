package com.tareaandroid.plantcare.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Grass
import androidx.compose.material.icons.outlined.LocalFlorist
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.model.CareEvent
import com.tareaandroid.plantcare.model.CareType
import com.tareaandroid.plantcare.model.LightLevel
import com.tareaandroid.plantcare.model.Plant
import com.tareaandroid.plantcare.ui.components.labelRes
import com.tareaandroid.plantcare.ui.theme.PlantCareTheme
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun PlantDetailScreen(
    onEdit: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlantDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val deleted by viewModel.deleted.collectAsStateWithLifecycle()

    LaunchedEffect(deleted) {
        if (deleted) onBack()
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    PlantDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onCare = { type ->
            viewModel.registerCare(type)
            val message = context.getString(R.string.detail_care_registered, context.getString(type.labelRes))
            scope.launch { snackbarHostState.showSnackbar(message) }
        },
        onEdit = onEdit,
        onDelete = viewModel::deletePlant,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailContent(
    uiState: PlantDetailUiState,
    snackbarHostState: SnackbarHostState,
    onCare: (CareType) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
) {
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text((uiState as? PlantDetailUiState.Success)?.plant?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
                actions = {
                    if (uiState is PlantDetailUiState.Success) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.detail_edit))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.detail_delete))
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                PlantDetailUiState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                PlantDetailUiState.NotFound -> Text(
                    stringResource(R.string.detail_not_found),
                    modifier = Modifier.align(Alignment.Center),
                )
                is PlantDetailUiState.Success -> PlantDetailBody(uiState, onCare)
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.detail_delete_title)) },
            text = { Text(stringResource(R.string.detail_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDelete()
                }) { Text(stringResource(R.string.detail_delete)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text(stringResource(R.string.action_cancel)) }
            },
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlantDetailBody(state: PlantDetailUiState.Success, onCare: (CareType) -> Unit) {
    val plant = state.plant
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    // En tablet el contenido no pasa de 720 dp y queda centrado
    LazyColumn(
        modifier = Modifier.widthIn(max = 720.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.LocalFlorist,
                    contentDescription = null,
                    modifier = Modifier.size(72.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
        item {
            Column {
                Text(plant.name, style = MaterialTheme.typography.headlineMedium)
                if (plant.species.isNotBlank()) {
                    Text(
                        plant.species,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { onCare(CareType.WATER) }) {
                    ButtonContent(Icons.Outlined.WaterDrop, stringResource(R.string.detail_action_water))
                }
                if (plant.fertilizeEveryDays != null) {
                    FilledTonalButton(onClick = { onCare(CareType.FERTILIZE) }) {
                        ButtonContent(Icons.Outlined.Science, stringResource(R.string.detail_action_fertilize))
                    }
                }
                OutlinedButton(onClick = { onCare(CareType.PRUNE) }) {
                    ButtonContent(Icons.Outlined.ContentCut, stringResource(R.string.detail_action_prune))
                }
                OutlinedButton(onClick = { onCare(CareType.REPOT) }) {
                    ButtonContent(Icons.Outlined.Grass, stringResource(R.string.detail_action_repot))
                }
            }
        }
        item {
            Card {
                InfoRow(
                    icon = Icons.Outlined.WaterDrop,
                    title = stringResource(R.string.detail_watering, plant.waterEveryDays),
                    subtitle = wateringStatus(plant, state.today, dateFormatter),
                )
                plant.fertilizeEveryDays?.let { days ->
                    InfoRow(
                        icon = Icons.Outlined.Science,
                        title = stringResource(R.string.detail_fertilizing, days),
                        subtitle = plant.nextFertilizing?.let {
                            stringResource(R.string.detail_next_date, it.format(dateFormatter))
                        },
                    )
                }
                InfoRow(
                    icon = Icons.Outlined.WbSunny,
                    title = stringResource(R.string.detail_light, stringResource(plant.lightLevel.labelRes)),
                    subtitle = stringResource(R.string.detail_light_range, plant.lightLevel.minLux, plant.lightLevel.maxLux),
                )
                if (plant.location.isNotBlank()) {
                    InfoRow(icon = Icons.Outlined.Place, title = plant.location)
                }
                if (plant.notes.isNotBlank()) {
                    InfoRow(icon = Icons.Outlined.Notes, title = plant.notes)
                }
            }
        }
        item {
            Text(
                stringResource(R.string.detail_history),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        if (state.history.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.detail_history_empty),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            items(state.history, key = { it.id }) { event ->
                ListItem(
                    headlineContent = { Text(stringResource(event.type.labelRes)) },
                    supportingContent = { Text(event.date.format(dateFormatter)) },
                    leadingContent = { Icon(event.type.icon, contentDescription = null) },
                )
            }
        }
    }
}

@Composable
private fun wateringStatus(plant: Plant, today: LocalDate, formatter: DateTimeFormatter): String {
    val days = plant.daysUntilWatering(today)
    val next = plant.nextWatering ?: return stringResource(R.string.watering_due)
    val relative = if (days <= 0) {
        stringResource(R.string.watering_due)
    } else {
        pluralStringResource(R.plurals.watering_in_days, days.toInt(), days.toInt())
    }
    return "$relative · ${next.format(formatter)}"
}

@Composable
private fun ButtonContent(icon: ImageVector, text: String) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
    Text(text, modifier = Modifier.padding(start = 8.dp))
}

@Composable
private fun InfoRow(icon: ImageVector, title: String, subtitle: String? = null) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
    )
}

private val CareType.icon: ImageVector
    get() = when (this) {
        CareType.WATER -> Icons.Outlined.WaterDrop
        CareType.FERTILIZE -> Icons.Outlined.Science
        CareType.PRUNE -> Icons.Outlined.ContentCut
        CareType.REPOT -> Icons.Outlined.Grass
    }

@Preview(showBackground = true)
@Composable
private fun PlantDetailPreview() {
    val today = LocalDate.of(2026, 10, 7)
    PlantCareTheme {
        PlantDetailContent(
            uiState = PlantDetailUiState.Success(
                plant = Plant(
                    id = 1,
                    name = "Monstera",
                    species = "Monstera deliciosa",
                    location = "Salón",
                    waterEveryDays = 7,
                    lastWatered = today.minusDays(2),
                    fertilizeEveryDays = 30,
                    lastFertilized = today.minusDays(10),
                    lightLevel = LightLevel.MEDIUM,
                ),
                history = listOf(
                    CareEvent(id = 2, plantId = 1, type = CareType.WATER, date = today.minusDays(2)),
                    CareEvent(id = 1, plantId = 1, type = CareType.FERTILIZE, date = today.minusDays(10)),
                ),
                today = today,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onCare = {},
            onEdit = {},
            onDelete = {},
            onBack = {},
        )
    }
}
