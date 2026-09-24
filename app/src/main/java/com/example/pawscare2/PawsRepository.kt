package com.example.pawscare2

import com.example.pawscare2.model.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PawsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    suspend fun saveUser(userId: String, user: User) {
        db.collection("users").document(userId).set(user).await()
    }

    suspend fun updateUserProfile(userId: String, updates: Map<String, Any>) {
        db.collection("users").document(userId).update(updates).await()
    }

    suspend fun uploadProfileImage(userId: String, imageUri: android.net.Uri): String {
        val ref = storage.reference.child("profile_images/$userId.jpg")
        ref.putFile(imageUri).await()
        return ref.downloadUrl.await().toString()
    }

    fun getUser(userId: String): Flow<User?> = callbackFlow {
        val subscription = db.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject<User>()?.copy(id = snapshot.id)
                    trySend(user)
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getPets(userId: String): Flow<List<Pet>> = callbackFlow {
        val subscription = db.collection("pets")
            .whereEqualTo("ownerId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Pet>()
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getAllPets(): Flow<List<Pet>> = callbackFlow {
        val subscription = db.collection("pets")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Pet>()
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getAppointments(userId: String): Flow<List<Appointment>> = callbackFlow {
        val subscription = db.collection("appointments")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Appointment>()?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getAllAppointments(): Flow<List<Appointment>> = callbackFlow {
        val subscription = db.collection("appointments")
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Appointment>()?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    fun getProcedures(petId: Long): Flow<List<Procedure>> = callbackFlow {
        val subscription = db.collection("procedures")
            .whereEqualTo("petId", petId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Procedure>()?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun saveAppointment(appointment: Appointment) {
        val ref = db.collection("appointments").document()
        val apptToSave = appointment.copy(id = ref.id)
        ref.set(apptToSave).await()
    }

    suspend fun updateAppointmentProgress(apptId: String, progress: Int, status: String) {
        if (apptId.isNotBlank()) {
            db.collection("appointments").document(apptId)
                .update(mapOf("progress" to progress, "status" to status)).await()
        }
    }

    suspend fun savePet(pet: Pet) {
        val newPet = if (pet.id == 0L) pet.copy(id = System.currentTimeMillis()) else pet
        db.collection("pets").add(newPet).await()
    }

    suspend fun updatePet(pet: Pet) {
        val docs = db.collection("pets").whereEqualTo("id", pet.id).get().await()
        for (doc in docs) {
            doc.reference.update(
                mapOf(
                    "name" to pet.name,
                    "species" to pet.species,
                    "breed" to pet.breed,
                    "age" to pet.age,
                    "weight" to pet.weight,
                    "gender" to pet.gender,
                    "microchip" to pet.microchip,
                    "isVerified" to pet.isVerified
                )
            ).await()
        }
    }

    fun getNotifications(userId: String): Flow<List<Notification>> = callbackFlow {
        val subscription = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        doc.toObject<Notification>()?.copy(id = doc.id)
                    }
                    trySend(list)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun addNotification(notification: Notification) {
        val ref = db.collection("notifications").document()
        val notifToSave = notification.copy(id = ref.id)
        ref.set(notifToSave).await()
    }

    suspend fun markNotificationAsRead(notificationId: String) {
        if (notificationId.isNotBlank()) {
            db.collection("notifications").document(notificationId)
                .update(mapOf("isRead" to true, "read" to true)).await()
        }
    }

    suspend fun deletePet(petId: Long) {
        val docs = db.collection("pets").whereEqualTo("id", petId).get().await()
        for (doc in docs) { doc.reference.delete().await() }
    }

    suspend fun deleteAppointment(apptId: String) {
        if (apptId.isNotBlank()) {
            db.collection("appointments").document(apptId).delete().await()
        }
    }

    suspend fun markProcedureAsCompleted(petId: Long, procName: String) {
        val docs = db.collection("procedures")
            .whereEqualTo("petId", petId)
            .whereEqualTo("name", procName)
            .get().await()
        for (doc in docs) { doc.reference.update("isCompleted", true).await() }
    }

    suspend fun addProcedure(procedure: Procedure) {
        val ref = db.collection("procedures").document()
        val procToSave = procedure.copy(id = ref.id)
        ref.set(procToSave).await()
    }

    suspend fun markAllNotificationsAsRead(userId: String) {
        val docs = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .get().await()
        for (doc in docs) {
            doc.reference.update(mapOf("isRead" to true, "read" to true)).await()
        }
    }

    suspend fun seedDemoData(userId: String) {
        db.collection("users").document(userId).update(
            mapOf(
                "phone" to "5512345678",
                "address" to "Av. Insurgentes Sur 1234, CDMX",
                "photoUrl" to "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=500"
            )
        ).await()

        val p1 = Pet(ownerId = userId, id = 101L, name = "Monchito", species = "PERRO", breed = "Golden Retriever", age = 3, weight = 28.5, gender = "Macho", microchip = "CHIP-MX98200142", isVerified = true)
        val p2 = Pet(ownerId = userId, id = 102L, name = "Luna", species = "GATO", breed = "Siamés", age = 2, weight = 4.2, gender = "Hembra", microchip = "CHIP-MX98200871", isVerified = true)
        val p3 = Pet(ownerId = userId, id = 103L, name = "Rocky", species = "PERRO", breed = "Bulldog Francés", age = 1, weight = 11.0, gender = "Macho", microchip = "CHIP-MX98200563", isVerified = true)

        listOf(p1, p2, p3).forEach { pet ->
            val existing = db.collection("pets").whereEqualTo("ownerId", userId).whereEqualTo("id", pet.id).get().await()
            if (existing.isEmpty) db.collection("pets").add(pet).await()
        }

        val appts = listOf(
            Appointment(userId = userId, petId = 101L, petName = "Monchito", title = "Consulta General", doctor = "Dr. García (Veterinario)", branch = "Sucursal Norte 📍", date = "28 Sep, 2026", hour = "10:30 AM", notes = "Chequeo de rutina y revisión de articulaciones.", type = "MEDICAL", isPast = false),
            Appointment(userId = userId, petId = 101L, petName = "Monchito", title = "Revisión Odontológica", doctor = "Dra. Martínez (Especialista)", branch = "Sucursal Centro 📍", date = "12 Ago, 2026", hour = "04:00 PM", notes = "Limpieza dental preventiva realizada.", type = "MEDICAL", isPast = true),
            Appointment(userId = userId, petId = 102L, petName = "Luna", title = "Spa & Masaje 💆", doctor = "Estilista Canino", branch = "Sucursal Sur 📍", date = "23 Sep, 2026", hour = "12:00 PM", notes = "Shampoo hipoalergénico y cepillado suave.", type = "GROOMING", progress = 75, status = "EN PROCESO", isPast = false)
        )
        appts.forEach { saveAppointment(it) }

        val procs = listOf(
            Procedure(petId = 101L, name = "Vacuna Antirrábica", doctor = "Dr. García", date = "15 Ene 2026", isCompleted = true),
            Procedure(petId = 101L, name = "Desparasitación Interna", doctor = "Dra. Martínez", date = "10 May 2026", isCompleted = true),
            Procedure(petId = 101L, name = "Refuerzo Quíntuple Canina", doctor = "Dr. García", date = "15 Oct 2026", isCompleted = false),
            Procedure(petId = 102L, name = "Vacuna Triple Felina", doctor = "Dra. Martínez", date = "01 Mar 2026", isCompleted = true),
            Procedure(petId = 102L, name = "Desparasitación Semestral", doctor = "Dr. García", date = "30 Sep 2026", isCompleted = false)
        )
        procs.forEach { addProcedure(it) }

        val notifs = listOf(
            Notification(userId = userId, title = "¡Luna está en proceso de secado! 🛁", message = "El servicio de spa para Luna va al 75%. Estará lista muy pronto.", type = "BATH", date = "23 Sep 2026", hour = "12:15 PM", isRead = false),
            Notification(userId = userId, title = "Recordatorio: Vacuna Pendiente 💉", message = "Monchito tiene pendiente el Refuerzo Quíntuple Canina este mes.", type = "VACCINE", date = "22 Sep 2026", hour = "09:00 AM", isRead = false),
            Notification(userId = userId, title = "Cita Confirmada con Dr. García 🩺", message = "Tu consulta general para Monchito quedó agendada para el 28 Sep a las 10:30 AM.", type = "APPOINTMENT", date = "20 Sep 2026", hour = "11:00 AM", isRead = true),
            Notification(userId = userId, title = "¡Bienvenido a PawsCare! 🐾", message = "Estamos felices de cuidar a Monchito, Luna y Rocky.", type = "WELCOME", date = "01 Sep 2026", hour = "08:00 AM", isRead = true)
        )
        notifs.forEach { addNotification(it) }
    }
}
