package com.watersf.app.data.remote.api

object ApiConstants {
    // La URL base para desarrollo con dispositivo físico local (misma red Wi-Fi)
    const val BASE_URL = "http://192.168.100.232:3000/"

    // Los endpoints relativos NO deben comenzar con barra diagonal (/) en Retrofit
    const val LOGIN_ENDPOINT = "auth/login"
    const val NOTIFICATIONS_ENDPOINT = "notifications/me"
    const val GENERAL_NOTIFICATION_ENDPOINT = "notification"
}
