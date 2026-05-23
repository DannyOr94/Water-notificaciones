package com.watersf.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Mapeo fiel del DTO de NestJS.
 * Se usan Strings para los Enums para evitar fallos de parsing si el backend añade nuevos tipos.
 */
data class NotificationResponseDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("module") val module: String,     // Enum: 'facturas', 'reportes', etc.
    @SerializedName("type") val type: String,         // Enum: 'ALERTA', 'RECORDATORIO', etc.
    @SerializedName("targetId") val targetId: String?,
    @SerializedName("createdAt") val createdAt: String,
    @SerializedName("isRead") val isRead: Boolean,
    @SerializedName("priority") val priority: String, // Enum: 'BAJA', 'MEDIA', 'ALTA'
    @SerializedName("audience") val audience: String?
)
