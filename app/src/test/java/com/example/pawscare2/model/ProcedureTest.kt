package com.example.pawscare2.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProcedureTest {

    @Test
    fun testProcedureDefaultValues() {
        val proc = Procedure()
        assertEquals("", proc.id)
        assertEquals(0L, proc.petId)
        assertEquals("", proc.name)
        assertEquals("", proc.doctor)
        assertEquals("", proc.date)
        assertFalse(proc.isCompleted)
        assertEquals("", proc.notes)
    }

    @Test
    fun testProcedureCompletedState() {
        val proc = Procedure(
            id = "proc1",
            petId = 101L,
            name = "Vacuna Antirrábica",
            doctor = "Dr. García",
            date = "15 Sep 2026",
            isCompleted = true
        )
        assertEquals("proc1", proc.id)
        assertEquals(101L, proc.petId)
        assertEquals("Vacuna Antirrábica", proc.name)
        assertTrue(proc.isCompleted)
    }
}
