package com.watersf.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.watersf.app.data.local.entity.NotificationEntity
import com.watersf.app.domain.model.NotificationPriority

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
        // [RISK-1] Normalizar al escribir: Room siempre guarda la forma canónica
        // ("alta"/"media"/"baja") aunque el backend mande UPPERCASE, para no romper
        // el ORDER BY ni el filtrado case-sensitive del DAO.
        priority = NotificationPriority.fromString(priority).value,
        audience = audience
    )
}
