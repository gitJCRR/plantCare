package com.tareaandroid.plantcare.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tareaandroid.plantcare.data.local.entity.CareEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CareEventDao {

    /** Historial de una planta, del más reciente al más antiguo. */
    @Query("SELECT * FROM care_events WHERE plantId = :plantId ORDER BY date DESC, id DESC")
    fun observeEvents(plantId: Long): Flow<List<CareEventEntity>>

    @Insert
    suspend fun insert(event: CareEventEntity): Long
}
