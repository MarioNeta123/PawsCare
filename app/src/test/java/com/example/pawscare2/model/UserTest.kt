package com.example.pawscare2.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class UserTest {

    @Test
    fun testUserDefaultValues() {
        val user = User()
        assertEquals("", user.id)
        assertEquals("", user.name)
        assertEquals("", user.email)
        assertEquals("", user.phone)
        assertEquals("", user.address)
        assertEquals("", user.photoUrl)
        assertFalse(user.welcomeSent)
        assertEquals("CUSTOMER", user.role)
    }

    @Test
    fun testUserCustomValues() {
        val user = User(
            id = "user123",
            name = "Dr. Mario",
            email = "mario@pawscare.com",
            phone = "5512345678",
            address = "Av. Insurgentes 123",
            photoUrl = "https://example.com/photo.jpg",
            welcomeSent = true,
            role = "VETERINARIAN"
        )
        assertEquals("user123", user.id)
        assertEquals("Dr. Mario", user.name)
        assertEquals("mario@pawscare.com", user.email)
        assertEquals("5512345678", user.phone)
        assertEquals("Av. Insurgentes 123", user.address)
        assertEquals("https://example.com/photo.jpg", user.photoUrl)
        assertEquals("VETERINARIAN", user.role)
    }
}
