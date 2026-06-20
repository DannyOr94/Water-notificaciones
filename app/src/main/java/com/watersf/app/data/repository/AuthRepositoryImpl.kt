package com.watersf.app.data.repository

import com.watersf.app.data.remote.api.WaterApiService
import com.watersf.app.data.remote.dto.LoginRequest
import com.watersf.app.data.security.EncryptedPrefsManager
import com.watersf.app.domain.model.User
import com.watersf.app.domain.model.UserType
import com.watersf.app.domain.repository.AuthRepository
import java.lang.Exception
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val waterApiService: WaterApiService,
    private val encryptedPrefsManager: EncryptedPrefsManager
) : AuthRepository {

    override suspend fun login(usernameOrEmail: String, password: String): Result<User> {
        return try {
            // Enviamos la petición al backend (NestJS)
            val response = waterApiService.login(LoginRequest(usernameOrEmail, password))

            if (response.isSuccessful) {
                val body = response.body()
                
                // Intentamos extraer el token del JSON
                var responseToken = body?.token ?: body?.accessToken

                // Si no está en el JSON, lo extraemos de la cabecera Set-Cookie
                if (responseToken.isNullOrEmpty()) {
                    val cookies = response.headers().values("Set-Cookie")
                    for (cookie in cookies) {
                        if (cookie.contains("eyJ")) { // Los JWT siempre empiezan con eyJ
                            val parts = cookie.split(";")
                            val nameValuePair = parts[0]
                            val valueIndex = nameValuePair.indexOf('=')
                            if (valueIndex != -1) {
                                responseToken = nameValuePair.substring(valueIndex + 1)
                                break
                            }
                        }
                    }
                }

                if (body?.user != null && !responseToken.isNullOrEmpty()) {
                    // 1. Guardamos el token real de la sesión
                    encryptedPrefsManager.saveToken(responseToken)

                    // 2. Convertimos el JwtUser del DTO al User del Dominio de la App
                    val domainUser = User(
                        id = body.user.id.toString(),
                        name = body.user.name,
                        email = body.user.email,
                        type = UserType.fromString(body.user.type),
                        nis = body.user.nis
                    )

                    // 3. Guardamos el usuario en la sesión local
                    encryptedPrefsManager.saveUser(domainUser)

                    Result.success(domainUser)
                } else {
                    Result.failure(Exception("No se pudo extraer el token de las cabeceras (Cookies) o el cuerpo. JSON: $body"))
                }
            } else {
                val errorBodyString = response.errorBody()?.string() ?: "Sin detalles"
                Result.failure(Exception("Error HTTP ${response.code()}: $errorBodyString"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getSessionUser(): User? {
        return encryptedPrefsManager.getUser()
    }

    override suspend fun getSessionToken(): String? {
        return encryptedPrefsManager.getToken()
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            encryptedPrefsManager.clearSession()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}