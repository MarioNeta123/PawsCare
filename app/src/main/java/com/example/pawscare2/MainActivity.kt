package com.example.pawscare2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.core.content.ContextCompat
import com.example.pawscare2.model.Appointment
import com.example.pawscare2.model.Pet
import com.example.pawscare2.ui.components.PawsBottomNavigation
import com.example.pawscare2.ui.components.PawsHeader
import com.example.pawscare2.ui.screens.*
import com.example.pawscare2.ui.theme.PawsCare2Theme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class PawsTheme(val primary: Int, val background: Int, val accent: Int, val accentBg: Int) {
    OCEAN(R.color.dark_blue, R.color.white, R.color.pink, R.color.light_pink),
    FOREST(R.color.dark_green, R.color.white, R.color.dark_yellow, R.color.light_yellow),
    SUNSET(R.color.cherry_red, R.color.white, R.color.dark_blue, R.color.light_blue)
}

// Actividad principal de la aplicación PawsCare
class MainActivity : ComponentActivity() {

    private val viewModel: PawsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PawsCare2Theme {
                val pets by viewModel.pets.collectAsState()
                val user by viewModel.user.collectAsState()
                val appointments by viewModel.appointments.collectAsState()
                val selectedPet by viewModel.selectedPet.collectAsState()
                val procedures by viewModel.procedures.collectAsState()
                val notifications by viewModel.notifications.collectAsState()
                val unreadCount by viewModel.unreadCount.collectAsState()

                val errorMessage by viewModel.errorMessage.collectAsState()
                val toastMessage by viewModel.toastMessage.collectAsState()
                val context = LocalContext.current

                LaunchedEffect(errorMessage) {
                    errorMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                    }
                }

