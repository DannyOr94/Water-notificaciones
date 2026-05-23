package com.watersf.app.data.repository

import android.content.Context
import android.media.RingtoneManager
import com.watersf.app.data.local.dao.NotificationDao
import com.watersf.app.data.local.entity.toDomain
import com.watersf.app.data.remote.api.WaterApiService
import com.watersf.app.data.remote.dto.toEntity
import com.watersf.app.data.security.EncryptedPrefsManager
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.model.UserType
import com.watersf.app.domain.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
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
    private val prefsManager: EncryptedPrefsManager,
    @ApplicationContext private val context: Context
) : NotificationRepository {

    private val _newNotificationFlow = MutableSharedFlow<Notification>(extraBufferCapacity = 64)
    override val newNotificationFlow: Flow<Notification> = _newNotificationFlow.asSharedFlow()

    private var isFirstSync = true

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
            val response = apiService.getMyNotifications()

            if (response.isSuccessful && response.body() != null) {
                val remoteNotifications = response.body()!!
                val entities = remoteNotifications.map { it.toEntity() }

                var hasNew = false
                var latestNewNotification: Notification? = null

                for (entity in entities) {
                    val alreadyExists = notificationDao.exists(entity.id)
                    if (!alreadyExists) {
                        hasNew = true
                        latestNewNotification = entity.toDomain()
                    }
                }

                // Guardamos en la base de datos local
                notificationDao.insertNotifications(entities)

                if (hasNew && latestNewNotification != null && !isFirstSync) {
                    playNotificationSound(context)
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

    private fun playNotificationSound(context: Context) {
        try {
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val ringtone = RingtoneManager.getRingtone(context, notificationUri)
            ringtone.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
