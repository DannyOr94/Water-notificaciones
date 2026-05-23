package com.watersf.app.data.repository

import com.watersf.app.data.local.dao.NotificationDao
import com.watersf.app.data.local.entity.toDomain
import com.watersf.app.data.remote.api.WaterApiService
import com.watersf.app.data.remote.dto.toEntity
import com.watersf.app.data.security.EncryptedPrefsManager
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.model.UserType
import com.watersf.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val apiService: WaterApiService,
    private val prefsManager: EncryptedPrefsManager
) : NotificationRepository {

    /**
     * Retorna un flujo observable de notificaciones directamente desde Room.
     * La UI siempre observa esta fuente de verdad local (Offline-First).
     */
    override fun getNotificationsFlow(
        priority: String?,
        isRead: Boolean?,
        module: String?
    ): Flow<List<Notification>> {
        return notificationDao.getNotifications(
            module = module,
            isRead = isRead,
            priority = priority
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getNotificationById(id: String): Notification? {
        return notificationDao.getNotificationById(id)?.toDomain()
    }

    /**
     * Sincroniza las notificaciones con el backend NestJS.
     * Determina dinámicamente qué servicio consumir según el rol del usuario actual.
     */
    override suspend fun syncNotifications(): Result<Unit> {
        return try {
            val user = prefsManager.getUser() ?: return Result.failure(Exception("No hay usuario autenticado en la sesión"))
            
            // Decidir endpoint dinámicamente según el tipo de usuario (Requerimiento 2)
            val response = when (user.type) {
                UserType.ADMINISTRADOR,
                UserType.ADMINISTRATIVO,
                UserType.SECRETARIO,
                UserType.JUNTA -> {
                    // Endpoint público unificado del proyecto cechProyect
                    apiService.getAdminNotifications()
                }
                UserType.ABONADO,
                UserType.FONTANERO -> {
                    // Endpoint personal del usuario actual
                    apiService.getMyNotifications()
                }
            }

            if (response.isSuccessful && response.body() != null) {
                val remoteNotifications = response.body()!!
                
                // Mapear DTOs a Entidades de Room
                val entities = remoteNotifications.map { it.toEntity() }
                
                // Guardar en la BD local. Room notificará automáticamente al Flow activo en UI.
                notificationDao.insertNotifications(entities)
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error de sincronización con el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            // En caso de fallas de conexión (offline), capturamos el error para avisar a la UI,
            // pero el flujo de Room seguirá sirviendo los datos cacheados localmente.
            Result.failure(e)
        }
    }

    /**
     * Marca una notificación como leída tanto localmente (inmediato para UX reactiva)
     * como remotamente en el servidor en segundo plano.
     */
    override suspend fun markAsRead(id: String): Result<Unit> {
        return try {
            // Actualización local inmediata (optimistic update)
            notificationDao.markAsRead(id)

            // Actualización remota
            val response = apiService.markAsRead(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                // Si la red falla, se mantiene marcada localmente y se sincronizará luego
                Result.failure(Exception("Error al actualizar estado en el servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
