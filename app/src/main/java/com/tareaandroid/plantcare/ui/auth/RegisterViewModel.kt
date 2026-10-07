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

data class RegisterUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    @StringRes val emailError: Int? = null,
    @StringRes val passwordError: Int? = null,
    @StringRes val confirmError: Int? = null,
    @StringRes val generalError: Int? = null,
    val isLoading: Boolean = false,
    val registered: Boolean = false,
)

sealed interface RegisterEvent {
    data class EmailChanged(val value: String) : RegisterEvent
    data class PasswordChanged(val value: String) : RegisterEvent
    data class ConfirmPasswordChanged(val value: String) : RegisterEvent
    data object Submit : RegisterEvent
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEvent(event: RegisterEvent) {
        when (event) {
            is RegisterEvent.EmailChanged ->
                _uiState.update { it.copy(email = event.value, emailError = null, generalError = null) }
            is RegisterEvent.PasswordChanged ->
                _uiState.update { it.copy(password = event.value, passwordError = null, generalError = null) }
            is RegisterEvent.ConfirmPasswordChanged ->
                _uiState.update { it.copy(confirmPassword = event.value, confirmError = null, generalError = null) }
            RegisterEvent.Submit -> submit()
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        val emailError = if (AuthValidator.isValidEmail(state.email)) null else R.string.auth_error_invalid_email
        val passwordError = if (AuthValidator.isValidPassword(state.password)) null else R.string.auth_error_weak_password
        val confirmError = if (state.confirmPassword == state.password) null else R.string.auth_error_passwords_mismatch
        if (emailError != null || passwordError != null || confirmError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passwordError, confirmError = confirmError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, generalError = null) }
        viewModelScope.launch {
            // Firebase deja la sesión iniciada tras crear la cuenta
            when (val result = authRepository.register(state.email, state.password)) {
                AuthResult.Success -> _uiState.update { it.copy(isLoading = false, registered = true) }
                is AuthResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, generalError = result.error.messageRes) }
            }
        }
    }
}
