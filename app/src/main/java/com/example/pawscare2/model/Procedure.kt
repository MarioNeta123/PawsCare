package com.example.pawscare2.model

import com.google.firebase.firestore.PropertyName

/**
 * Representa un procedimiento médico o cartilla de vacunación.
 */
data class Procedure(
    val id: String = "",
    val petId: Long = 0,    // ID de la mascota asociada
    val name: String = "",
    val doctor: String = "",
    val date: String = "",
    @get:PropertyName("isCompleted") @set:PropertyName("isCompleted") var isCompleted: Boolean = false,
    val notes: String = ""
)
