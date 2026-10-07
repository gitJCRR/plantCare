package com.tareaandroid.plantcare.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Implementación con el [SensorManager] de Android (sensor `TYPE_LIGHT`). */
@Singleton
class AndroidLightSensor @Inject constructor(
    @ApplicationContext context: Context,
) : LightSensor {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    override val isAvailable: Boolean = sensor != null

    override fun readings(): Flow<Float> {
        val lightSensor = sensor ?: return emptyFlow()
        return callbackFlow {
            val listener = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent) {
                    trySend(event.values[0])
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
            }
            sensorManager.registerListener(listener, lightSensor, SensorManager.SENSOR_DELAY_UI)
            // Se desregistra al cancelar el Flow para no gastar batería en segundo plano
            awaitClose { sensorManager.unregisterListener(listener) }
        }
    }
}
