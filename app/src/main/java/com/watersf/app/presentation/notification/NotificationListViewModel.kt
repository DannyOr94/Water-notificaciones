package com.watersf.app.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watersf.app.domain.model.Notification
import com.watersf.app.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationListViewModel @Inject constructor(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _isOffline = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    // Filtros observables reactivos
    private val _filterModule = MutableStateFlow<String?>(null)
    private val _filterIsRead = MutableStateFlow<Boolean?>(null)
    private val _filterPriority = MutableStateFlow<String?>(null)

    /**
     * Une los flujos de filtros y carga reactivamente las notificaciones de Room.
     */
    private val _notifications = combine(
        _filterModule,
        _filterIsRead,
        _filterPriority
    ) { module, isRead, priority ->
        Triple(module, isRead, priority)
    }.flatMapLatest { (module, isRead, priority) ->
        repository.getNotificationsFlow(priority, isRead, module)
    }

    /**
     * Estado consolidado expuesto a Compose UI
     */
    val state: StateFlow<NotificationListState> = kotlinx.coroutines.flow.combine(
        listOf(_isLoading, _notifications, _filterModule, _filterIsRead, _filterPriority, _isOffline, _error)
    ) { array ->
        NotificationListState(
            isLoading = array[0] as Boolean,
            notifications = array[1] as List<Notification>,
            filterModule = array[2] as String?,
            filterIsRead = array[3] as Boolean?,
            filterPriority = array[4] as String?,
            isOffline = array[5] as Boolean,
            errorMessage = array[6] as String?
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = NotificationListState(isLoading = true)
    )

    init {
        sync()
    }

    fun sync() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            repository.syncNotifications()
                .onSuccess {
                    _isOffline.value = false
                }
                .onFailure { exception ->
                    _isOffline.value = true
                    _error.value = exception.localizedMessage
                }

            _isLoading.value = false
        }
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            repository.markAsRead(id)
        }
    }

    fun setFilterModule(module: String?) {
        _filterModule.value = module
    }

    fun setFilterIsRead(isRead: Boolean?) {
        _filterIsRead.value = isRead
    }

    fun setFilterPriority(priority: String?) {
        _filterPriority.value = priority
    }

    fun clearFilters() {
        _filterModule.value = null
        _filterIsRead.value = null
        _filterPriority.value = null
    }
}