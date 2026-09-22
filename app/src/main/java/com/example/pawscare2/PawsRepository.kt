package com.example.pawscare2

import com.example.pawscare2.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repositorio que maneja la comunicación con Firebase Firestore y Storage.
 */
class PawsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    /**
     * Guarda o actualiza los datos de un usuario usando su UID como ID de documento.
     */
    fun saveUser(userId: String, user: User) {
        db.collection("users").document(userId).set(user)
    }

    /**
     * Actualiza campos específicos del usuario.
     */
    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        db.collection("users").document(userId).update(updates).await()
    }

    /**
     * Sube una imagen al Storage y devuelve la URL de descarga.
     */
    suspend fun uploadProfileImage(userId: String, imageUri: android.net.Uri): String {
        val ref = storage.reference.child("profile_images/$userId.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    /**
     * Obtiene los datos del usuario en tiempo real.
     */
    fun getUser(userId: String): Flow<User?> = callbackFlow {
        val subscription = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    trySend(snapshot.toObject<User>())
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Obtiene las mascotas de un usuario específico en tiempo real.
     */
    fun getPets(userId: String): Flow<List<Pet>> = callbackFlow {
        val subscription = db.collection("pets")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val pets = snapshot.toObjects<Pet>()
                    trySend(pets)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Obtiene las citas del usuario en tiempo real.
     */
    fun getAppointments(userId: String): Flow<List<Appointment>> = callbackFlow {
        val subscription = db.collection("appointments")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val appointments = snapshot.toObjects<Appointment>()
                    trySend(appointments)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Obtiene los procedimientos de una mascota específica.
     */
    fun getProcedures(petId: Long): Flow<List<Procedure>> = callbackFlow {
        val subscription = db.collection("procedures")
            .whereEqualTo("petId", petId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val procedures = snapshot.toObjects<Procedure>()
                    trySend(procedures)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Guarda una nueva cita en Firestore.
     */
    fun saveAppointment(appointment: Appointment) {
        db.collection("appointments").add(appointment)
    }

    /**
     * Guarda una nueva mascota en Firestore.
     */
    fun savePet(pet: Pet) {
        db.collection("pets").add(pet)
    }

    /**
     * Obtiene las notificaciones del usuario en tiempo real.
     */
    fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val subscription = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val notifications = snapshot.toObjects<Notification>()
                    trySend(notifications)
                }
            }
        awaitClose { subscription.remove() }
    }

    /**
     * Agrega una notificación a la colección.
     */
    fun addNotification(notification: Notification) {
        val ref = db.collection("notifications").document()
        db.collection("notifications").document(ref.id).set(notification.copy(id = ref.id))
    }

    /**
     * Marca una notificación como leída.
     */
    fun markNotificationAsRead(notificationId: String) {
        db.collection("notifications").document(notificationId).update("isRead", true)
    }

    /**
     * Elimina una mascota de Firestore.
     */
    fun deletePet(petId: Long) {
        db.collection("pets").whereEqualTo("id", petId).get().addOnSuccessListener { docs ->
            for (doc in docs) { doc.reference.delete() }
        }
    }

    /**
     * Elimina una cita de Firestore.
     */
    fun deleteAppointment(userId: String, petId: Long, date: String) {
        db.collection("appointments")
            .whereEqualTo("userId", userId)
            .whereEqualTo("petId", petId)
            .whereEqualTo("date", date)
            .get().addOnSuccessListener { docs ->
                for (doc in docs) { doc.reference.delete() }
            }
    }

    /**
     * Actualiza el estado de un procedimiento.
     */
    fun markProcedureAsCompleted(petId: Long, procName: String) {
        db.collection("procedures")
            .whereEqualTo("petId", petId)
            .whereEqualTo("name", procName)
            .get().addOnSuccessListener { docs ->
                for (doc in docs) { doc.reference.update("isCompleted", true) }
            }
    }
}
