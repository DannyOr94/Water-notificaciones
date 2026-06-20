package com.watersf.app.presentation.notification.model

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

/**
 * Constantes centralizadas del sistema de alertas ([REQ-4.2], [REQ-5.3]).
 * Evita números mágicos dispersos en ViewModel/UI.
 */
object NotificationAlertConfig {

    /** [REQ-5.1/5.3] No leídas necesarias para disparar la alerta de acumulación. */
    const val ACCUMULATION_THRESHOLD = 5

    /** [REQ-4.2] Ventana (horas) dentro de la cual una no leída se considera "Nueva". */
    const val NEW_WINDOW_HOURS = 24L

    /**
     * Determina si una notificación entra en la pestaña "Nuevos": su [createdAtIso]
     * cae dentro de las últimas [NEW_WINDOW_HOURS]. Fechas no parseables se excluyen.
     */
    fun isWithinNewWindow(createdAtIso: String, now: Instant = Instant.now()): Boolean {
        val created = parseInstant(createdAtIso) ?: return false
        val cutoff = now.minus(Duration.ofHours(NEW_WINDOW_HOURS))
        return created.isAfter(cutoff)
    }

    /** Parsing tolerante a las variantes ISO que produce el backend (offset, 'Z', sin zona). */
    private fun parseInstant(iso: String): Instant? {
        return runCatching { OffsetDateTime.parse(iso).toInstant() }
            .recoverCatching { Instant.parse(iso) }
            .recoverCatching { LocalDateTime.parse(iso).atZone(ZoneId.systemDefault()).toInstant() }
            .recoverCatching { LocalDate.parse(iso).atStartOfDay(ZoneId.systemDefault()).toInstant() }
            .getOrNull()
    }
}
