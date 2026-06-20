package com.watersf.app.presentation.notification.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.watersf.app.R
import com.watersf.app.presentation.notification.model.NotificationTab
import com.watersf.app.presentation.theme.BackgroundBase
import com.watersf.app.presentation.theme.Dimens
import com.watersf.app.presentation.theme.Outline
import com.watersf.app.presentation.theme.PrimaryBright
import com.watersf.app.presentation.theme.SeverityHigh
import com.watersf.app.presentation.theme.TextSecondary

/**
 * [REQ-4.1] Pestañas de seguimiento de lectura (Nuevos / Sin leer / Leídos).
 * La pestaña "Sin leer" muestra el conteo global de pendientes.
 */
@Composable
fun NotificationTabs(
    activeTab: NotificationTab,
    unreadCount: Int,
    onTabSelected: (NotificationTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = NotificationTab.entries
    TabRow(
        selectedTabIndex = tabs.indexOf(activeTab),
        containerColor = BackgroundBase,
        contentColor = PrimaryBright,
        divider = { HorizontalDivider(color = Outline, thickness = Dimens.borderThin) },
        modifier = modifier
    ) {
        tabs.forEach { tab ->
            val selected = tab == activeTab
            Tab(
                selected = selected,
                onClick = { onTabSelected(tab) },
                selectedContentColor = PrimaryBright,
                unselectedContentColor = TextSecondary,
                text = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.space2)
                    ) {
                        Text(
                            text = stringResource(labelFor(tab)),
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                        if (tab == NotificationTab.SIN_LEER && unreadCount > 0) {
                            Badge(
                                containerColor = SeverityHigh,
                                contentColor = BackgroundBase
                            ) {
                                Text(text = if (unreadCount > 99) "99+" else "$unreadCount")
                            }
                        }
                    }
                }
            )
        }
    }
}

private fun labelFor(tab: NotificationTab): Int = when (tab) {
    NotificationTab.NUEVOS -> R.string.tab_new
    NotificationTab.SIN_LEER -> R.string.tab_unread
    NotificationTab.LEIDOS -> R.string.tab_read
}
