package com.watersf.app.data.remote.api

import com.watersf.app.data.remote.dto.NotificationResponseDto
import retrofit2.Response
import retrofit2.http.GET

interface NotificationApiService {

    @GET(ApiConstants.NOTIFICATIONS_ENDPOINT)
    suspend fun getMyNotifications(): Response<List<NotificationResponseDto>>

    @GET(ApiConstants.GENERAL_NOTIFICATION_ENDPOINT)
    suspend fun getGeneralNotifications(): Response<List<NotificationResponseDto>>
}
