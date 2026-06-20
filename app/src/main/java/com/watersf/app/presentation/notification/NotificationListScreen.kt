package com.watersf.app.presentation.notification

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.watersf.app.R
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.model.NotificationPriority
import com.watersf.app.presentation.notification.components.AccumulationAlertBanner
import com.watersf.app.presentation.notification.components.NotificationCard
import com.watersf.app.presentation.notification.components.NotificationTabs
import com.watersf.app.presentation.notification.components.OfflineBanner
import com.watersf.app.presentation.notification.model.NotificationGroupMode
import com.watersf.app.presentation.notification.model.NotificationTab
import com.watersf.app.presentation.theme.AppIcons
import com.watersf.app.presentation.theme.BackgroundBase
import com.watersf.app.presentation.theme.Dimens
import com.watersf.app.presentation.theme.Outline
import com.watersf.app.presentation.theme.PrimaryBright
import com.watersf.app.presentation.theme.SeverityHigh
import com.watersf.app.presentation.theme.Surface
import com.watersf.app.presentation.theme.SurfaceVariant
import com.watersf.app.presentation.theme.TextMuted
import com.watersf.app.presentation.theme.TextPrimary
import com.watersf.app.presentation.theme.TextSecondary
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun formatCRDateTime(isoString: String): String {
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

        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale("es", "CR"))
        try {
            val ldt = java.time.LocalDateTime.parse(cleanIso)
            ldt.format(formatter)
        } catch (e: Exception) {
            val date = java.time.LocalDate.parse(cleanIso)
            date.format(DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("es", "CR")))
        }
    } catch (e: Exception) {
        isoString
    }
}

/**
 * [NotificationListScreen] - Versión STATEFUL.
 * Conecta la lógica de negocio del ViewModel con la interfaz de usuario.
 */
@Composable
fun NotificationListScreen(
    viewModel: NotificationListViewModel,
    onNotificationClick: (String) -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var selectedNotificationForDialog by remember { mutableStateOf<Notification?>(null) }
    var showBanner by remember { mutableStateOf(false) }
    var latestNotification by remember { mutableStateOf<Notification?>(null) }

    // Escuchar el evento de nueva notificación de forma reactiva y limpia (one-off event)
    LaunchedEffect(viewModel) {
        viewModel.newNotificationEvent.collect { notification: Notification ->
            latestNotification = notification
            showBanner = true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotificationListContent(
            uiState = uiState,
            onRefresh = { viewModel.sync() },
            onNotificationClick = { id ->
                viewModel.markAsRead(id)
                val clickedNotif = uiState.notifications.find { it.id == id }
                selectedNotificationForDialog = clickedNotif
                onNotificationClick(id)
            },
            onTabSelected = { viewModel.setActiveTab(it) },
            onGroupModeSelected = { viewModel.setGroupMode(it) },
            onReviewAccumulation = { viewModel.setActiveTab(NotificationTab.SIN_LEER) },
            onFilterPriority = { viewModel.setFilterPriority(it) },
            onFilterModule = { viewModel.setFilterModule(it) },
            onClearFilters = { viewModel.clearFilters() },
            onLogout = onLogout
        )

        // Banner flotante para alertas en tiempo real controlado por showBanner (In-App)
        if (showBanner && latestNotification != null) {
            val newNotif = latestNotification!!
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimens.elevFloating),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(Dimens.borderThin, PrimaryBright),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.space4)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.space4),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.space3)
                ) {
                    Icon(
                        imageVector = AppIcons.notificationActive,
                        contentDescription = null,
                        tint = PrimaryBright,
                        modifier = Modifier.size(Dimens.iconLg)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Nueva alerta recibida!",
                            style = MaterialTheme.typography.labelSmall,
                            color = PrimaryBright,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = newNotif.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = newNotif.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 2
                        )
                    }
                    TextButton(onClick = {
                        showBanner = false
                    }) {
                        Text("OK", color = PrimaryBright)
                    }
                }
            }
        }

        // Dialog flotante con los detalles completos de la alerta al hacer clic
        selectedNotificationForDialog?.let { notification ->
            AlertDialog(
                onDismissRequest = { selectedNotificationForDialog = null },
                title = {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(Dimens.space3),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        HorizontalDivider(color = SurfaceVariant)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Módulo: ${notification.module}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrimaryBright
                            )
                            Text(
                                text = "Prioridad: ${notification.priority.value.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = AppIcons.colorForPriority(notification.priority)
                            )
                        }
                        Text(
                            text = "Fecha: ${formatCRDateTime(notification.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedNotificationForDialog = null }) {
                        Text("Cerrar", color = PrimaryBright)
                    }
                },
                containerColor = Surface,
                titleContentColor = TextPrimary,
                textContentColor = TextPrimary
            )
        }
    }
}

