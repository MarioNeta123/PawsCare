package com.example.pawscare2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawscare2.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel que gestiona el estado de la aplicación PawsCare2 vinculado a Firebase.
 */
class PawsViewModel : ViewModel() {

    private val repository = PawsRepository()
    private val authManager = PawsAuthManager()

    // Estados de la UI recolectados desde Firebase
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

    private val _appointments = MutableStateFlow<List<Appointment>>(emptyList())
    val appointments: StateFlow<List<Appointment>> = _appointments.asStateFlow()

    private val _procedures = MutableStateFlow<List<Procedure>>(emptyList())
    val procedures: StateFlow<List<Procedure>> = _procedures.asStateFlow()

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    // Contador de notificaciones sin leer
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    init {
        observeAuthState()
    }

    /**
     * Observa si hay un usuario logueado para cargar sus datos automáticamente.
     */
    private fun observeAuthState() {
        viewModelScope.launch {
            authManager.getCurrentUserFlow().collect { firebaseUser ->
                if (firebaseUser != null) {
                    loadDataForUser(firebaseUser.uid)
                } else {
                    clearData()
                }
            }
        }
    }

    /**
     * Carga todos los datos vinculados al usuario desde Firestore en tiempo real.
     */
    private fun loadDataForUser(userId: String) {
        // Cargar Perfil del Usuario
        viewModelScope.launch {
            repository.getUser(userId).collect { userData ->
                _user.value = userData
                // Si el usuario existe pero no se le ha enviado el mensaje de bienvenida
                if (userData != null && !userData.welcomeSent) {
                    sendWelcomeNotification(userId)
                }
            }
        }

        // Cargar Mascotas
        viewModelScope.launch {
            repository.getPets(userId).collect { petList ->
                _pets.value = petList
                // Si no hay mascota seleccionada, elegimos la primera de la lista por defecto
                if (_selectedPet.value == null && petList.isNotEmpty()) {
                    selectPet(petList.first())
                }
            }
        }

        // Cargar Citas
        viewModelScope.launch {
            repository.getAppointments(userId).collect { apptList ->
                _appointments.value = apptList
            }
        }

        // Cargar Notificaciones
        viewModelScope.launch {
            repository.getNotifications(userId).collect { notifList ->
                _notifications.value = notifList
                _unreadCount.value = notifList.count { !it.isRead }
            }
        }
    }

    /**
     * Marca una notificación como leída.
     */
    fun markAsRead(notificationId: String) {
        repository.markNotificationAsRead(notificationId)
    }

    /**
     * Envía un mensaje de bienvenida a las notificaciones y marca al usuario.
     */
    private fun sendWelcomeNotification(userId: String) {
        val welcomeNotif = Notification(
            userId = userId,
            title = "¡Bienvenido a PawsCare! 🐾",
            message = "Estamos felices de tenerte aquí. Registra a tus mascotas para empezar a cuidar de su salud.",
            sender = "Soporte PawsCare",
            date = "19 Sep 2026",
            hour = "14:30",
            type = "WELCOME"
        )
        repository.addNotification(welcomeNotif)
        // Marcamos como enviado en Firestore
        viewModelScope.launch {
            repository.updateUserProfile(userId, mapOf("welcomeSent" to true))
        }
    }

    /**
     * Limpia los datos locales cuando se cierra sesión.
     */
    private fun clearData() {
        _pets.value = emptyList()
        _selectedPet.value = null
        _appointments.value = emptyList()
        _procedures.value = emptyList()
        _user.value = null
    }

    /**
     * Cambia la mascota seleccionada y carga sus procedimientos médicos específicos.
     */
    fun selectPet(pet: Pet) {
        _selectedPet.value = pet
        viewModelScope.launch {
            repository.getProcedures(pet.id).collect { procList ->
                _procedures.value = procList
            }
        }
    }

    /**
     * Inicia sesión con Firebase Auth.
     */
    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        authManager.signIn(email, pass, onResult)
    }

    /**
     * Registra un usuario y crea su documento inicial en Firestore.
     */
    fun register(email: String, pass: String, name: String, onResult: (Boolean, String?) -> Unit) {
        authManager.signUp(email, pass) { success, error ->
            if (success) {
                // Si el registro en Auth fue exitoso, creamos el perfil en Firestore con el nombre real
                val userId = authManager.getUserId()
                if (userId != null) {
                    val newUser = User(name = name, email = email)
                    repository.saveUser(userId, newUser)
                }
            }
            onResult(success, error)
        }
    }

    /**
     * Actualiza el perfil del usuario en Firestore.
     */
    fun updateProfile(name: String, phone: String, address: String) {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            val updates = mapOf(
                "name" to name,
                "phone" to phone,
                "address" to address
            )
            repository.updateUserProfile(userId, updates)
        }
    }

    /**
     * Sube una nueva foto de perfil y actualiza la URL en Firestore.
     */
    fun updateProfilePhoto(imageUri: android.net.Uri) {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            val photoUrl = repository.uploadProfileImage(userId, imageUri)
            repository.updateUserProfile(userId, mapOf("photoUrl" to photoUrl))
        }
    }

    /**
     * Cierra la sesión activa.
     */
    fun logout() {
        authManager.signOut()
    }

    /**
     * Registra una nueva mascota vinculada al usuario actual.
     */
    fun addPet(name: String, breed: String, age: Int, weight: Double) {
        val userId = authManager.getUserId() ?: return
        val newPet = Pet(
            ownerId = userId,
            id = System.currentTimeMillis(), // ID temporal basado en tiempo
            name = name,
            breed = breed,
            age = age,
            weight = weight,
            gender = "Macho", // Por defecto
            photo = R.drawable.ic_launcher_foreground
        )
        viewModelScope.launch {
            repository.savePet(newPet)
        }
    }

    /**
     * Guarda una cita en Firestore vinculada al usuario actual y genera una notificación.
     */
    fun addAppointment(appointment: Appointment) {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            // Aseguramos que la cita tenga el ID del usuario actual
            val updatedAppt = appointment.copy(userId = userId)
            repository.saveAppointment(updatedAppt)
            
            // Generar notificación automática para que aparezca en "Alertas Recientes"
            // Buscamos el nombre de la mascota para personalizar el título
            val petName = _pets.value.find { it.id == updatedAppt.petId }?.name ?: "tu mascota"
            
            val newNotif = Notification(
                userId = userId,
                title = "Cita Agendada: $petName",
                message = "Tu cita (${updatedAppt.title}) para el día ${updatedAppt.date} a las ${updatedAppt.hour} ha sido confirmada.",
                sender = "Sistema PawsCare",
                // Usamos la fecha y hora actual para el registro de la notificación
                date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es", "ES")).format(java.util.Calendar.getInstance().time),
                hour = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time),
                type = "APPOINTMENT"
            )
            repository.addNotification(newNotif)
        }
    }

    /**
     * Elimina una cita agendada.
     */
    fun cancelAppointment(appointment: Appointment) {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            repository.deleteAppointment(userId, appointment.petId, appointment.date)
        }
    }

    /**
     * Elimina el perfil de una mascota.
     */
    fun removePet(pet: Pet) {
        viewModelScope.launch {
            repository.deletePet(pet.id)
            if (_selectedPet.value?.id == pet.id) {
                _selectedPet.value = _pets.value.firstOrNull { it.id != pet.id }
            }
        }
    }

    /**
     * Marca un procedimiento de la cartilla como completado.
     */
    fun completeProcedure(procedure: Procedure) {
        viewModelScope.launch {
            repository.markProcedureAsCompleted(procedure.petId, procedure.name)
        }
    }
}
