package com.tareaandroid.plantcare.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tareaandroid.plantcare.data.repository.PlantRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

/**
 * Tarea en segundo plano que WorkManager ejecuta una vez al día: busca las plantas del
 * usuario que toca regar y, si hay alguna, muestra una notificación.
 * Se ejecuta aunque la app esté cerrada y sobrevive a reinicios del móvil.
 */
@HiltWorker
class WateringReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val plantRepository: PlantRepository,
    private val notificationHelper: NotificationHelper,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        // Sin sesión iniciada la lista está vacía y no se notifica nada
        val plantsToWater = plantRepository.observePlants().first().filter { it.needsWater(today) }
        notificationHelper.showWateringReminder(plantsToWater)
        return Result.success()
    }
}
