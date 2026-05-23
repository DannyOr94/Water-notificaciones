package com.watersf.app.data.remote.dto

import com.google.gson.annotations.SerializedName

// Lo que mandas al servidor
data class LoginRequest(
    @SerializedName("identifier") val identifier: String,
    @SerializedName("password") val password: String
)

// La respuesta completa que vimos en el Swagger
data class LoginResponse(
    @SerializedName("user") val user: JwtUser,
    @SerializedName("token") val token: String
)

// Los datos internos del usuario administrador de la ASADA
data class JwtUser(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("type") val type: String,
    @SerializedName("nis") val nis: String?
)