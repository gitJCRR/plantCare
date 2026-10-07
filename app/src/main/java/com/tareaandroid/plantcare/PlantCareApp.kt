package com.tareaandroid.plantcare

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.tareaandroid.plantcare.notifications.NotificationHelper
import com.tareaandroid.plantcare.notifications.ReminderScheduler
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Punto de entrada de la aplicación: inicializa el grafo de dependencias de Hilt y
 * configura WorkManager para que pueda inyectar dependencias en los Workers.
 */
@HiltAndroidApp
class PlantCareApp : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationHelper: NotificationHelper
    @Inject lateinit var reminderScheduler: ReminderScheduler

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        notificationHelper.createChannel()
        reminderScheduler.scheduleDaily()
    }
}
