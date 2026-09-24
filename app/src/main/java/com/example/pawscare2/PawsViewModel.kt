package com.example.pawscare2

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pawscare2.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class PawsViewModel : ViewModel() {

    private val repository = PawsRepository()
    private val authManager = PawsAuthManager()

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

    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    init { observeAuthState() }

    fun clearError() { _errorMessage.value = null }
    fun clearToast() { _toastMessage.value = null }

    private fun observeAuthState() {
        viewModelScope.launch {
            try {
                authManager.getCurrentUserFlow().collect { firebaseUser ->
                    if (firebaseUser != null) loadDataForUser(firebaseUser.uid)
                    else clearData()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error de autenticación: ${e.localizedMessage}"
            }
        }
    }

    private fun loadDataForUser(userId: String) {
        viewModelScope.launch {
            repository.getUser(userId).catch { e -> _errorMessage.value = "Error al cargar usuario: ${e.message}" }
                .collect { userData ->
                    _user.value = userData
                    if (userData != null && !userData.welcomeSent) sendWelcomeNotification(userId)

                    if (userData?.role == "VETERINARIAN") {
                        repository.getAllAppointments().collect { _appointments.value = it }
                    } else {
                        repository.getAppointments(userId).collect { _appointments.value = it }
                    }
                }
        }

        viewModelScope.launch {
            repository.getUser(userId).collect { userData ->
                if (userData?.role == "VETERINARIAN") {
                    repository.getAllPets().catch { e -> _errorMessage.value = "Error al cargar pacientes: ${e.message}" }
                        .collect { petList ->
                            _pets.value = petList
                            if (_selectedPet.value == null && petList.isNotEmpty()) selectPet(petList.first())
                        }
                } else {
                    repository.getPets(userId).catch { e -> _errorMessage.value = "Error al cargar mascotas: ${e.message}" }
                        .collect { petList ->
                            _pets.value = petList
                            if (_selectedPet.value == null && petList.isNotEmpty()) selectPet(petList.first())
                        }
                }
            }
        }

        viewModelScope.launch {
            repository.getNotifications(userId).catch { e -> _errorMessage.value = "Error al cargar notificaciones: ${e.message}" }
                .collect { notifList ->
                    _notifications.value = notifList
                    _unreadCount.value = notifList.count { !it.isRead }
                }
        }
    }

    fun toggleUserRole() {
        val u = _user.value ?: return
        val newRole = if (u.role == "VETERINARIAN") "CUSTOMER" else "VETERINARIAN"
        viewModelScope.launch {
            try {
                repository.updateUserProfile(u.id, mapOf("role" to newRole))
                _toastMessage.value = if (newRole == "VETERINARIAN") "¡Modo Veterinario Activado (Dr(a). ${u.name})! 🩺" else "¡Modo Cliente Activado! 🐶"
            } catch (e: Exception) {
                _errorMessage.value = "Error al cambiar de rol: ${e.message}"
            }
        }
    }

    fun sendBroadcastNotification(title: String, message: String, type: String = "ALERT") {
        val u = _user.value ?: return
        viewModelScope.launch {
            try {
                val cleanTitle = title.trim()
                val cleanMessage = message.trim()
                require(cleanTitle.isNotBlank() && cleanMessage.isNotBlank()) { "Título y mensaje son obligatorios." }

                val notif = Notification(
                    userId = u.id,
                    title = "📢 $cleanTitle",
                    message = cleanMessage,
                    type = type,
                    date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.forLanguageTag("es")).format(java.util.Calendar.getInstance().time),
                    hour = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                )
                repository.addNotification(notif)
                _toastMessage.value = "¡Aviso clínico enviado a la base de datos! 📢"
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar aviso: ${e.message}"
            }
        }
    }

    fun markAsRead(notificationId: String) {
        val target = _notifications.value.find { it.id == notificationId }
        if (target != null && target.isRead) return

        viewModelScope.launch {
            try {
                repository.markNotificationAsRead(notificationId)
                _toastMessage.value = "Notificación marcada como leída en la base de datos."
            } catch (e: Exception) {
                _errorMessage.value = "No se pudo actualizar la notificación: ${e.message}"
            }
        }
    }

    private fun sendWelcomeNotification(userId: String) {
        viewModelScope.launch {
            try {
                val welcomeNotif = Notification(
                    userId = userId, title = "¡Bienvenido a PawsCare! 🐾",
                    message = "Estamos felices de tenerte aquí. Registra a tus mascotas para empezar.",
                    type = "WELCOME",
                    date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es", "ES")).format(java.util.Calendar.getInstance().time),
                    hour = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                )
                repository.addNotification(welcomeNotif)
                repository.updateUserProfile(userId, mapOf("welcomeSent" to true))
            } catch (e: Exception) {
                _errorMessage.value = "Error al enviar bienvenida: ${e.message}"
            }
        }
    }

    private fun clearData() {
        _pets.value = emptyList(); _selectedPet.value = null
        _appointments.value = emptyList(); _procedures.value = emptyList()
        _user.value = null; _notifications.value = emptyList()
    }

    fun selectPet(pet: Pet) {
        _selectedPet.value = pet
        viewModelScope.launch {
            repository.getProcedures(pet.id).catch { e -> _errorMessage.value = "Error al cargar historial médico." }
                .collect { _procedures.value = it }
        }
    }

    private fun validateEmail(email: String) {
        val clean = email.trim().lowercase()
        val emailRegex = Regex("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,10}$")
        require(emailRegex.matches(clean)) {
            "Ingresa un correo electrónico válido con formato correcto (ejemplo: usuario@gmail.com)."
        }

        val domain = clean.substringAfter("@", "")
        val blockedDomains = listOf(
            "mailinator.com", "yopmail.com", "tempmail.com", "10minutemail.com",
            "trashmail.com", "guerrillamail.com", "dispostable.com", "sharklasers.com",
            "test.com", "example.com", "fake.com", "dummy.com", "temp.com"
        )

        require(domain !in blockedDomains && !domain.startsWith("temp") && !domain.startsWith("fake")) {
            "No se permiten correos temporales o falsos ($domain). Usa un correo real (Gmail, Outlook, Yahoo, Hotmail, etc.)."
        }
    }

    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        val cleanEmail = email.trim()
        if (cleanEmail.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Correo y contraseña son obligatorios."
            return
        }
        try {
            validateEmail(cleanEmail)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            return
        }
        authManager.signIn(cleanEmail, pass, onResult)
    }

    fun register(email: String, pass: String, name: String, role: String = "CUSTOMER", clinicCode: String = "", onResult: (Boolean, String?) -> Unit) {
        val cleanEmail = email.trim()
        val cleanName = name.trim()
        val cleanClinicCode = clinicCode.trim().uppercase()

        if (cleanEmail.isBlank() || pass.isBlank() || cleanName.isBlank()) {
            _errorMessage.value = "Todos los campos son obligatorios."
            return
        }
        try {
            validateEmail(cleanEmail)
        } catch (e: Exception) {
            _errorMessage.value = e.message
            return
        }
        if (pass.length < 6) {
            _errorMessage.value = "La contraseña debe tener al menos 6 caracteres."
            return
        }

        if (role == "VETERINARIAN") {
            val validCodes = listOf("VET2026", "PAWS-VET", "PAWS2026")
            if (cleanClinicCode !in validCodes && !cleanClinicCode.startsWith("CED-")) {
                _errorMessage.value = "Código de autorización clínica no válido. Solicita el código VET2026 a la administración."
                return
            }
        }

        authManager.signUp(cleanEmail, pass) { success, error ->
            if (success) {
                val userId = authManager.getUserId()
                if (userId != null) {
                    viewModelScope.launch {
                        try {
                            repository.saveUser(userId, User(name = cleanName, email = cleanEmail, role = role))
                            _toastMessage.value = if (role == "VETERINARIAN") "¡Cuenta de Veterinario verificada y creada exitosamente! 🩺" else "¡Cuenta creada exitosamente! 🐾"
                        } catch (e: Exception) {
                            _errorMessage.value = "Cuenta creada, pero hubo un error guardando el perfil."
                        }
                    }
                }
            }
            onResult(success, error)
        }
    }

    fun updateProfile(name: String, phone: String, address: String) {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                val cleanName = name.trim()
                val cleanPhone = phone.trim()
                val cleanAddress = address.trim()
                require(cleanName.length >= 2) { "El nombre debe tener al menos 2 caracteres." }
                require(cleanPhone.isEmpty() || cleanPhone.matches(Regex("^[0-9]{7,15}$"))) { "El teléfono debe contener entre 7 y 15 números." }
                require(cleanAddress.isEmpty() || cleanAddress.length >= 4) { "La dirección debe tener al menos 4 caracteres." }

                repository.updateUserProfile(userId, mapOf("name" to cleanName, "phone" to cleanPhone, "address" to cleanAddress))
                _toastMessage.value = "¡Perfil actualizado correctamente en la base de datos! 👤"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar perfil: ${e.message}"
            }
        }
    }

    fun updateProfilePhoto(imageUri: android.net.Uri) {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                val photoUrl = repository.uploadProfileImage(userId, imageUri)
                repository.updateUserProfile(userId, mapOf("photoUrl" to photoUrl))
                _toastMessage.value = "¡Foto de perfil actualizada en la base de datos! 📷"
            } catch (e: Exception) {
                _errorMessage.value = "Error al subir la imagen."
            }
        }
    }

    fun logout() {
        try { authManager.signOut() } catch (e: Exception) { _errorMessage.value = "Error al cerrar sesión." }
    }

    fun addPet(name: String, species: String, breed: String, age: Int, weight: Double, gender: String = "Macho") {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                val cleanName = name.trim()
                val cleanBreed = breed.trim()
                val cleanSpecies = species.uppercase().trim().ifBlank { "PERRO" }

                require(cleanName.length >= 2) { "El nombre de la mascota debe tener al menos 2 letras." }
                require(cleanBreed.isNotEmpty()) { "La raza no puede estar vacía." }

                when (cleanSpecies) {
                    "GATO" -> {
                        require(age in 0..25) { "La edad de un gato debe estar entre 0 y 25 años." }
                        require(weight in 0.2..15.0) { "El peso de un gato debe estar entre 0.2 y 15 kg." }
                    }
                    "AVE", "PAJARO" -> {
                        require(age in 0..50) { "La edad debe estar entre 0 y 50 años." }
                        require(weight in 0.01..5.0) { "El peso de un ave debe estar entre 0.01 y 5 kg." }
                    }
                    "CONEJO", "HAMSTER" -> {
                        require(age in 0..12) { "La edad debe estar entre 0 y 12 años." }
                        require(weight in 0.01..8.0) { "El peso debe estar entre 0.01 y 8 kg." }
                    }
                    else -> {
                        require(age in 0..30) { "La edad de un perro debe estar entre 0 y 30 años." }
                        require(weight in 0.2..120.0) { "El peso de un perro debe estar entre 0.2 y 120 kg." }
                    }
                }

                val microchip = "CHIP-MX" + (10000000..99999999).random()
                val newPet = Pet(
                    ownerId = userId,
                    id = System.currentTimeMillis(),
                    name = cleanName,
                    species = cleanSpecies,
                    breed = cleanBreed,
                    age = age,
                    weight = weight,
                    gender = gender,
                    microchip = microchip,
                    isVerified = true
                )
                repository.savePet(newPet)
                _toastMessage.value = "¡$cleanName (${newPet.getIconEmoji()}) registrado con folio $microchip en la base de datos! 🐾"
            } catch (e: Exception) {
                _errorMessage.value = "Error al registrar la mascota: ${e.message}"
            }
        }
    }

    fun updatePet(pet: Pet, name: String, species: String, breed: String, age: Int, weight: Double, gender: String = "Macho") {
        viewModelScope.launch {
            try {
                val cleanName = name.trim()
                val cleanBreed = breed.trim()
                val cleanSpecies = species.uppercase().trim().ifBlank { "PERRO" }

                require(cleanName.length >= 2) { "El nombre de la mascota debe tener al menos 2 letras." }
                require(cleanBreed.isNotEmpty()) { "La raza no puede estar vacía." }

                when (cleanSpecies) {
                    "GATO" -> {
                        require(age in 0..25) { "La edad de un gato debe estar entre 0 y 25 años." }
                        require(weight in 0.2..15.0) { "El peso de un gato debe estar entre 0.2 y 15 kg." }
                    }
                    "AVE", "PAJARO" -> {
                        require(age in 0..50) { "La edad debe estar entre 0 y 50 años." }
                        require(weight in 0.01..5.0) { "El peso de un ave debe estar entre 0.01 y 5 kg." }
                    }
                    "CONEJO", "HAMSTER" -> {
                        require(age in 0..12) { "La edad debe estar entre 0 y 12 años." }
                        require(weight in 0.01..8.0) { "El peso debe estar entre 0.01 y 8 kg." }
                    }
                    else -> {
                        require(age in 0..30) { "La edad de un perro debe estar entre 0 y 30 años." }
                        require(weight in 0.2..120.0) { "El peso de un perro debe estar entre 0.2 y 120 kg." }
                    }
                }

                val microchip = pet.microchip.ifBlank { "CHIP-MX" + (10000000..99999999).random() }
                val updatedPet = pet.copy(
                    name = cleanName,
                    species = cleanSpecies,
                    breed = cleanBreed,
                    age = age,
                    weight = weight,
                    gender = gender,
                    microchip = microchip,
                    isVerified = true
                )
                repository.updatePet(updatedPet)
                if (_selectedPet.value?.id == pet.id) {
                    _selectedPet.value = updatedPet
                }
                _toastMessage.value = "¡Datos de $cleanName (${updatedPet.getIconEmoji()}) actualizados en la base de datos! 🐾"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar datos de la mascota: ${e.message}"
            }
        }
    }

    fun addAppointment(appointment: Appointment) {
        val userId = authManager.getUserId() ?: return
        viewModelScope.launch {
            try {
                require(appointment.petId != 0L) { "Debes seleccionar una mascota." }
                require(appointment.date.isNotBlank() && appointment.hour.isNotBlank()) { "Fecha y hora son obligatorias." }
                require(appointment.branch.isNotBlank()) { "Debes seleccionar una sucursal." }

                val petName = _pets.value.find { it.id == appointment.petId }?.name ?: "tu mascota"
                val updatedAppt = appointment.copy(userId = userId, petName = petName)
                repository.saveAppointment(updatedAppt)

                val newNotif = Notification(
                    userId = userId, title = "Cita Agendada: $petName",
                    message = "Tu cita (${updatedAppt.title}) para el día ${updatedAppt.date} a las ${updatedAppt.hour} ha sido confirmada.",
                    type = "APPOINTMENT",
                    date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es", "ES")).format(java.util.Calendar.getInstance().time),
                    hour = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                )
                repository.addNotification(newNotif)
                _toastMessage.value = "¡Cita agendada y guardada en la base de datos! 📅"
            } catch (e: Exception) {
                _errorMessage.value = "Error al agendar la cita: ${e.message}"
            }
        }
    }

    fun updateBathProgress(appointment: Appointment, newProgress: Int) {
        viewModelScope.launch {
            try {
                val status = if (newProgress >= 100) "LISTO" else "EN PROCESO"
                repository.updateAppointmentProgress(appointment.id, newProgress, status)

                if (newProgress >= 100) {
                    val notif = Notification(
                        userId = appointment.userId, title = "¡${appointment.petName} está list@! 🐾",
                        message = "El servicio de estética ha terminado. Ya puedes pasar a recogerl@ en sucursal.",
                        type = "BATH",
                        date = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale("es", "ES")).format(java.util.Calendar.getInstance().time),
                        hour = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Calendar.getInstance().time)
                    )
                    repository.addNotification(notif)
                }
                _toastMessage.value = "¡Progreso actualizado en la base de datos ($newProgress%)! 🛁"
            } catch (e: Exception) {
                _errorMessage.value = "Error al actualizar progreso: ${e.message}"
            }
        }
    }

    fun cancelAppointment(appointment: Appointment) {
        viewModelScope.launch {
            try {
                repository.deleteAppointment(appointment.id)
                _toastMessage.value = "¡Cita cancelada y eliminada de la base de datos! 🗑️"
            } catch (e: Exception) {
                _errorMessage.value = "Error al cancelar la cita: ${e.message}"
            }
        }
    }

    fun removePet(pet: Pet) {
        viewModelScope.launch {
            try {
                repository.deletePet(pet.id)
                if (_selectedPet.value?.id == pet.id) _selectedPet.value = _pets.value.firstOrNull { it.id != pet.id }
                _toastMessage.value = "¡Perfil de mascota eliminado de la base de datos! 🗑️"
            } catch (e: Exception) {
                _errorMessage.value = "Error al eliminar el perfil de la mascota: ${e.message}"
            }
        }
    }

    fun completeProcedure(procedure: Procedure) {
        viewModelScope.launch {
            try {
                repository.markProcedureAsCompleted(procedure.petId, procedure.name)
                _toastMessage.value = "¡Procedimiento marcado como completado en la base de datos! ✅"
            } catch (e: Exception) {
                _errorMessage.value = "No se pudo actualizar la cartilla: ${e.message}"
            }
        }
    }

    fun addProcedure(petId: Long, name: String, doctor: String, date: String) {
        viewModelScope.launch {
            try {
                val cleanName = name.trim()
                require(cleanName.isNotBlank()) { "El nombre del procedimiento es obligatorio" }
                val proc = Procedure(petId = petId, name = cleanName, doctor = doctor.trim(), date = date.trim(), isCompleted = false)
                repository.addProcedure(proc)
                _toastMessage.value = "¡Procedimiento guardado en la base de datos! 📋"
            } catch (e: Exception) {
                _errorMessage.value = "Error al agregar procedimiento: ${e.message}"
            }
        }
    }

    fun markAllNotificationsAsRead() {
        val u = _user.value ?: return
        viewModelScope.launch {
            try {
                repository.markAllNotificationsAsRead(u.id)
                _toastMessage.value = "¡Todas las notificaciones marcadas como leídas en la base de datos! 🔔"
            } catch (e: Exception) {
                _errorMessage.value = "Error al marcar notificaciones como leídas: ${e.message}"
            }
        }
    }

    fun seedDemoData() {
        val u = _user.value ?: return
        viewModelScope.launch {
            try {
                repository.seedDemoData(u.id)
                _toastMessage.value = "¡Datos de prueba cargados exitosamente en la base de datos! 🚀"
            } catch (e: Exception) {
                _errorMessage.value = "Error al cargar datos de prueba: ${e.message}"
            }
        }
    }
}
