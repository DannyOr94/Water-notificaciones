package com.watersf.app.presentation.auth

import com.watersf.app.domain.model.User

sealed interface LoginState {
    object Idle : LoginState
    object Loading : LoginState
    data class Success(val user: User) : LoginState
    data class Error(val message: String) : LoginState
}