/**
 * [NotificationListContent] - Versión STATELESS.
 * UI Pura: Solo renderiza el estado y comunica eventos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationListContent(
    uiState: NotificationListState,
    onRefresh: () -> Unit,
    onNotificationClick: (String) -> Unit,
    onTabSelected: (NotificationTab) -> Unit,
    onGroupModeSelected: (NotificationGroupMode) -> Unit,
    onReviewAccumulation: () -> Unit,
    onFilterPriority: (String?) -> Unit,
    onFilterModule: (String?) -> Unit,
    onClearFilters: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Dimens.space3)
                    ) {
                        BadgedBox(
                            badge = {
                                if (uiState.unreadCount > 0) {
                                    Badge(
                                        containerColor = if (uiState.accumulationAlert) SeverityHigh else PrimaryBright,
                                        contentColor = BackgroundBase
                                    ) {
                                        Text(if (uiState.unreadCount > 99) "99+" else "${uiState.unreadCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = AppIcons.notificationActive,
                                contentDescription = null,
                                tint = TextPrimary
                            )
                        }
                        Text(
                            text = stringResource(R.string.nav_title_notifications),
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = AppIcons.refresh,
                            contentDescription = stringResource(R.string.sync_notifications)
                        )
                    }
                    if (uiState.filterPriority != null || uiState.filterModule != null) {
                        IconButton(onClick = onClearFilters) {
                            Icon(
                                AppIcons.filter,
                                tint = PrimaryBright,
                                contentDescription = stringResource(R.string.clear_filters)
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = AppIcons.logout,
                            tint = SeverityHigh,
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = BackgroundBase,
                    scrolledContainerColor = Surface,
                    titleContentColor = TextPrimary,
                    actionIconContentColor = TextSecondary
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = BackgroundBase
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador Offline elegante
            OfflineBanner(isOffline = uiState.isOffline)

            // [REQ-5] Alerta de acumulación de reportes sin atender
            AccumulationAlertBanner(
                visible = uiState.accumulationAlert,
                unreadCount = uiState.unreadCount,
                onReview = onReviewAccumulation
            )

            // [REQ-4] Pestañas de seguimiento de lectura
            NotificationTabs(
                activeTab = uiState.activeTab,
                unreadCount = uiState.unreadCount,
                onTabSelected = onTabSelected
            )

            // [REQ-1] Selector de modo de agrupación
            GroupModeSelector(
                groupMode = uiState.groupMode,
                onGroupModeSelected = onGroupModeSelected
            )

            // Sección de Filtros rápidos secundarios
            FilterSection(
                uiState = uiState,
                onFilterPriority = onFilterPriority,
                onFilterModule = onFilterModule
            )

            // Contenedor de lista con manejo de estados (Loading / Empty / List)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(BackgroundBase)
            ) {
                PullToRefreshBox(
                    isRefreshing = uiState.isLoading,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (uiState.isLoading && uiState.notifications.isEmpty()) {
                        ShimmerNotificationsList()
                    } else if (uiState.notifications.isEmpty()) {
                        EmptyNotificationsView(modifier = Modifier.align(Alignment.Center))
                    } else {
                        NotificationsLazyList(
                            notifications = uiState.notifications,
                            groupMode = uiState.groupMode,
                            onNotificationClick = onNotificationClick
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupModeSelector(
    groupMode: NotificationGroupMode,
    onGroupModeSelected: (NotificationGroupMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.space4, vertical = Dimens.space1),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.space2)
    ) {
        Text(
            text = stringResource(R.string.group_by),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )
        GroupChip(
            label = stringResource(R.string.group_priority),
            selected = groupMode == NotificationGroupMode.PRIORITY,
            onClick = { onGroupModeSelected(NotificationGroupMode.PRIORITY) }
        )
        GroupChip(
            label = stringResource(R.string.group_module),
            selected = groupMode == NotificationGroupMode.MODULE,
            onClick = { onGroupModeSelected(NotificationGroupMode.MODULE) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Surface,
            selectedContainerColor = SurfaceVariant,
            labelColor = TextSecondary,
            selectedLabelColor = PrimaryBright
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Outline,
            selectedBorderColor = PrimaryBright,
            borderWidth = Dimens.borderThin,
            selectedBorderWidth = Dimens.borderThin
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterSection(
    uiState: NotificationListState,
    onFilterPriority: (String?) -> Unit,
    onFilterModule: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = Dimens.space4, vertical = Dimens.space2),
        horizontalArrangement = Arrangement.spacedBy(Dimens.space2),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            QuickFilterChip(
                label = stringResource(R.string.filter_high_priority),
                selected = uiState.filterPriority == "alta",
                onClick = { onFilterPriority(if (uiState.filterPriority == "alta") null else "alta") }
            )
        }
        item {
            QuickFilterChip(
                label = stringResource(R.string.filter_reports),
                selected = uiState.filterModule == "Reportes",
                onClick = { onFilterModule(if (uiState.filterModule == "Reportes") null else "Reportes") }
            )
        }
        item {
            QuickFilterChip(
                label = stringResource(R.string.filter_requests),
                selected = uiState.filterModule == "Solicitudes",
                onClick = { onFilterModule(if (uiState.filterModule == "Solicitudes") null else "Solicitudes") }
            )
        }
        item {
            QuickFilterChip(
                label = stringResource(R.string.filter_tasks),
                selected = uiState.filterModule == "Tareas",
                onClick = { onFilterModule(if (uiState.filterModule == "Tareas") null else "Tareas") }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Surface,
            selectedContainerColor = SurfaceVariant,
            labelColor = TextSecondary,
            selectedLabelColor = PrimaryBright
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = Outline,
            selectedBorderColor = PrimaryBright,
            borderWidth = Dimens.borderThin,
            selectedBorderWidth = Dimens.borderThin
        )
    )
}

@Composable
private fun ShimmerNotificationsList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.space3)
    ) {
        repeat(5) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Surface.copy(alpha = 0.6f)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Dimens.space4),
                    verticalArrangement = Arrangement.spacedBy(Dimens.space2)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(16.dp)
                            .background(SurfaceVariant, shape = MaterialTheme.shapes.small)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(14.dp)
                            .background(SurfaceVariant, shape = MaterialTheme.shapes.small)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(12.dp)
                            .background(SurfaceVariant, shape = MaterialTheme.shapes.small)
                    )
                }
            }
        }
    }
}

private data class NotifSection(
    val label: String,
    val accent: Color?,
    val items: List<Notification>
)

/**
 * [REQ-1] Secciona la lista ya ordenada según el modo de agrupación. No re-consulta Room.
 */
