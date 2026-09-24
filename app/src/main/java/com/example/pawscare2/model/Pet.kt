package com.example.pawscare2.model

/**
 * Representa una mascota en el sistema vinculado a un usuario.
 */
data class Pet(
    val ownerId: String = "", // UID del dueño obtenido de Firebase Auth
    val id: Long = 0,
    val name: String = "",
    val species: String = "PERRO", // "PERRO", "GATO", "AVE", "CONEJO", "HAMSTER", "OTRO"
    val breed: String = "",
    val age: Int = 0,
    val weight: Double = 0.0,
    val gender: String = "Macho", // "Macho" o "Hembra"
    val microchip: String = "", // Nº de Microchip / Folio de verificación clínica
    val isVerified: Boolean = true,
    val photo: Int = 2131230816 // ID de recurso por defecto
) {
    fun getIconEmoji(): String = when (species.uppercase().trim()) {
        "GATO", "CAT" -> "🐱"
        "AVE", "PAJARO", "PÁJARO", "BIRD" -> "🦜"
        "CONEJO", "RABBIT" -> "🐰"
        "HAMSTER", "HÁMSTER" -> "🐹"
        "PEZ", "FISH" -> "🐠"
        "REPTIL", "REPTILE" -> "🦎"
        else -> "🐶"
    }
}
