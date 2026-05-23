package com.watersf.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.model.NotificationPriority

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val message: String,
    val module: String,
    val type: String,
    val targetId: String?,
    val createdAt: String,
    val isRead: Boolean,
    val priority: String, // stored as "alta", "media", "baja"
    val audience: String?
)

// Extension functions to map between Entity and Domain Model
fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        title = title,
        message = message,
        module = module,
        type = type,
        targetId = targetId,
        createdAt = createdAt,
        isRead = isRead,
        priority = NotificationPriority.fromString(priority),
        audience = audience
    )
}

fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        message = message,
        module = module,
        type = type,
        targetId = targetId,
        createdAt = createdAt,
        isRead = isRead,
        priority = priority.value,
        audience = audience
    )
}
