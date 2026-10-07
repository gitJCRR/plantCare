package com.tareaandroid.plantcare.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tareaandroid.plantcare.model.CareType
import java.time.LocalDate

/**
 * Tabla `care_events`: historial de cuidados. Relación 1:N con [PlantEntity];
 * al borrar una planta se borran sus cuidados (CASCADE).
 */
@Entity(
    tableName = "care_events",
    foreignKeys = [
        ForeignKey(
            entity = PlantEntity::class,
            parentColumns = ["id"],
            childColumns = ["plantId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("plantId")],
)
data class CareEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val type: CareType,
    val date: LocalDate,
    val note: String = "",
)
