package com.watersf.app.domain.repository

import com.watersf.app.domain.model.User

interface AuthRepository {
    suspend fun login(usernameOrEmail: String, password: String): Result<User>
    suspend fun getSessionUser(): User?
    suspend fun getSessionToken(): String?
    suspend fun logout(): Result<Unit>
}
