package com.tareaandroid.plantcare.model

import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Planta tal y como la usa la app (modelo de dominio). Es independiente de Room:
 * la capa de datos la convierte desde/hacia `PlantEntity`.
 */
data class Plant(
    val id: Long = 0,
    val name: String,
    val species: String = "",
    val location: String = "",
    val photoUri: String? = null,
    val waterEveryDays: Int,
    val lastWatered: LocalDate? = null,
    val fertilizeEveryDays: Int? = null,
    val lastFertilized: LocalDate? = null,
    val lightLevel: LightLevel = LightLevel.MEDIUM,
    val notes: String = "",
) {
    /** Fecha del próximo riego, o `null` si nunca se ha regado. */
    val nextWatering: LocalDate?
        get() = lastWatered?.plusDays(waterEveryDays.toLong())

    /** Fecha del próximo abono, o `null` si no se abona o nunca se ha abonado. */
    val nextFertilizing: LocalDate?
        get() = fertilizeEveryDays?.let { days -> lastFertilized?.plusDays(days.toLong()) }

    /** Días que faltan para regar; 0 o negativo significa que toca hoy o va con retraso. */
    fun daysUntilWatering(today: LocalDate): Long =
        nextWatering?.let { ChronoUnit.DAYS.between(today, it) } ?: 0

    fun needsWater(today: LocalDate): Boolean = daysUntilWatering(today) <= 0
}
