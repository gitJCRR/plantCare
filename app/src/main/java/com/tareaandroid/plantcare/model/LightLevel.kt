package com.tareaandroid.plantcare.model

/**
 * Luz que necesita una planta, con el rango aproximado de lux recomendado.
 * El medidor de luz compara la lectura del sensor con estos rangos.
 */
enum class LightLevel(val minLux: Int, val maxLux: Int) {
    LOW(minLux = 50, maxLux = 1_000),
    MEDIUM(minLux = 1_000, maxLux = 10_000),
    HIGH(minLux = 10_000, maxLux = 100_000);

    companion object {
        /**
         * Nivel de luz de una lectura del sensor, o `null` si hay demasiado poca luz
         * para cualquier planta (por debajo de [LOW]).
         */
        fun fromLux(lux: Float): LightLevel? = when {
            lux < LOW.minLux -> null
            lux < MEDIUM.minLux -> LOW
            lux < HIGH.minLux -> MEDIUM
            else -> HIGH
        }
    }
}
