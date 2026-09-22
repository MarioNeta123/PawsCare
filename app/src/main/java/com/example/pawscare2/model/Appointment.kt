package com.example.pawscare2.model

/**
 * Representa una cita programada vinculado a un usuario y mascota.
 */
data class Appointment(
    val userId: String = "",
    val petId: Long = 0,
    val title: String = "",
    val date: String = "",
    val hour: String = "",
    val doctor: String = "",
    val branch: String = "",
    val notes: String = "",
    val type: String = "", // "MEDICAL" o "GROOMING"
    val isPast: Boolean = false
)
