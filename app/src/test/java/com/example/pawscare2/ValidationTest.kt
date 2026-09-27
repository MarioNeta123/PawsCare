package com.example.pawscare2

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    private val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,10}$")
    private val blockedDomains = listOf(
        "mailinator.com", "yopmail.com", "tempmail.com", "10minutemail.com",
        "trashmail.com", "guerrillamail.com", "dispostable.com", "sharklasers.com",
        "test.com", "example.com", "fake.com", "dummy.com", "temp.com"
    )

    private fun isValidEmail(email: String): Boolean {
        val clean = email.trim().lowercase()
        if (!emailRegex.matches(clean)) return false
        val domain = clean.substringAfter("@", "")
        if (domain in blockedDomains || domain.startsWith("temp") || domain.startsWith("fake")) return false
        return true
    }

    @Test
    fun testValidEmails() {
        assertTrue(isValidEmail("mario@gmail.com"))
        assertTrue(isValidEmail("doctor.garcia@hotmail.com"))
        assertTrue(isValidEmail("contacto@pawscare.com.mx"))
        assertTrue(isValidEmail("user_123@outlook.es"))
    }

    @Test
    fun testMalformedEmails() {
        assertFalse(isValidEmail("mario"))
        assertFalse(isValidEmail("mario@"))
        assertFalse(isValidEmail("mario@gmail"))
        assertFalse(isValidEmail("@gmail.com"))
        assertFalse(isValidEmail("mario gmail.com"))
    }

    @Test
    fun testDisposableAndFakeEmails() {
        assertFalse(isValidEmail("test@yopmail.com"))
        assertFalse(isValidEmail("user@mailinator.com"))
        assertFalse(isValidEmail("admin@tempmail.com"))
        assertFalse(isValidEmail("test@fake.com"))
        assertFalse(isValidEmail("fake@example.com"))
    }

    @Test
    fun testClinicCodeValidation() {
        val validCodes = listOf("VET2026", "PAWS-VET", "PAWS2026")

        fun isValidClinicCode(code: String): Boolean {
            val clean = code.trim().uppercase()
            return clean in validCodes || clean.startsWith("CED-")
        }

        assertTrue(isValidClinicCode("VET2026"))
        assertTrue(isValidClinicCode("PAWS-VET"))
        assertTrue(isValidClinicCode("CED-12345678"))
        assertFalse(isValidClinicCode("1234"))
        assertFalse(isValidClinicCode("INVALID_CODE"))
    }
}
