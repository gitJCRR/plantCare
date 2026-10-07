package com.tareaandroid.plantcare.data.repository

import androidx.room.withTransaction
import com.tareaandroid.plantcare.data.local.PlantCareDatabase
import com.tareaandroid.plantcare.data.local.entity.CareEventEntity
import com.tareaandroid.plantcare.data.local.toEntity
import com.tareaandroid.plantcare.data.local.toModel
import com.tareaandroid.plantcare.model.CareEvent
import com.tareaandroid.plantcare.model.CareType
import com.tareaandroid.plantcare.model.Plant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Implementación del repositorio sobre Room. */
@Singleton
class PlantRepositoryImpl @Inject constructor(
    private val database: PlantCareDatabase,
) : PlantRepository {

    private val plantDao = database.plantDao()
    private val careEventDao = database.careEventDao()

    // Provisional hasta la fase 5: se sustituirá por el uid del usuario de Firebase.
    private val currentUserId = LOCAL_USER

    override fun observePlants(): Flow<List<Plant>> =
        plantDao.observePlants(currentUserId).map { list -> list.map { it.toModel() } }

    override fun observePlant(id: Long): Flow<Plant?> =
        plantDao.observePlant(id).map { it?.toModel() }

    override fun observeCareEvents(plantId: Long): Flow<List<CareEvent>> =
        careEventDao.observeEvents(plantId).map { list -> list.map { it.toModel() } }

    override suspend fun getPlant(id: Long): Plant? = plantDao.getPlant(id)?.toModel()

    override suspend fun savePlant(plant: Plant): Long {
        val entity = plant.toEntity(currentUserId)
        return if (plant.id == 0L) {
            plantDao.insert(entity)
        } else {
            plantDao.update(entity)
            plant.id
        }
    }

    override suspend fun deletePlant(id: Long) = plantDao.deleteById(id)

    override suspend fun registerCare(plantId: Long, type: CareType, date: LocalDate) {
        // Transacción: el historial y la fecha de la planta se actualizan juntos o no se actualizan.
        database.withTransaction {
            careEventDao.insert(CareEventEntity(plantId = plantId, type = type, date = date))
            val plant = plantDao.getPlant(plantId) ?: return@withTransaction
            when (type) {
                CareType.WATER -> plantDao.update(plant.copy(lastWatered = date))
                CareType.FERTILIZE -> plantDao.update(plant.copy(lastFertilized = date))
                CareType.PRUNE, CareType.REPOT -> Unit
            }
        }
    }

    private companion object {
        const val LOCAL_USER = "local"
    }
}
