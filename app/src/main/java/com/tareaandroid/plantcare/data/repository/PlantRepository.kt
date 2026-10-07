package com.tareaandroid.plantcare.data.repository

import com.tareaandroid.plantcare.model.CareEvent
import com.tareaandroid.plantcare.model.CareType
import com.tareaandroid.plantcare.model.Plant
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * Único punto de acceso a los datos de plantas. Los ViewModels dependen de esta
 * interfaz y no saben si los datos vienen de Room, de la red o de una prueba.
 */
interface PlantRepository {

    /** Plantas del usuario actual; se actualiza sola cuando cambian los datos. */
    fun observePlants(): Flow<List<Plant>>

    fun observePlant(id: Long): Flow<Plant?>

    fun observeCareEvents(plantId: Long): Flow<List<CareEvent>>

    suspend fun getPlant(id: Long): Plant?

    /** Inserta la planta si es nueva (`id == 0`) o la actualiza. @return su id. */
    suspend fun savePlant(plant: Plant): Long

    suspend fun deletePlant(id: Long)

    /**
     * Registra un cuidado en el historial. Si es riego o abono, actualiza también
     * la fecha del último riego/abono de la planta.
     */
    suspend fun registerCare(plantId: Long, type: CareType, date: LocalDate = LocalDate.now())
}
