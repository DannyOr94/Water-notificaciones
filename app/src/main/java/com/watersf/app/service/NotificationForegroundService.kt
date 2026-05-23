package com.watersf.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import androidx.core.app.NotificationCompat
import com.watersf.app.MainActivity
import com.watersf.app.R
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.repository.NotificationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class NotificationForegroundService : Service() {

    @Inject
    lateinit var repository: NotificationRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 2002
        private const val FOREGROUND_CHANNEL_ID = "water_sf_foreground_channel"
        private const val ALERT_CHANNEL_ID = "water_sf_alerts_channel_v3"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        startForeground(FOREGROUND_NOTIFICATION_ID, buildForegroundNotification())
        
        // Iniciar polling de 10 segundos
        startPolling()
        
        // Escuchar flujo de nuevas notificaciones para emitir alerta flotante
        listenForNewNotifications()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    private fun startPolling() {
        serviceScope.launch {
            while (isActive) {
                try {
                    repository.syncNotifications()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                delay(10000) // Polling cada 10 segundos
            }
        }
    }

    private fun listenForNewNotifications() {
        serviceScope.launch {
            repository.newNotificationFlow.collect { notification ->
                playAlertSound()
                showHeadsUpNotification(notification)
            }
        }
    }

    private fun buildForegroundNotification(): android.app.Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_launcher)
            .setContentTitle("Water-SF activo")
            .setContentText("Buscando nuevas alertas en segundo plano...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun showHeadsUpNotification(notification: Notification) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_app_launcher)
            .setContentTitle(notification.title)
            .setContentText(notification.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notification.message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(notification.id.hashCode(), builder.build())
    }

    private fun playAlertSound() {
        try {
            val mediaPlayer = MediaPlayer.create(this, Settings.System.DEFAULT_NOTIFICATION_URI)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { mp ->
                mp.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Canal de servicio continuo (Prioridad baja)
            val fgChannel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                "Servicio en Segundo Plano",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene el servicio de alertas activo"
            }
            notificationManager.createNotificationChannel(fgChannel)

            // 2. Canal de alertas importantes (Heads-up)
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Alertas Water-SF",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Canal para alertas de cortes de agua, averías y facturación"
                enableLights(true)
                enableVibration(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(alertChannel)
        }
    }
}
