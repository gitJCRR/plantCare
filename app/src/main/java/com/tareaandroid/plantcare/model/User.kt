package com.tareaandroid.plantcare.model

/** Usuario con la sesión iniciada. [uid] identifica sus plantas en la base de datos. */
data class User(
    val uid: String,
    val email: String?,
)
