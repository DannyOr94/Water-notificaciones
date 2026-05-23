package com.watersf.app.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val type: UserType,
    val nis: String?
)
