package com.tareaandroid.plantcare.ui.edit

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tareaandroid.plantcare.data.photo.PhotoFile
import com.tareaandroid.plantcare.data.photo.PhotoStorage
import com.tareaandroid.plantcare.data.repository.PlantRepository
import com.tareaandroid.plantcare.model.LightLevel
import com.tareaandroid.plantcare.model.Plant
import com.tareaandroid.plantcare.navigation.PlantEditRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/** Estado del formulario. Los números se guardan como texto tal y como los escribe el usuario. */
data class PlantEditUiState(
    val isNew: Boolean,
    val isLoading: Boolean = false,
    val name: String = "",
    val species: String = "",
    val location: String = "",
    val waterEveryDays: String = "7",
    val needsFertilizer: Boolean = false,
    val fertilizeEveryDays: String = "30",
    val lightLevel: LightLevel = LightLevel.MEDIUM,
    val notes: String = "",
    /** Ruta de la foto en el almacenamiento de la app, o `null` si no tiene. */
    val photoPath: String? = null,
    val nameError: Boolean = false,
    val waterError: Boolean = false,
    val fertilizeError: Boolean = false,
    val isSaving: Boolean = false,
    /** Pasa a `true` cuando la planta se ha guardado y hay que cerrar la pantalla. */
    val saved: Boolean = false,
)

/** Acciones que la pantalla envía al ViewModel. */
sealed interface PlantEditEvent {
    data class NameChanged(val value: String) : PlantEditEvent
    data class SpeciesChanged(val value: String) : PlantEditEvent
    data class LocationChanged(val value: String) : PlantEditEvent
    data class WaterEveryDaysChanged(val value: String) : PlantEditEvent
    data class NeedsFertilizerChanged(val value: Boolean) : PlantEditEvent
    data class FertilizeEveryDaysChanged(val value: String) : PlantEditEvent
    data class LightLevelChanged(val value: LightLevel) : PlantEditEvent
    data class NotesChanged(val value: String) : PlantEditEvent
    /** La cámara ha guardado la foto en [path] (archivo creado con [PlantEditViewModel.createPhotoFile]). */
    data class PhotoTaken(val path: String) : PlantEditEvent
    data class PhotoPickedFromGallery(val uri: Uri) : PlantEditEvent
    data object PhotoRemoved : PlantEditEvent
    data object Save : PlantEditEvent
}

@HiltViewModel
class PlantEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PlantRepository,
    private val photoStorage: PhotoStorage,
) : ViewModel() {

    // El parámetro de navegación llega en el SavedStateHandle y se lee de forma type-safe
    private val plantId: Long? = savedStateHandle.toRoute<PlantEditRoute>().plantId
        .takeIf { it != PlantEditRoute.NEW_PLANT }

    /** Planta original al editar, para conservar los campos que el formulario no muestra. */
    private var original: Plant? = null

    /** Fotos creadas en esta pantalla; las que no se guarden se borran al salir. */
    private val newPhotos = mutableListOf<String>()

    private val _uiState = MutableStateFlow(PlantEditUiState(isNew = plantId == null, isLoading = plantId != null))
    val uiState: StateFlow<PlantEditUiState> = _uiState.asStateFlow()

    init {
        if (plantId != null) loadPlant(plantId)
    }

    private fun loadPlant(id: Long) = viewModelScope.launch {
        val plant = repository.getPlant(id)
        original = plant
        _uiState.update { state ->
            if (plant == null) {
                state.copy(isLoading = false)
            } else {
                state.copy(
                    isLoading = false,
                    name = plant.name,
                    species = plant.species,
                    location = plant.location,
                    waterEveryDays = plant.waterEveryDays.toString(),
                    needsFertilizer = plant.fertilizeEveryDays != null,
                    fertilizeEveryDays = (plant.fertilizeEveryDays ?: 30).toString(),
                    lightLevel = plant.lightLevel,
                    notes = plant.notes,
                    photoPath = plant.photoUri,
                )
            }
        }
    }

    fun onEvent(event: PlantEditEvent) {
        when (event) {
            is PlantEditEvent.NameChanged -> _uiState.update { it.copy(name = event.value, nameError = false) }
            is PlantEditEvent.SpeciesChanged -> _uiState.update { it.copy(species = event.value) }
            is PlantEditEvent.LocationChanged -> _uiState.update { it.copy(location = event.value) }
            is PlantEditEvent.WaterEveryDaysChanged ->
                _uiState.update { it.copy(waterEveryDays = event.value.onlyDigits(), waterError = false) }
            is PlantEditEvent.NeedsFertilizerChanged ->
                _uiState.update { it.copy(needsFertilizer = event.value, fertilizeError = false) }
            is PlantEditEvent.FertilizeEveryDaysChanged ->
                _uiState.update { it.copy(fertilizeEveryDays = event.value.onlyDigits(), fertilizeError = false) }
            is PlantEditEvent.LightLevelChanged -> _uiState.update { it.copy(lightLevel = event.value) }
            is PlantEditEvent.NotesChanged -> _uiState.update { it.copy(notes = event.value) }
            is PlantEditEvent.PhotoTaken -> _uiState.update { it.copy(photoPath = event.path) }
            is PlantEditEvent.PhotoPickedFromGallery -> viewModelScope.launch {
                photoStorage.importFromGallery(event.uri)?.let { path ->
                    newPhotos += path
                    _uiState.update { it.copy(photoPath = path) }
                }
            }
            PlantEditEvent.PhotoRemoved -> _uiState.update { it.copy(photoPath = null) }
            PlantEditEvent.Save -> save()
        }
    }

    private fun save() {
        val state = _uiState.value
        if (state.isSaving) return

        val waterDays = state.waterEveryDays.toValidDays()
        val fertilizeDays = state.fertilizeEveryDays.toValidDays()
        val nameError = state.name.isBlank()
        val waterError = waterDays == null
        val fertilizeError = state.needsFertilizer && fertilizeDays == null
        if (nameError || waterError || fertilizeError) {
            _uiState.update { it.copy(nameError = nameError, waterError = waterError, fertilizeError = fertilizeError) }
            return
        }

        val today = LocalDate.now()
        val base = original ?: Plant(name = "", waterEveryDays = 1, lastWatered = today)
        val plant = base.copy(
            name = state.name.trim(),
            species = state.species.trim(),
            location = state.location.trim(),
            waterEveryDays = waterDays!!,
            fertilizeEveryDays = if (state.needsFertilizer) fertilizeDays else null,
            // Al activar el abono por primera vez se toma hoy como fecha de referencia
            lastFertilized = if (state.needsFertilizer) base.lastFertilized ?: today else base.lastFertilized,
            lightLevel = state.lightLevel,
            notes = state.notes.trim(),
            photoUri = state.photoPath,
        )

        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            repository.savePlant(plant)
            // La foto guardada deja de ser «nueva»; si se ha sustituido la anterior, se borra
            newPhotos.remove(state.photoPath)
            if (base.photoUri != state.photoPath) photoStorage.delete(base.photoUri)
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }

    /** Crea el archivo donde la app de cámara guardará la foto. */
    fun createPhotoFile(): PhotoFile = photoStorage.createPhotoFile().also { newPhotos += it.path }

    /** Si se sale sin guardar, se borran las fotos hechas en esta pantalla. */
    override fun onCleared() {
        newPhotos.forEach(photoStorage::delete)
    }

    private fun String.onlyDigits() = filter(Char::isDigit).take(3)

    private fun String.toValidDays(): Int? = toIntOrNull()?.takeIf { it in 1..365 }
}
