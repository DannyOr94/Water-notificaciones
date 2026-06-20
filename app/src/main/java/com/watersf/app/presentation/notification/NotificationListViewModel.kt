package com.watersf.app.presentation.notification

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.repository.NotificationRepository
import com.watersf.app.presentation.notification.model.NotificationAlertConfig
import com.watersf.app.presentation.notification.model.NotificationGroupMode
import com.watersf.app.presentation.notification.model.NotificationTab
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val repository: NotificationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isOffline = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    // Filtros observables reactivos
    private val _filterModule = MutableStateFlow<String?>(null)
    private val _filterPriority = MutableStateFlow<String?>(null)
    // isRead lo gobierna la pestaña activa ([REQ-4.2]); arranca alineado a NUEVOS.
    private val _filterIsRead = MutableStateFlow(NotificationTab.NUEVOS.isReadFilter)

    // Pestaña activa ([REQ-4]) y modo de agrupación ([REQ-1]).
    private val _activeTab = MutableStateFlow(NotificationTab.NUEVOS)
    private val _groupMode = MutableStateFlow(NotificationGroupMode.PRIORITY)

    // Flujo para notificaciones entrantes como eventos de un solo uso
    private val _newNotificationEvent = MutableSharedFlow<Notification>(extraBufferCapacity = 64)
    val newNotificationEvent: Flow<Notification> = _newNotificationEvent

    private data class NotifQuery(
        val module: String?,
        val isRead: Boolean?,
        val priority: String?,
        val tab: NotificationTab
    )

    /**
     * Une filtros + pestaña y carga reactivamente desde Room. La pestaña NUEVOS aplica
     * además el filtro de recencia in-memory ([REQ-4.2]); el resto solo usa la query.
     */
    private val _displayedNotifications: Flow<List<Notification>> = combine(
        _filterModule,
        _filterIsRead,
        _filterPriority,
        _activeTab
    ) { module, isRead, priority, tab ->
        NotifQuery(module, isRead, priority, tab)
    }.flatMapLatest { query ->
        repository.getNotificationsFlow(query.priority, query.isRead, query.module).map { list ->
            if (query.tab == NotificationTab.NUEVOS) {
                list.filter { NotificationAlertConfig.isWithinNewWindow(it.createdAt) }
            } else {
                list
            }
        }
    }

    /**
     * Estado consolidado expuesto a Compose UI. El badge ([unreadCount]) se deriva de
     * [NotificationRepository.observeUnreadCount] (Flow de Room) para garantizar el
     * incremento real 5→6 sin contador en memoria ([REQ-3.2], [INV-4]).
     */
    val state: StateFlow<NotificationListState> = combine(
        listOf(
            _isLoading,
            _displayedNotifications,
            _filterModule,
            _filterIsRead,
            _filterPriority,
            _isOffline,
            _error,
            repository.observeUnreadCount(),
            _activeTab,
            _groupMode
        )
    ) { array ->
        @Suppress("UNCHECKED_CAST")
        NotificationListState(
            isLoading = array[0] as Boolean,
            notifications = array[1] as List<Notification>,
            filterModule = array[2] as String?,
            filterIsRead = array[3] as Boolean?,
            filterPriority = array[4] as String?,
            isOffline = array[5] as Boolean,
            errorMessage = array[6] as String?,
            unreadCount = array[7] as Int,
            activeTab = array[8] as NotificationTab,
            groupMode = array[9] as NotificationGroupMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationListState(isLoading = true)
    )

    init {
        listenForNewNotifications()
    }

    private fun listenForNewNotifications() {
        viewModelScope.launch {
            repository.newNotificationFlow.collect { notification ->
                _newNotificationEvent.emit(notification)
            }
        }
    }

    fun resetSyncState() {
        repository.resetSyncState()
    }

    fun sync(isBackground: Boolean = false) {
        viewModelScope.launch {
            if (!isBackground) {
                _isLoading.value = true
                _error.value = null
            }

            repository.syncNotifications()
                .onSuccess {
                    _isOffline.value = false
                }
                .onFailure { exception ->
                    _isOffline.value = true
                    if (!isBackground) {
                        _error.value = exception.localizedMessage
                    }
                }

            if (!isBackground) {
                _isLoading.value = false
            }
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    /** [REQ-4] Cambia la pestaña y alinea el filtro isRead que la respalda. */
    fun setActiveTab(tab: NotificationTab) {
        _activeTab.value = tab
        _filterIsRead.value = tab.isReadFilter
    }

    /** [REQ-1] Cambia el modo de agrupación (presentacional). */
    fun setGroupMode(mode: NotificationGroupMode) {
        _groupMode.value = mode
    }

    fun setFilterModule(module: String?) {
        _filterModule.value = module
    }

    fun setFilterPriority(priority: String?) {
        _filterPriority.value = priority
    }

    /** Limpia solo los filtros secundarios (módulo/prioridad); la pestaña gobierna isRead. */
    fun clearFilters() {
        _filterModule.value = null
        _filterPriority.value = null
    }
}
