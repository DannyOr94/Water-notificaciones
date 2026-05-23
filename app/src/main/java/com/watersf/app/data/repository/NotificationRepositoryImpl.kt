package com.watersf.app.data.repository

import com.watersf.app.data.local.dao.NotificationDao
import com.watersf.app.data.local.entity.toDomain
import com.watersf.app.data.remote.api.WaterApiService
import com.watersf.app.data.remote.dto.toEntity
import com.watersf.app.data.security.EncryptedPrefsManager
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val apiService: WaterApiService,
    private val prefsManager: EncryptedPrefsManager
) : NotificationRepository {

    private val _newNotificationFlow = MutableSharedFlow<Notification>(extraBufferCapacity = 64)
    override val newNotificationFlow: Flow<Notification> = _newNotificationFlow.asSharedFlow()

    private var isFirstSync = true
    private var lastSessionToken: String? = null
    private val seenNotificationIds = HashSet<String>()

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
            if (prefsManager.getToken() == null) return Result.success(Unit)

            val response = apiService.getMyNotifications()

            if (response.isSuccessful && response.body() != null) {
                val remoteNotifications = response.body()!!
                val entities = remoteNotifications.map { it.toEntity() }

                var hasNew = false
                var latestNewNotification: Notification? = null

                for (entity in entities) {
                    val alreadySeen = seenNotificationIds.contains(entity.id) || notificationDao.exists(entity.id)
                    if (!alreadySeen) {
                        hasNew = true
                        latestNewNotification = entity.toDomain()
                    }
                    seenNotificationIds.add(entity.id)
                }

                // Guardamos en la base de datos local
                notificationDao.insertNotifications(entities)

                if (hasNew && latestNewNotification != null && !isFirstSync) {
                    _newNotificationFlow.emit(latestNewNotification)
                }

                isFirstSync = false
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error de sincronización con el servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun markAsRead(id: String): Result<Unit> {
        return try {
            // Actualización local inmediata (optimistic update)
            notificationDao.markAsRead(id)

            // Actualización remota
            val response = apiService.markAsRead(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al actualizar estado en el servidor"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun resetSyncState() {
        isFirstSync = true
        seenNotificationIds.clear()
        lastSessionToken = null
    }
}
