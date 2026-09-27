package com.example.pawscare2.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppointmentTest {

    @Test
    fun testAppointmentDefaultValues() {
        val appt = Appointment()
        assertEquals("", appt.id)
        assertEquals("", appt.userId)
        assertEquals(0L, appt.petId)
        assertEquals("", appt.petName)
        assertEquals("", appt.title)
        assertEquals("", appt.date)
        assertEquals("", appt.hour)
        assertEquals("", appt.doctor)
        assertEquals("", appt.branch)
        assertEquals("", appt.notes)
        assertEquals("", appt.type)
        assertFalse(appt.isPast)
        assertEquals(0, appt.progress)
        assertEquals("PENDIENTE", appt.status)
    }

    @Test
    fun testAppointmentGroomingProgress() {
        val appt = Appointment(
            title = "Spa & Baño",
            type = "GROOMING",
            progress = 75,
            status = "EN PROCESO"
        )
        assertEquals("GROOMING", appt.type)
        assertEquals(75, appt.progress)
        assertEquals("EN PROCESO", appt.status)
    }
}
