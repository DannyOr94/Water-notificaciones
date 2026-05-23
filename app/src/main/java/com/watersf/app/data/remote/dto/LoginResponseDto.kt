package com.watersf.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("token") val token: String?,
    @SerializedName("user") val user: JwtUser
)
