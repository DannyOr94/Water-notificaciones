package com.watersf.app.presentation.notification.model

/**
 * [REQ-4.1] Pestañas de seguimiento de lectura.
 *
 * - [NUEVOS]   no leídas recibidas recientemente (isRead=false + ventana de recencia
 *              [NotificationAlertConfig.NEW_WINDOW_HOURS], aplicada en el ViewModel).
 * - [SIN_LEER] todas las no leídas (isRead=false).
 * - [LEIDOS]   leídas (isRead=true).
 *
 * [isReadFilter] mapea cada pestaña al filtro existente del DAO/ViewModel ([REQ-4.2]).
 */
enum class NotificationTab(val isReadFilter: Boolean?) {
    NUEVOS(isReadFilter = false),
    SIN_LEER(isReadFilter = false),
    LEIDOS(isReadFilter = true);
}
