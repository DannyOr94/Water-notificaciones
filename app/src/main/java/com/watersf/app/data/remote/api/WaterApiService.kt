package com.watersf.app.data.remote.api

import com.watersf.app.data.remote.dto.LoginRequest
import com.watersf.app.data.remote.dto.LoginResponseDto
import com.watersf.app.data.remote.dto.NotificationDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface WaterApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<com.watersf.app.data.remote.dto.LoginResponseDto>

    @GET("notifications/me")
    suspend fun getMyNotifications(): Response<List<NotificationDto>>

    @GET("notifications/me")
    suspend fun getAdminNotifications(): Response<List<NotificationDto>>

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: String
    ): Response<Unit>
}