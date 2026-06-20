package com.watersf.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.watersf.app.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    /**
     * Retorna un Flow de notificaciones filtradas dinámicamente.
     * Si un parámetro es null, se ignora el filtro respectivo en SQLite.
     * Ordena por Prioridad ('alta' > 'media' > 'baja') y luego por fecha descendente.
     */
    @Query("""
        SELECT * FROM notifications
        WHERE (:module IS NULL OR module = :module)
          AND (:isRead IS NULL OR isRead = :isRead)
          AND (:priority IS NULL OR LOWER(priority) = LOWER(:priority))
        ORDER BY
          CASE LOWER(priority)
            WHEN 'alta' THEN 1
            WHEN 'media' THEN 2
            WHEN 'baja' THEN 3
            ELSE 4
          END ASC,
          createdAt DESC
    """)
    fun getNotifications(
        module: String?,
        isRead: Boolean?,
        priority: String?
    ): Flow<List<NotificationEntity>>

    /**
     * [REQ-3.3] Conteo reactivo de no leídas. Room re-emite ante cualquier cambio
     * en la tabla (insert/update), por lo que el badge se deriva de aquí (single
     * source of truth) y nunca de un contador en memoria.
     */
    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun observeUnreadCount(): Flow<Int>

    @Query("SELECT * FROM notifications WHERE id = :id LIMIT 1")
    suspend fun getNotificationById(id: String): NotificationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDirectly(notification: NotificationEntity)

    @androidx.room.Transaction
    suspend fun insertOrUpdateNotifications(notifications: List<NotificationEntity>) {
        for (notification in notifications) {
            val existing = getNotificationById(notification.id)
            if (existing != null) {
                // Preservar el estado de lectura local
                val updated = notification.copy(isRead = existing.isRead)
                insertDirectly(updated)
            } else {
                insertDirectly(notification)
            }
        }
    }

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: String)

    @Query("SELECT EXISTS(SELECT 1 FROM notifications WHERE id = :id LIMIT 1)")
    suspend fun exists(id: String): Boolean

    @Query("DELETE FROM notifications")
    suspend fun deleteAll()
}
