package com.watersf.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.watersf.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun login(usernameOrEmail: String, password: String) {
        if (usernameOrEmail.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("El usuario/correo y la contraseña no pueden estar vacíos")
            return
        }

        _loginState.value = LoginState.Loading

        viewModelScope.launch(Dispatchers.IO) {
            authRepository.login(usernameOrEmail, password)
                .onSuccess { user ->
                    _loginState.value = LoginState.Success(user)
                }
                .onFailure { exception ->
                    val errorMsg = exception.localizedMessage ?: "Error de conexión inesperado"
                    _loginState.value = LoginState.Error(errorMsg)
                }
        }
    }

    fun resetState() {
        _loginState.value = LoginState.Idle
    }
}
