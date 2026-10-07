package com.tareaandroid.plantcare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.tareaandroid.plantcare.data.local.entity.PlantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlantDao {

    /** Plantas del usuario ordenadas por nombre; emite de nuevo cada vez que cambia la tabla. */
    @Query("SELECT * FROM plants WHERE userId = :userId ORDER BY name COLLATE NOCASE")
    fun observePlants(userId: String): Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE id = :id")
    fun observePlant(id: Long): Flow<PlantEntity?>

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlant(id: Long): PlantEntity?

    /** @return el id generado para la nueva planta. */
    @Insert
    suspend fun insert(plant: PlantEntity): Long

    @Update
    suspend fun update(plant: PlantEntity)

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Cambia el propietario de todas las plantas de [fromUserId] a [toUserId]. */
    @Query("UPDATE plants SET userId = :toUserId WHERE userId = :fromUserId")
    suspend fun reassignOwner(fromUserId: String, toUserId: String)
}
