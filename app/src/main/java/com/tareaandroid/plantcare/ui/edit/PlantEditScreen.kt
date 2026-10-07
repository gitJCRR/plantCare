package com.tareaandroid.plantcare.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.model.LightLevel
import com.tareaandroid.plantcare.ui.components.labelRes
import com.tareaandroid.plantcare.ui.theme.PlantCareTheme

@Composable
fun PlantEditScreen(
    onDone: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlantEditViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Cuando el ViewModel confirma que se ha guardado, se cierra la pantalla
    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onDone()
    }

    PlantEditContent(uiState = uiState, onEvent = viewModel::onEvent, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantEditContent(
    uiState: PlantEditUiState,
    onEvent: (PlantEditEvent) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (uiState.isNew) R.string.edit_title_new else R.string.edit_title_edit))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // En pantallas anchas el formulario no pasa de 600 dp y queda centrado
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.TopCenter,
            ) {
                PlantForm(uiState, onEvent, Modifier.widthIn(max = 600.dp).padding(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlantForm(uiState: PlantEditUiState, onEvent: (PlantEditEvent) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = { onEvent(PlantEditEvent.NameChanged(it)) },
            label = { Text(stringResource(R.string.edit_name)) },
            isError = uiState.nameError,
            supportingText = if (uiState.nameError) {
                { Text(stringResource(R.string.error_name_required)) }
            } else null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.species,
            onValueChange = { onEvent(PlantEditEvent.SpeciesChanged(it)) },
            label = { Text(stringResource(R.string.edit_species)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = uiState.location,
            onValueChange = { onEvent(PlantEditEvent.LocationChanged(it)) },
            label = { Text(stringResource(R.string.edit_location)) },
            placeholder = { Text(stringResource(R.string.edit_location_hint)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
        )
        DaysField(
            value = uiState.waterEveryDays,
            onValueChange = { onEvent(PlantEditEvent.WaterEveryDaysChanged(it)) },
            label = stringResource(R.string.edit_water_every),
            isError = uiState.waterError,
        )

        // Toda la fila es pulsable, no solo el interruptor
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.toggleable(
                value = uiState.needsFertilizer,
                role = Role.Switch,
                onValueChange = { onEvent(PlantEditEvent.NeedsFertilizerChanged(it)) },
            ),
        ) {
            Text(
                stringResource(R.string.edit_needs_fertilizer),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = uiState.needsFertilizer, onCheckedChange = null)
        }
        if (uiState.needsFertilizer) {
            DaysField(
                value = uiState.fertilizeEveryDays,
                onValueChange = { onEvent(PlantEditEvent.FertilizeEveryDaysChanged(it)) },
                label = stringResource(R.string.edit_fertilize_every),
                isError = uiState.fertilizeError,
            )
        }

        Text(stringResource(R.string.edit_light), style = MaterialTheme.typography.bodyLarge)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            LightLevel.entries.forEachIndexed { index, level ->
                SegmentedButton(
                    selected = uiState.lightLevel == level,
                    onClick = { onEvent(PlantEditEvent.LightLevelChanged(level)) },
                    shape = SegmentedButtonDefaults.itemShape(index, LightLevel.entries.size),
                ) {
                    Text(stringResource(level.labelRes))
                }
            }
        }

        OutlinedTextField(
            value = uiState.notes,
            onValueChange = { onEvent(PlantEditEvent.NotesChanged(it)) },
            label = { Text(stringResource(R.string.edit_notes)) },
            minLines = 3,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = { onEvent(PlantEditEvent.Save) },
            enabled = !uiState.isSaving,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (uiState.isSaving) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.action_save))
            }
        }
    }
}

@Composable
private fun DaysField(value: String, onValueChange: (String) -> Unit, label: String, isError: Boolean) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        suffix = { Text(stringResource(R.string.edit_days_suffix)) },
        isError = isError,
        supportingText = if (isError) {
            { Text(stringResource(R.string.error_days_invalid)) }
        } else null,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun PlantEditPreview() {
    PlantCareTheme {
        PlantEditContent(
            uiState = PlantEditUiState(isNew = true, name = "Monstera", needsFertilizer = true, nameError = false),
            onEvent = {},
            onBack = {},
        )
    }
}
