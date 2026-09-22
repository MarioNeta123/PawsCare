package com.example.pawscare2.model

/**
 * Representa un procedimiento médico o cartilla de vacunación.
 */
data class Procedure(
    val petId: Long = 0,    // ID de la mascota asociada
    val name: String = "",
    val doctor: String = "",
    val date: String = "",
    val isCompleted: Boolean = false
)
