package com.example.pawscare2.model

/**
 * Representa el perfil del usuario/dueño.
 * Los valores por defecto son necesarios para que Firebase pueda convertir los datos automáticamente.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val address: String = "",
    val photoUrl: String = "",
    val welcomeSent: Boolean = false,
    val role: String = "CUSTOMER" // "CUSTOMER", "VETERINARIAN", "WORKER"
)
