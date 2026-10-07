package com.tareaandroid.plantcare.ui.light

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tareaandroid.plantcare.data.repository.PlantRepository
import com.tareaandroid.plantcare.data.sensor.LightSensor
import com.tareaandroid.plantcare.model.LightLevel
import com.tareaandroid.plantcare.model.Plant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/** Cómo encaja la luz medida con lo que necesita una planta. */
enum class LightFit { GOOD, NEEDS_MORE, NEEDS_LESS }

data class PlantLightFit(val plant: Plant, val fit: LightFit)

data class LightMeterUiState(
    val sensorAvailable: Boolean = true,
    /** Última lectura en lux; `null` mientras llega la primera. */
    val lux: Float? = null,
    /** Nivel de la lectura; `null` si hay demasiado poca luz para cualquier planta. */
    val level: LightLevel? = null,
    val plants: List<PlantLightFit> = emptyList(),
)

@HiltViewModel
class LightMeterViewModel @Inject constructor(
    lightSensor: LightSensor,
    plantRepository: PlantRepository,
) : ViewModel() {

    private val luxReadings = if (lightSensor.isAvailable) {
        lightSensor.readings().onStart<Float?> { emit(null) }
    } else {
        flowOf(null)
    }

    val uiState: StateFlow<LightMeterUiState> =
        combine(luxReadings, plantRepository.observePlants()) { lux, plants ->
            val level = lux?.let(LightLevel::fromLux)
            LightMeterUiState(
                sensorAvailable = lightSensor.isAvailable,
                lux = lux,
                level = level,
                plants = if (lux == null) emptyList() else plants.map { PlantLightFit(it, fitFor(it.lightLevel, level)) },
            )
        }.stateIn(
            viewModelScope,
            // Al salir de la pantalla se deja de escuchar el sensor (ahorro de batería)
            SharingStarted.WhileSubscribed(5_000),
            LightMeterUiState(sensorAvailable = lightSensor.isAvailable),
        )

    companion object {
        /** Compara la luz que necesita la planta con la medida (`null` = demasiado oscuro). */
        fun fitFor(needed: LightLevel, measured: LightLevel?): LightFit = when {
            measured == null || measured < needed -> LightFit.NEEDS_MORE
            measured > needed -> LightFit.NEEDS_LESS
            else -> LightFit.GOOD
        }
    }
}
