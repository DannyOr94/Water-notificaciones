package com.watersf.app.presentation.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.watersf.app.domain.model.NotificationPriority

// ============================================================================
// Water-SF · Mapeo central de iconos semánticos. Las pantallas referencian
// AppIcons, nunca íconos sueltos. [INV-5]
// Todos provienen de material-icons-extended (ya dependencia del proyecto).
// Se prefieren variantes AutoMirrored donde existen para no introducir
// warnings de deprecación. [C-E1]
// ============================================================================

object AppIcons {
    // Marca / autenticación
    val brand: ImageVector = Icons.Filled.WaterDrop
    val email: ImageVector = Icons.Filled.Email
    val password: ImageVector = Icons.Filled.Lock
    val passwordShow: ImageVector = Icons.Filled.Visibility
    val passwordHide: ImageVector = Icons.Filled.VisibilityOff

    // Acciones / navegación
    val logout: ImageVector = Icons.AutoMirrored.Filled.Logout
    val refresh: ImageVector = Icons.Filled.Refresh
    val filter: ImageVector = Icons.Filled.FilterList

    // Notificaciones / estado
    val notificationActive: ImageVector = Icons.Filled.NotificationsActive
    val notificationEmpty: ImageVector = Icons.Filled.NotificationsNone
    val offline: ImageVector = Icons.Filled.CloudOff
    val alertHigh: ImageVector = Icons.Filled.PriorityHigh

    // Módulos
    val moduleFacturas: ImageVector = Icons.AutoMirrored.Filled.ReceiptLong
    val moduleAverias: ImageVector = Icons.Filled.Build
    val moduleReportes: ImageVector = Icons.Filled.Assessment
    val moduleSolicitudes: ImageVector = Icons.Filled.AssignmentTurnedIn
    val moduleTareas: ImageVector = Icons.Filled.Checklist
    val moduleDefault: ImageVector = Icons.Filled.Campaign

    /** Icono según el módulo de la notificación (match laxo por nombre). */
    fun forModule(module: String): ImageVector = when {
        module.contains("factura", ignoreCase = true) -> moduleFacturas
        module.contains("aver", ignoreCase = true) -> moduleAverias
        module.contains("reporte", ignoreCase = true) -> moduleReportes
        module.contains("solicitud", ignoreCase = true) -> moduleSolicitudes
        module.contains("tarea", ignoreCase = true) -> moduleTareas
        else -> moduleDefault
    }

    /** Color de severidad por prioridad (enum). Centraliza la lógica antes duplicada. */
    fun colorForPriority(priority: NotificationPriority): Color = when (priority) {
        NotificationPriority.ALTA -> SeverityHigh
        NotificationPriority.MEDIA -> SeverityMedium
        NotificationPriority.BAJA -> SeverityLow
    }

    /** Color de contenedor (chip) por severidad (enum). */
    fun containerForPriority(priority: NotificationPriority): Color = when (priority) {
        NotificationPriority.ALTA -> SeverityHighContainer
        NotificationPriority.MEDIA -> SeverityMediumContainer
        NotificationPriority.BAJA -> SeverityLowContainer
    }

    /** Color de severidad por string de prioridad (para el diálogo que opera sobre String). */
    fun colorForPriority(priorityValue: String): Color = when (priorityValue.lowercase()) {
        "alta" -> SeverityHigh
        "media" -> SeverityMedium
        else -> SeverityLow
    }
}
