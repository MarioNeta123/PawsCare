package com.example.pawscare2.model

/**
 * Representa una mascota en el sistema vinculado a un usuario.
 */
data class Pet(
    val ownerId: String = "", // UID del dueño obtenido de Firebase Auth
    val id: Long = 0,
    val name: String = "",
    val breed: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val gender: String = "", // "Macho" o "Hembra"
    val photo: Int = 2131230816 // ID de recurso por defecto (drawable)
)
