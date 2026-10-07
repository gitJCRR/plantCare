package com.tareaandroid.plantcare.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tareaandroid.plantcare.data.auth.AuthRepository
import com.tareaandroid.plantcare.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Sesión de la app. Firebase recuerda el usuario entre ejecuciones, así que al abrir la
 * app ya se sabe si hay sesión y se puede empezar directamente en «Mis plantas».
 */
@HiltViewModel
class SessionViewModel @Inject constructor(
    authRepository: AuthRepository,
) : ViewModel() {

    val currentUser: StateFlow<User?> = authRepository.currentUser.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        // Valor inicial síncrono: evita mostrar el login un instante si ya hay sesión
        initialValue = authRepository.currentUserOrNull(),
    )
}
