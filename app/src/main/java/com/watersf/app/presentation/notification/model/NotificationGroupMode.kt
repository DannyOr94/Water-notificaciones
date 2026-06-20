package com.watersf.app.presentation.notification.model

/**
 * [REQ-1.1] Modo de agrupación de la lista. Es una decisión puramente presentacional:
 * no altera la query de Room, solo cómo se seccionan los resultados ya ordenados.
 *
 * - [PRIORITY] agrupa por severidad (ALTA / MEDIA / BAJA). Coincide con el orden del DAO.
 * - [MODULE]   agrupa por módulo (facturas, reportes, ...).
 */
enum class NotificationGroupMode {
    PRIORITY,
    MODULE;
}
