package com.tareaandroid.plantcare.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.tareaandroid.plantcare.MainActivity
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.model.Plant
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/** Crea el canal de notificaciones y muestra el recordatorio de riego. */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /** Android 8+ exige un canal; el usuario puede configurarlo desde los ajustes del sistema. */
    fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = context.getString(R.string.notification_channel_description) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /** En Android 13+ hace falta el permiso POST_NOTIFICATIONS; antes basta con no tenerlas bloqueadas. */
    fun canNotify(): Boolean {
        val permissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        return permissionGranted && NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showWateringReminder(plants: List<Plant>) {
        if (plants.isEmpty() || !canNotify()) return

        // Al tocar la notificación se abre la app
        val openApp = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val names = plants.joinToString(", ") { it.name }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_water)
            .setContentTitle(
                context.resources.getQuantityString(R.plurals.notification_title, plants.size, plants.size),
            )
            .setContentText(names)
            .setStyle(NotificationCompat.BigTextStyle().bigText(names))
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(WATERING_NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // El usuario retiró el permiso justo antes de mostrarla: no se hace nada
        }
    }

    private companion object {
        const val CHANNEL_ID = "watering_reminders"
        const val WATERING_NOTIFICATION_ID = 1
    }
}
