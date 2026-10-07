package com.tareaandroid.plantcare.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tareaandroid.plantcare.data.repository.PlantRepository
import com.tareaandroid.plantcare.model.Plant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/** Estado de la pantalla de inicio. */
sealed interface HomeUiState {
    data object Loading : HomeUiState

    /** @param plants plantas ordenadas: primero las que toca regar. */
    data class Success(val plants: List<Plant>, val today: LocalDate) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    repository: PlantRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = repository.observePlants()
        .map { plants ->
            val today = LocalDate.now()
            HomeUiState.Success(
                plants = plants.sortedBy { it.daysUntilWatering(today) },
                today = today,
            )
        }
        .stateIn(
            scope = viewModelScope,
            // Mantiene la suscripción 5 s tras salir de la pantalla (p. ej. al rotar)
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState.Loading,
        )
}
