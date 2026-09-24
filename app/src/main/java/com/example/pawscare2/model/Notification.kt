package com.example.pawscare2.model

import com.google.firebase.firestore.PropertyName

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
    val type: String = "INFO", // INFO, VACCINE, BATH, WELCOME, APPOINTMENT, ALERT
    @get:PropertyName("isRead") @set:PropertyName("isRead") var isRead: Boolean = false
) {
    fun getIconEmoji(): String = when (type.uppercase()) {
        "WELCOME" -> "🐾"
        "VACCINE" -> "💉"
        "BATH" -> "🛁"
        "APPOINTMENT" -> "🩺"
        "ALERT" -> "⚠️"
        else -> "🔔"
    }
}
