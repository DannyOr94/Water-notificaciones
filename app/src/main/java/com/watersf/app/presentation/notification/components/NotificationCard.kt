package com.watersf.app.presentation.notification.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.model.NotificationPriority
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    item: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Paleta de colores por prioridad adaptada al fondo azul oscuro
    val (priorityColor, priorityBg) = when (item.priority) {
        NotificationPriority.ALTA -> Color(0xFFF87171) to Color(0xFF3B1E1E)
        NotificationPriority.MEDIA -> Color(0xFFFBBF24) to Color(0xFF3B2A1E)
        NotificationPriority.BAJA -> Color(0xFF34D399) to Color(0xFF1E3B2E)
    }

    // Etiqueta del módulo
    val moduleLabel = item.module.replaceFirstChar { it.uppercase() }

    // Fondo según si es leída o no
    val cardBg = if (item.isRead) Color(0xFF131C33) else Color(0xFF1E294B)
    val borderTint = if (item.isRead) Color(0xFF222D4A) else Color(0xFF3B4D80)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 12.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, borderTint, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isRead) 0.dp else 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 90.dp)
        ) {
            // Indicador de prioridad vertical izquierdo
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Header: Módulo y estado de lectura
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(moduleLabel, style = MaterialTheme.typography.labelSmall) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = priorityBg,
                            labelColor = priorityColor
                        ),
                        border = null,
                        modifier = Modifier.height(24.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Indicador de no leído
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .width(8.dp)
                                .height(8.dp)
                                .clip(RoundedCornerShape(50))
                                .background(Color(0xFF38BDF8)) // Cyan brillante
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nueva",
                            color = Color(0xFF38BDF8),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Título
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = if (item.isRead) Color(0xFF94A3B8) else Color(0xFFF8FAF4)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Mensaje
                val displayMessage = formatNotificationMessage(item)
                Text(
                    text = displayMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFCBD5E1),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Footer: Fecha
                Text(
                    text = formatDateTime(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

/**
 * Formatea el mensaje dinámicamente según el módulo
 */
private fun formatNotificationMessage(notification: Notification): String {
    return when (notification.module.lowercase()) {
        "facturas", "facturación" -> {
            if (notification.message.contains("Total", ignoreCase = true) || notification.message.contains("₡")) {
                notification.message
            } else {
                "${notification.message} (₡)"
            }
        }
        else -> notification.message
    }
}

private fun formatDateTime(isoString: String): String {
    return try {
        val normalized = if (!isoString.contains("Z") && !isoString.contains("+") && isoString.contains("T")) {
            isoString + "Z"
        } else {
            isoString
        }
        val instant = Instant.parse(normalized)
        val crZone = ZoneId.of("America/Costa_Rica")
        val crDateTime = instant.atZone(crZone)
        val formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale("es", "CR"))
        crDateTime.format(formatter)
    } catch (e: Exception) {
        isoString
    }
}