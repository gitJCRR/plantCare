package com.tareaandroid.plantcare.notifications

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Programa con WorkManager la comprobación diaria de riego. */
@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Programa (o reprograma) el recordatorio diario a la hora indicada. */
    fun scheduleDaily(time: LocalTime = DEFAULT_TIME) {
        val request = PeriodicWorkRequestBuilder<WateringReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delayUntil(time, LocalDateTime.now()).toMinutes(), TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_WORK_NAME,
            // UPDATE: si ya estaba programado, se actualiza en lugar de duplicarlo
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    /** Lanza la comprobación ahora mismo (para probar los avisos). */
    fun runNow() {
        WorkManager.getInstance(context).enqueue(OneTimeWorkRequestBuilder<WateringReminderWorker>().build())
    }

    companion object {
        val DEFAULT_TIME: LocalTime = LocalTime.of(9, 0)
        private const val DAILY_WORK_NAME = "daily_watering_reminder"

        /** Tiempo que falta desde [now] hasta la próxima vez que el reloj marque [time]. */
        fun delayUntil(time: LocalTime, now: LocalDateTime): Duration {
            var next = now.toLocalDate().atTime(time)
            if (!next.isAfter(now)) next = next.plusDays(1)
            return Duration.between(now, next)
        }
    }
}
