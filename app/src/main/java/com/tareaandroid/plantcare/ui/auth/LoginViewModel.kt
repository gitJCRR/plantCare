package com.tareaandroid.plantcare.ui.auth

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tareaandroid.plantcare.R
import com.tareaandroid.plantcare.data.auth.AuthRepository
import com.tareaandroid.plantcare.data.auth.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    /** Error devuelto por Firebase (credenciales, red…). */
    @StringRes val generalError: Int? = null,
    val isLoading: Boolean = false,
    val loggedIn: Boolean = false,
)

sealed interface LoginEvent {
    data class EmailChanged(val value: String) : LoginEvent
    data class PasswordChanged(val value: String) : LoginEvent
    data object Submit : LoginEvent
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.value, emailError = null, generalError = null) }
            is LoginEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value, passwordError = null, generalError = null) }
            LoginEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = if (AuthValidator.isValidEmail(state.email)) null else R.string.auth_error_invalid_email
        val passwordError = if (state.password.isEmpty()) R.string.auth_error_password_required else null
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            when (val result = authRepository.signIn(state.email, state.password)) {
                AuthResult.Success -> _uiState.update { it.copy(isLoading = false, loggedIn = true) }
                is AuthResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, generalError = result.error.messageRes) }
            }
        }
    }
}
