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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.watersf.app.domain.model.Notification
import com.watersf.app.presentation.theme.AppIcons
import com.watersf.app.presentation.theme.Dimens
import com.watersf.app.presentation.theme.Outline
import com.watersf.app.presentation.theme.OutlineStrong
import com.watersf.app.presentation.theme.PrimaryBright
import com.watersf.app.presentation.theme.Surface
import com.watersf.app.presentation.theme.SurfaceVariant
import com.watersf.app.presentation.theme.TextBody
import com.watersf.app.presentation.theme.TextPrimary
import com.watersf.app.presentation.theme.TextSecondary
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(
    item: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Severidad centralizada en AppIcons (antes duplicada inline)
    val priorityColor = AppIcons.colorForPriority(item.priority)
    val priorityBg = AppIcons.containerForPriority(item.priority)

    // Etiqueta del módulo
    val moduleLabel = item.module.replaceFirstChar { it.uppercase() }

    // Fondo y borde según si es leída o no
    val cardBg = if (item.isRead) Surface else SurfaceVariant
    val borderTint = if (item.isRead) Outline else OutlineStrong

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = Dimens.space3)
            .clip(RoundedCornerShape(Dimens.radiusMd))
            .border(Dimens.borderThin, borderTint, RoundedCornerShape(Dimens.radiusMd))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Dimens.radiusMd),
        colors = CardDefaults.cardColors(
            containerColor = cardBg
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (item.isRead) Dimens.elevNone else Dimens.elevCard
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(androidx.compose.foundation.layout.IntrinsicSize.Max)
        ) {
            // Indicador de prioridad vertical izquierdo
            Box(
                modifier = Modifier
                    .width(Dimens.priorityBarWidth)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(Dimens.space4)
            ) {
                // Header: Módulo (con icono semántico) y estado de lectura
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SuggestionChip(
                        onClick = {},
                        label = { Text(moduleLabel, style = MaterialTheme.typography.labelSmall) },
                        icon = {
                            Icon(
                                imageVector = AppIcons.forModule(item.module),
                                contentDescription = null,
                                tint = priorityColor,
                                modifier = Modifier.size(Dimens.iconSm)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = priorityBg,
                            labelColor = priorityColor,
                            iconContentColor = priorityColor
                        ),
                        border = null,
                        modifier = Modifier.height(Dimens.space7)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Indicador de no leído
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .size(Dimens.unreadDot)
                                .clip(RoundedCornerShape(Dimens.radiusPill))
                                .background(PrimaryBright)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Nueva",
                            color = PrimaryBright,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.space2))

                // Título
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (item.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = if (item.isRead) TextSecondary else TextPrimary
                )

                Spacer(modifier = Modifier.height(Dimens.space1))

                // Mensaje
                val displayMessage = formatNotificationMessage(item)
                Text(
                    text = displayMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextBody,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(Dimens.space2))

                // Footer: Fecha
                Text(
                    text = formatDateTime(item.createdAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
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
        val cleanIso = if (isoString.contains("T")) {
            val datePart = isoString.substringBefore("T")
            val timePart = isoString.substringAfter("T")
                .substringBefore("Z")
                .substringBefore("+")
            val timeWithoutOffset = if (timePart.contains("-")) {
                timePart.substringBeforeLast("-")
            } else {
                timePart
            }
            "${datePart}T${timeWithoutOffset}"
        } else {
            isoString
        }

        val formatter = DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale("es", "CR"))
        try {
            val ldt = java.time.LocalDateTime.parse(cleanIso)
            ldt.format(formatter)
        } catch (e: Exception) {
            val date = java.time.LocalDate.parse(cleanIso)
            date.format(DateTimeFormatter.ofPattern("dd MMM", Locale("es", "CR")))
        }
    } catch (e: Exception) {
        isoString
    }
}
