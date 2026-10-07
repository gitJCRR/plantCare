package com.tareaandroid.plantcare.ui.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun validEmails_areAccepted() {
        assertTrue(AuthValidator.isValidEmail("ana@ejemplo.com"))
        assertTrue(AuthValidator.isValidEmail("  juan.ros+plantas@correo.es  "))
    }

    @Test
    fun invalidEmails_areRejected() {
        assertFalse(AuthValidator.isValidEmail(""))
        assertFalse(AuthValidator.isValidEmail("ana"))
        assertFalse(AuthValidator.isValidEmail("ana@ejemplo"))
        assertFalse(AuthValidator.isValidEmail("ana @ejemplo.com"))
    }

    @Test
    fun password_needsAtLeastSixCharacters() {
        assertFalse(AuthValidator.isValidPassword("12345"))
        assertTrue(AuthValidator.isValidPassword("123456"))
    }
}
