package com.watersf.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginResponseDto(
    @SerializedName(value = "accessToken", alternate = ["access_token", "access_Token"]) val accessToken: String?,
    @SerializedName(value = "token", alternate = ["Token"]) val token: String?,
    @SerializedName("user") val user: JwtUser
)
