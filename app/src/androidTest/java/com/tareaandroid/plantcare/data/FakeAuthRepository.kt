package com.tareaandroid.plantcare.data

import com.tareaandroid.plantcare.data.auth.AuthRepository
import com.tareaandroid.plantcare.data.auth.AuthResult
import com.tareaandroid.plantcare.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** Sustituto de Firebase para las pruebas: la sesión se cambia a mano. */
class FakeAuthRepository(initialUser: User?) : AuthRepository {

    private val user = MutableStateFlow(initialUser)

    override val currentUser: StateFlow<User?> = user

    override fun currentUserOrNull(): User? = user.value

    fun switchTo(newUser: User?) {
        user.value = newUser
    }

    override suspend fun signIn(email: String, password: String) = AuthResult.Success

    override suspend fun register(email: String, password: String) = AuthResult.Success

    override suspend fun sendPasswordReset(email: String) = AuthResult.Success

    override fun signOut() = switchTo(null)
}