                LaunchedEffect(toastMessage) {
                    toastMessage?.let { msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                var currentScreen by remember { mutableIntStateOf(R.layout.screen_home) }
                var currentTheme by remember { mutableStateOf(PawsTheme.OCEAN) }

                var showEditProfileDialog by remember { mutableStateOf(false) }
                var showAddPetDialog by remember { mutableStateOf(false) }
                var petToEdit by remember { mutableStateOf<Pet?>(null) }
                var petToDeleteStep1 by remember { mutableStateOf<Pet?>(null) }
                var petToDeleteStep2 by remember { mutableStateOf<Pet?>(null) }
                var showScheduleDialog by remember { mutableStateOf(false) }
                var schedulingType by remember { mutableStateOf("MEDICAL") }

                // Al cerrar sesión, reinicia la navegación a la pantalla de inicio limpia
                LaunchedEffect(user) {
                    if (user == null) {
                        currentScreen = R.layout.screen_home
                    }
                }

                val imagePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri -> uri?.let { viewModel.updateProfilePhoto(it) } }

                // Diálogo para Editar Perfil
                if (showEditProfileDialog && user != null) {
                    var newName by remember { mutableStateOf(user!!.name) }
                    var newPhone by remember { mutableStateOf(user!!.phone) }
                    var newAddress by remember { mutableStateOf(user!!.address) }

                    AlertDialog(
                        onDismissRequest = { showEditProfileDialog = false },
                        title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = newAddress, onValueChange = { newAddress = it }, label = { Text("Dirección") }, modifier = Modifier.fillMaxWidth())
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.updateProfile(newName, newPhone, newAddress)
                                    showEditProfileDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(ContextCompat.getColor(context, currentTheme.primary)))
                            ) { Text("Guardar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditProfileDialog = false }) { Text("Cancelar") }
                        }
                    )
                }

                // Diálogo para Registrar Mascota
                if (showAddPetDialog) {
                    var pName by remember { mutableStateOf("") }
                    var pSpecies by remember { mutableStateOf("PERRO") }
                    var pBreed by remember { mutableStateOf("") }
                    var pAge by remember { mutableStateOf("") }
                    var pWeight by remember { mutableStateOf("") }

                    AlertDialog(
                        onDismissRequest = { showAddPetDialog = false },
                        title = { Text("Registrar Nueva Mascota", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Especie / Animal:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("PERRO" to "🐶", "GATO" to "🐱", "AVE" to "🦜", "CONEJO" to "🐰", "HAMSTER" to "🐹").forEach { (code, emoji) ->
                                        FilterChip(
                                            selected = (pSpecies == code),
                                            onClick = { pSpecies = code },
                                            label = { Text(emoji, fontSize = 14.sp) }
                                        )
                                    }
                                }
                                OutlinedTextField(value = pName, onValueChange = { pName = it }, label = { Text("Nombre de la mascota") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = pBreed, onValueChange = { pBreed = it }, label = { Text("Raza / Variante") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = pAge, onValueChange = { pAge = it }, label = { Text("Edad (años)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = pWeight, onValueChange = { pWeight = it }, label = { Text("Peso (kg)") }, modifier = Modifier.fillMaxWidth())
                            }
                        },
                        confirmButton = {
                            Button(
                                enabled = pName.isNotBlank() && pBreed.isNotBlank(),
                                onClick = {
                                    viewModel.addPet(
                                        name = pName,
                                        species = pSpecies,
                                        breed = pBreed,
                                        age = pAge.toIntOrNull() ?: 1,
                                        weight = pWeight.toDoubleOrNull() ?: 1.0
                                    )
                                    showAddPetDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(ContextCompat.getColor(context, currentTheme.primary)))
                            ) { Text("Registrar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddPetDialog = false }) { Text("Cancelar") }
                        }
                    )
                }

                // Diálogo para Editar Mascota Existente
                petToEdit?.let { pet ->
                    var editName by remember { mutableStateOf(pet.name) }
                    var editSpecies by remember { mutableStateOf(pet.species) }
                    var editBreed by remember { mutableStateOf(pet.breed) }
                    var editAge by remember { mutableStateOf(pet.age.toString()) }
                    var editWeight by remember { mutableStateOf(pet.weight.toString()) }

                    AlertDialog(
                        onDismissRequest = { petToEdit = null },
                        title = { Text("Editar Datos de ${pet.name}", fontWeight = FontWeight.Bold) },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Especie / Animal:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    listOf("PERRO" to "🐶", "GATO" to "🐱", "AVE" to "🦜", "CONEJO" to "🐰", "HAMSTER" to "🐹").forEach { (code, emoji) ->
                                        FilterChip(
                                            selected = (editSpecies == code),
                                            onClick = { editSpecies = code },
                                            label = { Text(emoji, fontSize = 14.sp) }
                                        )
                                    }
                                }
                                OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editBreed, onValueChange = { editBreed = it }, label = { Text("Raza") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editAge, onValueChange = { editAge = it }, label = { Text("Edad (años)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = editWeight, onValueChange = { editWeight = it }, label = { Text("Peso (kg)") }, modifier = Modifier.fillMaxWidth())
                            }
                        },
                        confirmButton = {
                            Button(
                                enabled = editName.isNotBlank() && editBreed.isNotBlank(),
                                onClick = {
                                    viewModel.updatePet(
                                        pet = pet,
                                        name = editName,
                                        species = editSpecies,
                                        breed = editBreed,
                                        age = editAge.toIntOrNull() ?: pet.age,
                                        weight = editWeight.toDoubleOrNull() ?: pet.weight
                                    )
                                    petToEdit = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(ContextCompat.getColor(context, currentTheme.primary)))
                            ) { Text("Guardar Cambios") }
                        },
                        dismissButton = {
                            TextButton(onClick = { petToEdit = null }) { Text("Cancelar") }
                        }
                    )
                }

                // Paso 1 de Eliminación: Primera Confirmación
                petToDeleteStep1?.let { pet ->
                    AlertDialog(
                        onDismissRequest = { petToDeleteStep1 = null },
                        title = { Text("Eliminar Perfil de ${pet.name}", fontWeight = FontWeight.Bold) },
                        text = { Text("¿Deseas iniciar el proceso para eliminar el perfil de ${pet.name} (${pet.getIconEmoji()}) de tu cuenta?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    petToDeleteStep2 = pet
                                    petToDeleteStep1 = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor.Red)
                            ) { Text("Continuar ➔", color = ComposeColor.White) }
                        },
                        dismissButton = {
                            TextButton(onClick = { petToDeleteStep1 = null }) { Text("Cancelar") }
                        }
                    )
                }

                // Paso 2 de Eliminación: Segunda Advertencia Explícita sobre lo que se pierde
                petToDeleteStep2?.let { pet ->
                    AlertDialog(
                        onDismissRequest = { petToDeleteStep2 = null },
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 22.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("ADVERTENCIA DEFINITIVA", fontWeight = FontWeight.Bold, color = ComposeColor.Red)
                            }
                        },
                        text = {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Esta acción es IRREVERSIBLE. Al eliminar a ${pet.name} (${pet.getIconEmoji()}) de la aplicación perderás permanentemente:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = ComposeColor.DarkGray
                                )
                                Text("• Su cartilla médica completa y registro de vacunas completadas/pendientes.", fontSize = 13.sp, color = ComposeColor.DarkGray)
                                Text("• El historial de citas médicas y estéticas agendadas.", fontSize = 13.sp, color = ComposeColor.DarkGray)
                                Text("• Todas las alertas y notificaciones asociadas.", fontSize = 13.sp, color = ComposeColor.DarkGray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("¿Confirmas la eliminación permanente?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = ComposeColor.Red)
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    viewModel.removePet(pet)
                                    petToDeleteStep2 = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor.Red)
                            ) { Text("Sí, Eliminar Definitivamente", color = ComposeColor.White, fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { petToDeleteStep2 = null }) { Text("No, Conservar Mascota") }
                        }
                    )
                }

                // Diálogo para Agendar Cita (Médica o Estética)
                if (showScheduleDialog) {
                    val calendar = Calendar.getInstance()
                    var selPetId by remember { mutableLongStateOf(selectedPet?.id ?: pets.firstOrNull()?.id ?: 0L) }
                    var selBranch by remember { mutableStateOf("") }
                    var selDate by remember { mutableStateOf("") }
                    var selHour by remember { mutableStateOf("") }
                    var selDoctor by remember { mutableStateOf("") }
                    var selService by remember { mutableStateOf("") }
                    var selNotes by remember { mutableStateOf("") }

                    val branches = listOf("Sucursal Norte 📍", "Sucursal Sur 📍", "Sucursal Centro 📍")
                    val doctors = listOf("Dr. García (Veterinario)", "Dra. Martínez (Especialista)")
                    val services = listOf("Baño Completo 🧼", "Corte de Pelo ✂️", "Spa & Masaje 💆")

                    val datePickerDialog = DatePickerDialog(context, { _, year, month, day ->
                        val cal = Calendar.getInstance().apply { set(year, month, day) }
                        selDate = SimpleDateFormat("dd MMM, yyyy", Locale.forLanguageTag("es")).format(cal.time)
                    }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply { datePicker.minDate = System.currentTimeMillis() }

                    val timePickerDialog = TimePickerDialog(context, { _, hour, minute ->
                        if (hour !in 6..22) {
                            Toast.makeText(context, "El horario de atención es de 6:00 AM a 10:00 PM.", Toast.LENGTH_SHORT).show()
                            selHour = ""
                        } else {
                            val amPm = if (hour < 12) "AM" else "PM"
                            val displayHour = if (hour % 12 == 0) 12 else hour % 12
                            val fmtMin = if (minute < 10) "0$minute" else minute.toString()
                            selHour = "$displayHour:$fmtMin $amPm"
                        }
                    }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)

                    val isFormValid by remember { derivedStateOf { selPetId != 0L && selBranch.isNotBlank() && selDate.isNotBlank() && selHour.isNotBlank() } }

                    AlertDialog(
                        onDismissRequest = { showScheduleDialog = false },
                        title = { Text(if (schedulingType == "MEDICAL") "🩺 Agendar Consulta Médica" else "🛁 Agendar Servicio de Estética", fontWeight = FontWeight.Bold) },
                        text = {
                            LazyColumn(modifier = Modifier.heightIn(max = 450.dp)) {
                                item {
                                    Text("1. Selecciona la mascota:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    pets.forEach { p ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().clickable { selPetId = p.id }.padding(vertical = 4.dp)
                                        ) {
                                            RadioButton(selected = (p.id == selPetId), onClick = { selPetId = p.id })
                                            Text("${p.getIconEmoji()} ${p.name}", modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                    Text("2. Selecciona la sucursal:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    branches.forEach { b ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.fillMaxWidth().clickable { selBranch = b }.padding(vertical = 4.dp)
                                        ) {
                                            RadioButton(selected = (b == selBranch), onClick = { selBranch = b })
                                            Text(b, modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                    Text("3. Fecha y Hora de atención:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }.padding(top = 8.dp)) {
                                        OutlinedTextField(
                                            value = selDate,
                                            onValueChange = {},
                                            label = { Text("Fecha de la cita") },
                                            modifier = Modifier.fillMaxWidth(),
                                            readOnly = true,
                                            enabled = false,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledBorderColor = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                    }
                                    Box(modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() }.padding(top = 8.dp)) {
                                        OutlinedTextField(
                                            value = selHour,
                                            onValueChange = {},
                                            label = { Text("Hora de la cita") },
                                            modifier = Modifier.fillMaxWidth(),
                                            readOnly = true,
                                            enabled = false,
                                            colors = OutlinedTextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledBorderColor = MaterialTheme.colorScheme.outline
                                            )
                                        )
                                    }
                                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                    if (schedulingType == "MEDICAL") {
                                        Text("4. Veterinario responsable:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        doctors.forEach { d ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth().clickable { selDoctor = d }.padding(vertical = 4.dp)
                                            ) {
                                                RadioButton(selected = (d == selDoctor), onClick = { selDoctor = d })
                                                Text(d, modifier = Modifier.padding(start = 8.dp))
                                            }
                                        }
                                    } else {
                                        Text("4. Servicio deseado:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        services.forEach { s ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.fillMaxWidth().clickable { selService = s }.padding(vertical = 4.dp)
                                            ) {
                                                RadioButton(selected = (s == selService), onClick = { selService = s })
                                                Text(s, modifier = Modifier.padding(start = 8.dp))
                                            }
                                        }
                                    }
                                    OutlinedTextField(
                                        value = selNotes,
                                        onValueChange = { selNotes = it },
                                        label = { Text(if (schedulingType == "MEDICAL") "Motivo de la consulta" else "Requerimientos especiales") },
                                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                                    )
                                }
                            }
                        },
                        confirmButton = {
                            Button(
                                enabled = isFormValid,
                                onClick = {
                                    val newAppt = Appointment(
                                        petId = selPetId,
                                        title = if (schedulingType == "MEDICAL") "Consulta Médica" else selService,
                                        date = selDate,
                                        hour = selHour,
                                        branch = selBranch,
                                        notes = selNotes,
                                        type = schedulingType,
                                        doctor = if (schedulingType == "MEDICAL") selDoctor else "Estilista Canino"
                                    )
                                    viewModel.addAppointment(newAppt)
                                    showScheduleDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ComposeColor(ContextCompat.getColor(this@MainActivity, currentTheme.primary))),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) { Text("CONFIRMAR CITA", fontWeight = FontWeight.Bold) }
                        },
                        dismissButton = { TextButton(onClick = { showScheduleDialog = false }) { Text("Cancelar") } }
                    )
                }

                val isLoggedIn = user != null

                if (!isLoggedIn && currentScreen != R.layout.screen_styles) {
                    BackHandler(enabled = true) {
                        // Evita salir de la aplicación al presionar el botón Atrás cuando está en la pantalla de Login
                    }
                    LoginScreen(
                        viewModel = viewModel,
                        currentTheme = currentTheme,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Scaffold(
                        topBar = {
                            PawsHeader(
                                title = when (currentScreen) {
                                    R.layout.screen_home -> "PawsCare"
                                    R.layout.screen_appointments -> "Citas"
                                    R.layout.screen_pet_card -> "Cartilla Médica"
                                    R.layout.screen_bath -> "Estética y Spa"
                                    R.layout.screen_profile -> "Mi Perfil"
                                    R.layout.screen_notifications -> "Notificaciones"
                                    R.layout.screen_styles -> "Estilos"
                                    else -> "PawsCare"
                                },
                                unreadCount = unreadCount,
                                currentTheme = currentTheme,
                                onNotificationClick = { currentScreen = R.layout.screen_notifications }
                            )
                        },
                        bottomBar = {
                            PawsBottomNavigation(
                                currentScreen = currentScreen,
                                currentTheme = currentTheme,
                                onTabSelected = { currentScreen = it }
                            )
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentScreen) {
                                R.layout.screen_home -> HomeScreen(
                                    user = user,
                                    pets = pets,
                                    selectedPet = selectedPet,
                                    notifications = notifications,
                                    viewModel = viewModel,
                                    currentTheme = currentTheme,
                                    onNavigateToAppointments = { currentScreen = R.layout.screen_appointments },
                                    onNavigateToBath = { currentScreen = R.layout.screen_bath },
                                    onNavigateToPetCard = { currentScreen = R.layout.screen_pet_card },
                                    onAddPetClick = { showAddPetDialog = true },
                                    onEditPetClick = { petToEdit = it },
                                    onNotificationClick = { currentScreen = R.layout.screen_notifications }
                                )

                                R.layout.screen_appointments -> AppointmentsScreen(
                                    pets = pets,
                                    appointments = appointments,
                                    selectedPet = selectedPet,
                                    user = user,
                                    viewModel = viewModel,
                                    currentTheme = currentTheme,
                                    onNavigateToBath = { currentScreen = R.layout.screen_bath },
                                    onScheduleClick = { type ->
                                        schedulingType = type
                                        showScheduleDialog = true
                                    },
                                    onAddPetClick = { showAddPetDialog = true }
                                )

                                R.layout.screen_pet_card -> PetCardScreen(
                                    pets = pets,
                                    selectedPet = selectedPet,
                                    procedures = procedures,
                                    user = user,
                                    viewModel = viewModel,
                                    currentTheme = currentTheme,
                                    onAddPetClick = { showAddPetDialog = true },
                                    onEditPetClick = { petToEdit = it },
                                    onDeletePetClick = { petToDeleteStep1 = it }
                                )

                                R.layout.screen_bath -> BathScreen(
                                    pets = pets,
                                    appointments = appointments,
                                    user = user,
                                    currentTheme = currentTheme,
                                    viewModel = viewModel,
                                    onNavigateToAppointments = { currentScreen = R.layout.screen_appointments },
                                    onScheduleClick = { type ->
                                        schedulingType = type
                                        showScheduleDialog = true
                                    },
                                    onAddPetClick = { showAddPetDialog = true }
                                )

                                R.layout.screen_profile -> ProfileScreen(
                                    user = user,
                                    pets = pets,
                                    viewModel = viewModel,
                                    currentTheme = currentTheme,
                                    onEditProfileClick = { showEditProfileDialog = true },
                                    onChangePhotoClick = { imagePicker.launch("image/*") },
                                    onEditPetClick = { petToEdit = it },
                                    onDeletePetClick = { petToDeleteStep1 = it }
                                )

                                R.layout.screen_notifications -> NotificationsScreen(
                                    notifications = notifications,
                                    user = user,
                                    viewModel = viewModel,
                                    currentTheme = currentTheme,
                                    onBackClick = { currentScreen = R.layout.screen_home }
                                )

                                R.layout.screen_styles -> StylesScreen(
                                    currentTheme = currentTheme,
                                    onApplyTheme = { currentTheme = it }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
