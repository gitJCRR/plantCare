package com.tareaandroid.plantcare.ui.auth

/**
 * Validaciones de los formularios de login y registro. Se comprueban en la app antes de
 * llamar a Firebase para dar una respuesta inmediata al usuario. Es Kotlin puro, sin
 * dependencias de Android, para poder probarlo con pruebas unitarias.
 */
object AuthValidator {

    /** Firebase exige al menos 6 caracteres. */
    const val MIN_PASSWORD_LENGTH = 6

    private val emailRegex = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun isValidPassword(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH
}