private fun buildSections(
    notifications: List<Notification>,
    mode: NotificationGroupMode
): List<NotifSection> = when (mode) {
    NotificationGroupMode.PRIORITY -> NotificationPriority.entries
        .map { priority -> priority to notifications.filter { it.priority == priority } }
        .filter { it.second.isNotEmpty() }
        .map { (priority, items) ->
            NotifSection(priority.name, AppIcons.colorForPriority(priority), items)
        }

    NotificationGroupMode.MODULE -> notifications
        .groupBy { it.module }
        .map { (module, items) ->
            NotifSection(module.replaceFirstChar { it.uppercase() }, null, items)
        }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NotificationsLazyList(
    notifications: List<Notification>,
    groupMode: NotificationGroupMode,
    onNotificationClick: (String) -> Unit
) {
    val sections = remember(notifications, groupMode) { buildSections(notifications, groupMode) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Dimens.space4),
        verticalArrangement = Arrangement.spacedBy(Dimens.space3)
    ) {
        sections.forEach { section ->
            stickyHeader(key = "header_${section.label}") {
                SectionHeader(section)
            }
            items(
                items = section.items,
                key = { it.id }
            ) { notification ->
                NotificationCard(
                    item = notification,
                    onClick = { onNotificationClick(notification.id) }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(section: NotifSection) {
    val accent = section.accent ?: TextSecondary
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BackgroundBase)
            .padding(vertical = Dimens.space2),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.space2)
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.unreadDot)
                .clip(CircleShape)
                .background(accent)
        )
        Text(
            text = section.label,
            style = MaterialTheme.typography.labelSmall,
            color = accent,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "(${section.items.size})",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}

@Composable
private fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(Dimens.space8),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = AppIcons.notificationEmpty,
            contentDescription = null,
            modifier = Modifier.size(Dimens.iconXl),
            tint = TextMuted
        )
        Spacer(modifier = Modifier.height(Dimens.space4))
        Text(
            text = stringResource(R.string.empty_notifications_title),
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(Dimens.space1))
        Text(
            text = stringResource(R.string.empty_notifications_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
    }
}
