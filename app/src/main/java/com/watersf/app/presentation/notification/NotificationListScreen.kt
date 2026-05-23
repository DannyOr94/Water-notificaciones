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

    // Sincronización inicial automática al entrar a la pantalla
    LaunchedEffect(Unit) {
        viewModel.sync()
    }

    NotificationListContent(
        uiState = uiState,
        onRefresh = { viewModel.sync() },
        onNotificationClick = { id ->
            viewModel.markAsRead(id)
            onNotificationClick(id)
        },
        onFilterIsRead = { viewModel.setFilterIsRead(it) },
        onFilterPriority = { viewModel.setFilterPriority(it) },
        onFilterModule = { viewModel.setFilterModule(it) },
        onClearFilters = { viewModel.clearFilters() },
        onLogout = onLogout
    )
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
                    if (uiState.notifications.isEmpty() && !uiState.isLoading) {
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
            val isSelected = uiState.filterModule == "facturas"
            FilterChip(
                selected = isSelected,
                onClick = { onFilterModule(if (uiState.filterModule == "facturas") null else "facturas") },
                label = { Text(stringResource(R.string.filter_bills)) },
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
