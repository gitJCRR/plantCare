package com.tareaandroid.plantcare.data.auth

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.tareaandroid.plantcare.model.User
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Implementación de [AuthRepository] con Firebase Authentication (email y contraseña). */
@Singleton
class FirebaseAuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
) : AuthRepository {

    // Convierte el listener de Firebase en un Flow: emite cada vez que cambia la sesión
    override val currentUser: Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toUser()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }.distinctUntilChanged()

    override fun currentUserOrNull(): User? = auth.currentUser?.toUser()

    override suspend fun signIn(email: String, password: String): AuthResult =
        runCatchingAuth { auth.signInWithEmailAndPassword(email.trim(), password).await() }

    override suspend fun register(email: String, password: String): AuthResult =
        runCatchingAuth { auth.createUserWithEmailAndPassword(email.trim(), password).await() }

    override suspend fun sendPasswordReset(email: String): AuthResult =
        runCatchingAuth { auth.sendPasswordResetEmail(email.trim()).await() }

    override fun signOut() = auth.signOut()

    private suspend fun runCatchingAuth(block: suspend () -> Unit): AuthResult =
        try {
            block()
            AuthResult.Success
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            AuthResult.Failure(e.toAuthError())
        }

    /** Traduce las excepciones de Firebase a errores propios que la UI sabe mostrar. */
    private fun Exception.toAuthError(): AuthError = when (this) {
        is FirebaseAuthWeakPasswordException -> AuthError.WEAK_PASSWORD
        is FirebaseAuthUserCollisionException -> AuthError.EMAIL_ALREADY_IN_USE
        is FirebaseAuthInvalidUserException -> AuthError.INVALID_CREDENTIALS
        is FirebaseAuthInvalidCredentialsException ->
            if (errorCode == "ERROR_INVALID_EMAIL") AuthError.INVALID_EMAIL else AuthError.INVALID_CREDENTIALS
        is FirebaseNetworkException -> AuthError.NETWORK
        is FirebaseTooManyRequestsException -> AuthError.TOO_MANY_REQUESTS
        else -> AuthError.UNKNOWN
    }

    private fun FirebaseUser.toUser() = User(uid = uid, email = email)
}
