package com.tareaandroid.plantcare.navigation

import kotlinx.serialization.Serializable

/** Destinos de la app (navegación type-safe con Kotlin Serialization). */

@Serializable
object LoginRoute

@Serializable
object RegisterRoute

@Serializable
object HomeRoute

/** Detalle de una planta: recibe su id como parámetro. */
@Serializable
data class PlantDetailRoute(val plantId: Long)

/** Alta o edición de una planta: [NEW_PLANT] indica una planta nueva. */
@Serializable
data class PlantEditRoute(val plantId: Long = NEW_PLANT) {
    companion object {
        const val NEW_PLANT = -1L
    }
}

@Serializable
object LightMeterRoute

@Serializable
object SettingsRoute
