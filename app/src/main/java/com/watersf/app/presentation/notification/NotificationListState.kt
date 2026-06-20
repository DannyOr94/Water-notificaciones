package com.watersf.app.presentation.notification

import com.watersf.app.domain.model.Notification
import com.watersf.app.presentation.notification.model.NotificationAlertConfig
import com.watersf.app.presentation.notification.model.NotificationGroupMode
import com.watersf.app.presentation.notification.model.NotificationTab

data class NotificationListState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val filterModule: String? = null,
    val filterIsRead: Boolean? = null,
    val filterPriority: String? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null,
    // [REQ-3] Conteo global de no leídas, derivado de Room (single source of truth).
    val unreadCount: Int = 0,
    // [REQ-4] Pestaña activa de seguimiento de lectura.
    val activeTab: NotificationTab = NotificationTab.NUEVOS,
    // [REQ-1] Modo de agrupación de la lista.
    val groupMode: NotificationGroupMode = NotificationGroupMode.PRIORITY,
    // [REQ-5.3] Umbral de acumulación expuesto para el copy de la UI.
    val accumulationThreshold: Int = NotificationAlertConfig.ACCUMULATION_THRESHOLD,
) {
    /** [REQ-5.1] Hay acumulación de reportes sin atender. Deriva del conteo de Room. */
    val accumulationAlert: Boolean get() = unreadCount >= accumulationThreshold
}
