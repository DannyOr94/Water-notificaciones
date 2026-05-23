package com.watersf.app.data.remote.api

object ApiConstants {
    // La URL base para el servidor de producción remoto
    const val BASE_URL = "http://212.38.95.11:3000/"

    // Los endpoints relativos NO deben comenzar con barra diagonal (/) en Retrofit
    const val LOGIN_ENDPOINT = "auth/login"
    const val NOTIFICATIONS_ENDPOINT = "notifications/me"
    const val GENERAL_NOTIFICATION_ENDPOINT = "notification"
}
