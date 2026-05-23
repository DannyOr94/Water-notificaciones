package com.watersf.app.presentation.notification

import com.watersf.app.domain.model.Notification

data class NotificationListState(
    val isLoading: Boolean = false,
    val notifications: List<Notification> = emptyList(),
    val filterModule: String? = null,
    val filterIsRead: Boolean? = null,
    val filterPriority: String? = null,
    val isOffline: Boolean = false,
    val errorMessage: String? = null
)
