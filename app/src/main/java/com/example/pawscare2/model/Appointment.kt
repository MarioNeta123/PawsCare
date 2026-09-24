package com.example.pawscare2.model

import com.google.firebase.firestore.PropertyName

data class Appointment(
    val id: String = "", // ID del documento en Firestore
    val userId: String = "",
    val petId: Long = 0,
    val petName: String = "", // Facilita la vista para el veterinario
    val title: String = "",
    val date: String = "",
    val hour: String = "",
    val doctor: String = "",
    val branch: String = "",
    val notes: String = "",
    val type: String = "", // "MEDICAL" o "GROOMING"
    @get:PropertyName("isPast") @set:PropertyName("isPast") var isPast: Boolean = false,
    val progress: Int = 0, // Progreso del baño (0 a 100)
    val status: String = "PENDIENTE" // PENDIENTE, EN PROCESO, LISTO
)
