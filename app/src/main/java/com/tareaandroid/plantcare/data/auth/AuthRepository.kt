package com.tareaandroid.plantcare.data.auth

import com.tareaandroid.plantcare.model.User
import kotlinx.coroutines.flow.Flow

/** Motivos por los que puede fallar una operación de autenticación. */
enum class AuthError {
    INVALID_CREDENTIALS,
    INVALID_EMAIL,
    EMAIL_ALREADY_IN_USE,
    WEAK_PASSWORD,
    NETWORK,
    TOO_MANY_REQUESTS,
    UNKNOWN,
}

sealed interface AuthResult {
    data object Success : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

/**
 * Acceso a la autenticación de usuarios. Los ViewModels dependen de esta interfaz
 * y no saben que por debajo se usa Firebase.
 */
interface AuthRepository {

    /** Usuario actual; emite `null` cuando no hay sesión y se actualiza al entrar o salir. */
    val currentUser: Flow<User?>

    /** Usuario actual en este momento, sin esperar a un Flow. */
    fun currentUserOrNull(): User?

    suspend fun signIn(email: String, password: String): AuthResult

    suspend fun register(email: String, password: String): AuthResult

    suspend fun sendPasswordReset(email: String): AuthResult

    fun signOut()
}
