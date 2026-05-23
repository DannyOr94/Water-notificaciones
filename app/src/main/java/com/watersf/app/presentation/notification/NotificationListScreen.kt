package com.watersf.app.presentation.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.watersf.app.R
import com.watersf.app.domain.model.Notification
import com.watersf.app.presentation.notification.components.NotificationCard
import com.watersf.app.presentation.notification.components.OfflineBanner
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private fun formatCRDateTime(isoString: String): String {
    return try {
        val normalized = if (!isoString.contains("Z") && !isoString.contains("+") && isoString.contains("T")) {
            isoString + "Z"
        } else {
            isoString
        }
        val instant = Instant.parse(normalized)
        val crZone = ZoneId.of("America/Costa_Rica")
        val crDateTime = instant.atZone(crZone)
        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale("es", "CR"))
        crDateTime.format(formatter)
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

    // Activamos el banner de forma controlada cuando llega una nueva notificación
    LaunchedEffect(uiState.newNotificationReceived) {
        if (uiState.newNotificationReceived != null) {
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
            onFilterIsRead = { viewModel.setFilterIsRead(it) },
            onFilterPriority = { viewModel.setFilterPriority(it) },
            onFilterModule = { viewModel.setFilterModule(it) },
            onClearFilters = { viewModel.clearFilters() },
            onLogout = onLogout
        )

        // Banner flotante para alertas en tiempo real controlado por showBanner (In-App)
        if (showBanner && uiState.newNotificationReceived != null) {
            val newNotif = uiState.newNotificationReceived!!
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF131C33)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                          imageVector = Icons.Default.NotificationsNone,
                          contentDescription = null,
                          tint = Color(0xFF38BDF8),
                          modifier = Modifier.size(28.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Nueva alerta recibida!",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = newNotif.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = newNotif.message,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            maxLines = 2
                        )
                    }
                    TextButton(onClick = {
                        showBanner = false
                        viewModel.dismissNewNotificationReceived()
                    }) {
                        Text("OK", color = Color(0xFF38BDF8))
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
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = notification.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0)
                        )
                        HorizontalDivider(color = Color(0xFF1E294B))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Módulo: ${notification.module}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "Prioridad: ${notification.priority.value.uppercase()}",
                                style = MaterialTheme.typography.bodySmall,
                                color = when (notification.priority.value.lowercase()) {
                                    "alta" -> Color(0xFFF87171)
                                    "media" -> Color(0xFFFBBF24)
                                    else -> Color(0xFF34D399)
                                }
                            )
                        }
                        Text(
                            text = "Fecha: ${formatCRDateTime(notification.createdAt)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { selectedNotificationForDialog = null }) {
                        Text("Cerrar", color = Color(0xFF38BDF8))
                    }
                },
                containerColor = Color(0xFF131C33),
                titleContentColor = Color.White,
                textContentColor = Color(0xFFE2E8F0)
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
    onFilterIsRead: (Boolean?) -> Unit,
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
                    Text(
                        text = stringResource(R.string.nav_title_notifications),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF8FAFC)
                    )
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.sync_notifications)
                        )
                    }
                    if (uiState.filterIsRead != null || uiState.filterPriority != null || uiState.filterModule != null) {
                        IconButton(onClick = onClearFilters) {
                            Icon(
                                Icons.Default.FilterList,
                                tint = Color(0xFF38BDF8),
                                contentDescription = stringResource(R.string.clear_filters)
                            )
                        }
                    }
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            tint = Color(0xFFF87171), // Color rojo suave
                            contentDescription = "Cerrar sesión"
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = Color(0xFF0A0F1D),
                    scrolledContainerColor = Color(0xFF131C33),
                    titleContentColor = Color(0xFFF8FAFC),
                    actionIconContentColor = Color(0xFF94A3B8)
                ),
                scrollBehavior = scrollBehavior
            )
        },
        containerColor = Color(0xFF0A0F1D) // Fondo azul oscuro profundo
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Indicador Offline elegante
            OfflineBanner(isOffline = uiState.isOffline)

            // Sección de Filtros rápidos
            FilterSection(
                uiState = uiState,
                onFilterIsRead = onFilterIsRead,
                onFilterPriority = onFilterPriority,
                onFilterModule = onFilterModule
            )

            // Contenedor de lista con manejo de estados (Loading / Empty / List)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .background(Color(0xFF0A0F1D))
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
private fun FilterSection(
    uiState: NotificationListState,
    onFilterIsRead: (Boolean?) -> Unit,
    onFilterPriority: (String?) -> Unit,
    onFilterModule: (String?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        item {
            val isSelected = uiState.filterIsRead == false
            FilterChip(
                selected = isSelected,
                onClick = { onFilterIsRead(if (uiState.filterIsRead == false) null else false) },
                label = { Text(stringResource(R.string.filter_unread)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF131C33),
                    selectedContainerColor = Color(0xFF1E294B),
                    labelColor = Color(0xFF94A3B8),
                    selectedLabelColor = Color(0xFF38BDF8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color(0xFF222D4A),
                    selectedBorderColor = Color(0xFF38BDF8),
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
        item {
            val isSelected = uiState.filterPriority == "alta"
            FilterChip(
                selected = isSelected,
                onClick = { onFilterPriority(if (uiState.filterPriority == "alta") null else "alta") },
                label = { Text(stringResource(R.string.filter_high_priority)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF131C33),
                    selectedContainerColor = Color(0xFF1E294B),
                    labelColor = Color(0xFF94A3B8),
                    selectedLabelColor = Color(0xFF38BDF8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color(0xFF222D4A),
                    selectedBorderColor = Color(0xFF38BDF8),
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
        item {
            val isSelected = uiState.filterModule == "Reportes"
            FilterChip(
                selected = isSelected,
                onClick = { onFilterModule(if (uiState.filterModule == "Reportes") null else "Reportes") },
                label = { Text(stringResource(R.string.filter_reports)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF131C33),
                    selectedContainerColor = Color(0xFF1E294B),
                    labelColor = Color(0xFF94A3B8),
                    selectedLabelColor = Color(0xFF38BDF8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color(0xFF222D4A),
                    selectedBorderColor = Color(0xFF38BDF8),
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
        item {
            val isSelected = uiState.filterModule == "Solicitudes"
            FilterChip(
                selected = isSelected,
                onClick = { onFilterModule(if (uiState.filterModule == "Solicitudes") null else "Solicitudes") },
                label = { Text(stringResource(R.string.filter_requests)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF131C33),
                    selectedContainerColor = Color(0xFF1E294B),
                    labelColor = Color(0xFF94A3B8),
                    selectedLabelColor = Color(0xFF38BDF8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color(0xFF222D4A),
                    selectedBorderColor = Color(0xFF38BDF8),
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
        item {
            val isSelected = uiState.filterModule == "Tareas"
            FilterChip(
                selected = isSelected,
                onClick = { onFilterModule(if (uiState.filterModule == "Tareas") null else "Tareas") },
                label = { Text(stringResource(R.string.filter_tasks)) },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color(0xFF131C33),
                    selectedContainerColor = Color(0xFF1E294B),
                    labelColor = Color(0xFF94A3B8),
                    selectedLabelColor = Color(0xFF38BDF8)
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = Color(0xFF222D4A),
                    selectedBorderColor = Color(0xFF38BDF8),
                    borderWidth = 1.dp,
                    selectedBorderWidth = 1.dp
                )
            )
        }
    }
}

@Composable
private fun ShimmerNotificationsList() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(5) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF131C33).copy(alpha = 0.6f)
                ),
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.4f)
                            .height(16.dp)
                            .background(Color(0xFF1E294B), shape = MaterialTheme.shapes.small)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(14.dp)
                            .background(Color(0xFF1E294B), shape = MaterialTheme.shapes.small)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(12.dp)
                            .background(Color(0xFF1E294B), shape = MaterialTheme.shapes.small)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationsLazyList(
    notifications: List<Notification>,
    onNotificationClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = notifications,
            key = { it.id }
        ) { notification ->
            NotificationCard(
                item = notification,
                onClick = { onNotificationClick(notification.id) }
            )
        }
    }
}

@Composable
private fun EmptyNotificationsView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF475569) // Color pizarra
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_notifications_title),
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFFF8FAFC),
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.empty_notifications_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF94A3B8)
        )
    }
}

