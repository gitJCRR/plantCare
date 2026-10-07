package com.tareaandroid.plantcare.data.sensor

import kotlinx.coroutines.flow.Flow

/** Fuente de lecturas del sensor de luz ambiental. */
interface LightSensor {

    /** `false` si el dispositivo no tiene sensor de luz. */
    val isAvailable: Boolean

    /** Lecturas en lux mientras haya alguien suscrito; al dejar de observar se apaga el sensor. */
    fun readings(): Flow<Float>
}
