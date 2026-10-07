package com.tareaandroid.plantcare.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.tareaandroid.plantcare.model.LightLevel
import java.time.LocalDate

/** Tabla `plants`: una fila por planta del usuario. */
@Entity(
    tableName = "plants",
    indices = [Index("userId")],
)
data class PlantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Propietario de la planta (uid de Firebase a partir de la fase 5). */
    val userId: String,
    val name: String,
    val species: String,
    val location: String,
    /** Ruta de la foto tomada con la cámara, o `null` si no tiene. */
    val photoUri: String?,
    val waterEveryDays: Int,
    val lastWatered: LocalDate?,
    /** Cada cuántos días se abona, o `null` si no necesita abono. */
    val fertilizeEveryDays: Int?,
    val lastFertilized: LocalDate?,
    val lightLevel: LightLevel,
    val notes: String,
)
