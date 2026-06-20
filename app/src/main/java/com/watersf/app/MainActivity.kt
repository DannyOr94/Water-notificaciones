package com.watersf.app

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.watersf.app.data.security.EncryptedPrefsManager
import com.watersf.app.presentation.auth.LoginScreen
import com.watersf.app.presentation.notification.NotificationListScreen
import com.watersf.app.presentation.theme.WaterSfTheme
import com.watersf.app.service.NotificationForegroundService
import com.watersf.app.worker.NotificationSyncWorker
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsManager: EncryptedPrefsManager

    @Inject
    lateinit var notificationRepository: com.watersf.app.domain.repository.NotificationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Solicitar permisos de notificación a nivel de S.O. (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 101)
            }
        }

        // 2. Inicializar y programar el trabajador periódico de sincronización de fondo
        try {
            val workRequest = NotificationSyncWorker.buildWorkRequest()
            WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
                NotificationSyncWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            WaterSfTheme {
                var isLoggedIn by remember { mutableStateOf(prefsManager.getToken() != null) }

                LaunchedEffect(isLoggedIn) {
                    val serviceIntent = Intent(this@MainActivity, NotificationForegroundService::class.java)
                    if (isLoggedIn) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            startForegroundService(serviceIntent)
                        } else {
                            startService(serviceIntent)
                        }
                    } else {
                        stopService(serviceIntent)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isLoggedIn) {
                        NotificationListScreen(
                            viewModel = hiltViewModel(),
                            onNotificationClick = { notificationId ->
                                // Navegación adicional si fuera necesaria
                            },
                            onLogout = {
                                val serviceIntent = Intent(this@MainActivity, NotificationForegroundService::class.java)
                                stopService(serviceIntent)

                                prefsManager.clearSession()
                                notificationRepository.resetSyncState()
                                isLoggedIn = false
                            }
                        )
                    } else {
                        LoginScreen(
                            viewModel = hiltViewModel(),
                            onLoginSuccess = { user ->
                                notificationRepository.resetSyncState()
                                isLoggedIn = true
                            }
                        )
                    }
                }
            }
        }
    }
}
