package com.watersf.app.domain.repository

import com.watersf.app.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    val newNotificationFlow: Flow<Notification>

    fun getNotificationsFlow(
        priority: String? = null,
        isRead: Boolean? = null,
        module: String? = null
    ): Flow<List<Notification>>

    suspend fun getNotificationById(id: String): Notification?

    suspend fun syncNotifications(): Result<Unit>

    suspend fun markAsRead(id: String): Result<Unit>

    fun resetSyncState()
}
