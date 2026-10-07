package com.tareaandroid.plantcare.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.tareaandroid.plantcare.data.repository.PlantRepository
import com.tareaandroid.plantcare.model.CareEvent
import com.tareaandroid.plantcare.model.CareType
import com.tareaandroid.plantcare.model.Plant
import com.tareaandroid.plantcare.navigation.PlantDetailRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed interface PlantDetailUiState {
    data object Loading : PlantDetailUiState
    data object NotFound : PlantDetailUiState
    data class Success(
        val plant: Plant,
        val history: List<CareEvent>,
        val today: LocalDate,
    ) : PlantDetailUiState
}

@HiltViewModel
class PlantDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: PlantRepository,
) : ViewModel() {

    private val plantId = savedStateHandle.toRoute<PlantDetailRoute>().plantId

    /** Combina la planta y su historial: cualquier cambio en Room actualiza la pantalla. */
    val uiState: StateFlow<PlantDetailUiState> = combine(
        repository.observePlant(plantId),
        repository.observeCareEvents(plantId),
    ) { plant, history ->
        if (plant == null) {
            PlantDetailUiState.NotFound
        } else {
            PlantDetailUiState.Success(plant, history, LocalDate.now())
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PlantDetailUiState.Loading)

    private val _deleted = MutableStateFlow(false)

    /** Pasa a `true` tras borrar la planta, para que la pantalla se cierre. */
    val deleted: StateFlow<Boolean> = _deleted.asStateFlow()

    fun registerCare(type: CareType) {
        viewModelScope.launch { repository.registerCare(plantId, type) }
    }

    fun deletePlant() {
        viewModelScope.launch {
            repository.deletePlant(plantId)
            _deleted.value = true
        }
    }
}
