package com.watersf.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import com.watersf.app.data.security.EncryptedPrefsManager
import com.watersf.app.presentation.auth.LoginScreen
import com.watersf.app.presentation.notification.NotificationListScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var prefsManager: EncryptedPrefsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var isLoggedIn by remember { mutableStateOf(prefsManager.getToken() != null) }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF0A0F1D) // Fondo azul oscuro global
            ) {
                if (isLoggedIn) {
                    NotificationListScreen(
                        viewModel = hiltViewModel(),
                        onNotificationClick = { notificationId ->
                            // Navegación adicional si fuera necesaria
                        },
                        onLogout = {
                            prefsManager.clearSession()
                            isLoggedIn = false
                        }
                    )
                } else {
                    LoginScreen(
                        viewModel = hiltViewModel(),
                        onLoginSuccess = { user ->
                            isLoggedIn = true
                        }
                    )
                }
            }
        }
    }
}