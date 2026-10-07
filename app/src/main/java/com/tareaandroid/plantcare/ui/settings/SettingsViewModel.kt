package com.tareaandroid.plantcare.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tareaandroid.plantcare.data.auth.AuthRepository
import com.tareaandroid.plantcare.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class SettingsUiState(val email: String? = null)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = authRepository.currentUser
        .map { SettingsUiState(email = it?.email) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            SettingsUiState(authRepository.currentUserOrNull()?.email),
        )

    fun signOut() = authRepository.signOut()

    /** Ejecuta ya la comprobación de riego para ver la notificación sin esperar al día siguiente. */
    fun testReminder() = reminderScheduler.runNow()
}
