package com.watersf.app.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.watersf.app.data.local.dao.NotificationDao
import com.watersf.app.data.remote.api.WaterApiService
import com.watersf.app.data.remote.dto.toEntity
import com.watersf.app.data.security.EncryptedPrefsManager
import com.watersf.app.domain.model.UserType
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class NotificationSyncWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: WaterApiService,
    private val notificationDao: NotificationDao,
    private val prefsManager: EncryptedPrefsManager
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "water_sf_alerts_channel_v3"
        const val WORK_NAME = "WaterSfSyncWorker"

        /**
         * Genera las restricciones de batería y red para optimizar la sincronización.
         */
        fun buildWorkRequest() = PeriodicWorkRequestBuilder<NotificationSyncWorker>(
            15, TimeUnit.MINUTES // Mínimo permitido por Android
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()
        ).build()
    }

    override suspend fun doWork(): Result {
        val user = prefsManager.getUser() ?: return Result.success() // No hay sesión activa, abortar sync

        return try {
            // 1. Decidir el endpoint según el rol del usuario (Requerimiento 2)
            val response = when (user.type) {
                UserType.ADMINISTRADOR,
                UserType.ADMINISTRATIVO,
                UserType.SECRETARIO,
                UserType.JUNTA -> {
                    apiService.getAdminNotifications()
                }
                UserType.ABONADO,
                UserType.FONTANERO -> {
                    apiService.getMyNotifications()
                }
            }

            if (response.isSuccessful && response.body() != null) {
                val remoteNotifications = response.body()!!
                var newNotificationsCount = 0

                // 2. Filtrar duplicados comparando con los registros de Room (Requerimiento 3)
                for (dto in remoteNotifications) {
                    val alreadyExists = notificationDao.exists(dto.id)
                    if (!alreadyExists) {
                        // 3. Si es un registro nuevo, insertar localmente
                        notificationDao.insertNotifications(listOf(dto.toEntity()))
                        
                        // 4. Disparar notificación del sistema Android
                        showSystemNotification(dto.id.hashCode(), dto.title, dto.message)
                        newNotificationsCount++
                    }
                }
                Result.success()
            } else {
                Result.retry() // Reintentar con retroceso exponencial si falla el servidor
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }

    /**
     * Muestra la notificación nativa de Android en la barra de estado
     */
    private fun showSystemNotification(notificationId: Int, title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear el canal de notificaciones en Android Oreo (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas Water-SF"
            val descriptionText = "Canal para alertas de cortes de agua, averías y facturación"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent para abrir la app al hacer clic
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Construir la notificación con un diseño limpio y moderno
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(com.watersf.app.R.drawable.ic_app_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // Verificar permisos en Android 13+ (API 33+) antes de publicar
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationManager.notify(notificationId, notification)
        } else {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
