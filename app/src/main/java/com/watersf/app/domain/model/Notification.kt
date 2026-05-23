package com.watersf.app.domain.model

enum class NotificationPriority(val value: String) {
    ALTA("alta"),
    MEDIA("media"),
    BAJA("baja");

    companion object {
        fun fromString(value: String?): NotificationPriority {
            return entries.find { it.value.equals(value, ignoreCase = true) } ?: BAJA
        }
    }
}

data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val module: String, // e.g. "facturas", "reportes", "tareas"
    val type: String,   // e.g. "factura_vencida", "averia_asignada"
    val targetId: String?,
    val createdAt: String, // ISO date string
    val isRead: Boolean,
    val priority: NotificationPriority,
    val audience: String?
)
