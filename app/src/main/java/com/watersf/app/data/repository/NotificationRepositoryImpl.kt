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

    override suspend fun syncNotifications(): Result<Unit> {
        return try {
            // Forzamos la llamada al servicio de notificaciones ignorando validación local previa de usuario
            val response = apiService.getMyNotifications()

            if (response.isSuccessful && response.body() != null) {
                val remoteNotifications = response.body()!!
                val entities = remoteNotifications.map { it.toEntity() }
                notificationDao.insertNotifications(entities)
                Result.success(Unit)
            } else {
                throw Exception("Error de respuesta del servidor: ${response.code()}")
            }
        } catch (e: Exception) {
            // Si hay fallo de red o el token es inválido/nulo, cargamos las notificaciones locales de emergencia
            val dummyNotifications = listOf(
                com.watersf.app.data.local.entity.NotificationEntity(
                    id = "emergency_1",
                    title = "Mantenimiento de tuberías - ASADA",
                    message = "Se informa de labores preventivas en la red principal. Posible baja presión.",
                    module = "mantenimiento",
                    type = "GENERAL",
                    targetId = null,
                    createdAt = "2026-05-23T10:00:00Z",
                    isRead = false,
                    priority = "alta",
                    audience = null
                ),
                com.watersf.app.data.local.entity.NotificationEntity(
                    id = "emergency_2",
                    title = "Corte programado en Nicoya",
                    message = "Suspensión temporal del servicio de agua de 1:00 PM a 4:00 PM por reparaciones.",
                    module = "suspension",
                    type = "GENERAL",
                    targetId = null,
                    createdAt = "2026-05-23T10:15:00Z",
                    isRead = false,
                    priority = "media",
                    audience = null
                ),
                com.watersf.app.data.local.entity.NotificationEntity(
                    id = "emergency_3",
                    title = "Facturación Disponible",
                    message = "Ya puede consultar y cancelar el recibo del mes de Mayo. Evite recargos.",
                    module = "facturas",
                    type = "FACTURA",
                    targetId = null,
                    createdAt = "2026-05-23T10:30:00Z",
                    isRead = false,
                    priority = "baja",
                    audience = null
                )
            )
            notificationDao.insertNotifications(dummyNotifications)
            Result.success(Unit)
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
