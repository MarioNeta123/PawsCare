package com.example.pawscare2.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class NotificationTest {

    @Test
    fun testNotificationDefaultValues() {
        val notif = Notification()
        assertEquals("", notif.id)
        assertEquals("", notif.userId)
        assertEquals("", notif.title)
        assertEquals("", notif.message)
        assertEquals("PawsCare Team", notif.sender)
        assertEquals("", notif.date)
        assertEquals("", notif.hour)
        assertEquals("INFO", notif.type)
        assertFalse(notif.isRead)
    }

    @Test
    fun testGetIconEmojiWelcome() {
        val notif = Notification(type = "WELCOME")
        assertEquals("🐾", notif.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiVaccine() {
        val notif = Notification(type = "VACCINE")
        assertEquals("💉", notif.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiBath() {
        val notif = Notification(type = "BATH")
        assertEquals("🛁", notif.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiAppointment() {
        val notif = Notification(type = "APPOINTMENT")
        assertEquals("🩺", notif.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiAlert() {
        val notif = Notification(type = "ALERT")
        assertEquals("⚠️", notif.getIconEmoji())
    }

    @Test
    fun testGetIconEmojiFallback() {
        val notif = Notification(type = "OTRO")
        assertEquals("🔔", notif.getIconEmoji())
    }
}
