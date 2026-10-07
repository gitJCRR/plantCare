package com.tareaandroid.plantcare.model

/**
 * Luz que necesita una planta, con el rango aproximado de lux recomendado.
 * Se usará también en el medidor de luz para comparar con la lectura del sensor.
 */
enum class LightLevel(val minLux: Int, val maxLux: Int) {
    LOW(minLux = 50, maxLux = 1_000),
    MEDIUM(minLux = 1_000, maxLux = 10_000),
    HIGH(minLux = 10_000, maxLux = 100_000),
}
