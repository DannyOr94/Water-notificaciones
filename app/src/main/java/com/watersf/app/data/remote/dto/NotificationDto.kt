package com.watersf.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.watersf.app.data.local.entity.NotificationEntity

data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("module") val module: String,
    @SerializedName("type") val type: String,
    @SerializedName("targetId") val targetId: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("priority") val priority: String,
    @SerializedName("audience") val audience: String?
)

fun NotificationDto.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        message = message,
        module = module,
        type = type,
        targetId = targetId,
        createdAt = createdAt,
        isRead = isRead,
        priority = priority,
        audience = audience
    )
}
