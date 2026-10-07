package com.tareaandroid.plantcare.data.repository

import androidx.room.withTransaction
import com.tareaandroid.plantcare.data.auth.AuthRepository
import com.tareaandroid.plantcare.data.local.PlantCareDatabase
import com.tareaandroid.plantcare.data.local.entity.CareEventEntity
import com.tareaandroid.plantcare.data.local.toEntity
import com.tareaandroid.plantcare.data.local.toModel
import com.tareaandroid.plantcare.data.photo.PhotoStorage
import com.tareaandroid.plantcare.model.CareEvent
import com.tareaandroid.plantcare.model.CareType
import com.tareaandroid.plantcare.model.Plant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/** Implementación del repositorio sobre Room. Cada usuario solo ve y modifica sus plantas. */
@Singleton
class PlantRepositoryImpl @Inject constructor(
    private val database: PlantCareDatabase,
    private val authRepository: AuthRepository,
    private val photoStorage: PhotoStorage,
) : PlantRepository {

    private val plantDao = database.plantDao()
    private val careEventDao = database.careEventDao()

    private fun currentUserIdOrNull(): String? = authRepository.currentUserOrNull()?.uid

    private fun requireUserId(): String =
        checkNotNull(currentUserIdOrNull()) { "Operación sobre plantas sin sesión iniciada" }

    /** Cambia de lista automáticamente cuando otro usuario inicia sesión (flatMapLatest). */
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observePlants(): Flow<List<Plant>> =
        authRepository.currentUser.flatMapLatest { user ->
            if (user == null) {
                flowOf(emptyList())
            } else {
                flow {
                    // Las plantas creadas antes de existir el login pasan al primer usuario que entra
                    plantDao.reassignOwner(fromUserId = LEGACY_LOCAL_USER, toUserId = user.uid)
                    emitAll(plantDao.observePlants(user.uid).map { list -> list.map { it.toModel() } })
                }
            }
        }

    override fun observePlant(id: Long): Flow<Plant?> =
        plantDao.observePlant(id).map { entity -> entity?.takeIf { it.userId == currentUserIdOrNull() }?.toModel() }

    override fun observeCareEvents(plantId: Long): Flow<List<CareEvent>> =
        careEventDao.observeEvents(plantId).map { list -> list.map { it.toModel() } }

    override suspend fun getPlant(id: Long): Plant? =
        plantDao.getPlant(id)?.takeIf { it.userId == currentUserIdOrNull() }?.toModel()

    override suspend fun savePlant(plant: Plant): Long {
        val entity = plant.toEntity(requireUserId())
        return if (plant.id == 0L) {
            plantDao.insert(entity)
        } else {
            plantDao.update(entity)
            plant.id
        }
    }

    override suspend fun deletePlant(id: Long) {
        val plant = getPlant(id) ?: return
        plantDao.deleteById(id)
        photoStorage.delete(plant.photoUri)
    }

    override suspend fun registerCare(plantId: Long, type: CareType, date: LocalDate) {
        // Transacción: el historial y la fecha de la planta se actualizan juntos o no se actualizan.
        database.withTransaction {
            val plant = plantDao.getPlant(plantId)?.takeIf { it.userId == requireUserId() }
                ?: return@withTransaction
            careEventDao.insert(CareEventEntity(plantId = plantId, type = type, date = date))
            when (type) {
                CareType.WATER -> plantDao.update(plant.copy(lastWatered = date))
                CareType.FERTILIZE -> plantDao.update(plant.copy(lastFertilized = date))
                CareType.PRUNE, CareType.REPOT -> Unit
            }
        }
    }

    private companion object {
        /** Propietario usado antes de la fase 5, cuando aún no había login. */
        const val LEGACY_LOCAL_USER = "local"
    }
}
