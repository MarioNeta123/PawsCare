package com.example.pawscare2.model

/**
 * Clase para representar una notificación en la aplicación.
 */
data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val sender: String = "PawsCare Team",
    val date: String = "",
    val hour: String = "",
    val type: String = "INFO", // INFO, VACCINE, BATH, WELCOME
    val isRead: Boolean = false
)
