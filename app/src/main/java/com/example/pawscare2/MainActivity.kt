package com.example.pawscare2

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.load
import coil.transform.CircleCropTransformation
import com.example.pawscare2.model.*
import com.example.pawscare2.ui.theme.PawsCare2Theme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.ui.graphics.Color as ComposeColor

/**
 * Actividad Principal de PawsCare2.
 * Gestiona navegación, temas dinámicos y el sistema de citas refinado.
 * Implementa restricciones de acceso para usuarios sin mascotas registradas.
 */
enum class PawsTheme(
    val primary: Int, 
    val background: Int, 
    val accent: Int, 
    val accentBg: Int
) {
    OCEAN(R.color.dark_blue, R.color.white, R.color.pink, R.color.light_pink),
    FOREST(R.color.dark_green, R.color.white, R.color.dark_yellow, R.color.light_yellow),
    SUNSET(R.color.cherry_red, R.color.white, R.color.dark_blue, R.color.light_blue)
}

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

                var currentLayout by remember { mutableIntStateOf(R.layout.screen_home) }
                var currentTheme by remember { mutableStateOf(PawsTheme.OCEAN) }
                
                val unreadCount = notifications.count { !it.isRead }
                var lastUnreadCount by remember { mutableStateOf<Int?>(null) }

                var showEditDialog by remember { mutableStateOf(false) }
                var showAddPetDialog by remember { mutableStateOf(false) }
                var showScheduleDialog by remember { mutableStateOf(false) }
                var schedulingType by remember { mutableStateOf("MEDICAL") }

                val imagePicker = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri ->
                    uri?.let { viewModel.updateProfilePhoto(it) }
                }

                if (showEditDialog && user != null) {
                    var newName by remember { mutableStateOf(user!!.name) }
                    var newPhone by remember { mutableStateOf(user!!.phone) }
                    var newAddress by remember { mutableStateOf(user!!.address) }

                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showEditDialog = false },
                        title = { Text("Editar Perfil") },
                        text = {
                            Column {
                                TextField(value = newName, onValueChange = { newName = it }, label = { Text("Nombre") })
                                TextField(value = newPhone, onValueChange = { newPhone = it }, label = { Text("Teléfono") })
                                TextField(value = newAddress, onValueChange = { newAddress = it }, label = { Text("Dirección") })
                            }
                        },
                        confirmButton = {
                            androidx.compose.material3.Button(onClick = {
                                viewModel.updateProfile(newName, newPhone, newAddress)
                                showEditDialog = false
                            }) { Text("Guardar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
                        }
                    )
                }

                if (showAddPetDialog) {
                    var pName by remember { mutableStateOf("") }
                    var pBreed by remember { mutableStateOf("") }
                    var pAge by remember { mutableStateOf("") }
                    var pWeight by remember { mutableStateOf("") }

                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showAddPetDialog = false },
                        title = { Text("Registrar Mascota") },
                        text = {
                            Column {
                                TextField(value = pName, onValueChange = { pName = it }, label = { Text("Nombre") })
                                TextField(value = pBreed, onValueChange = { pBreed = it }, label = { Text("Raza") })
                                TextField(value = pAge, onValueChange = { pAge = it }, label = { Text("Edad (años)") })
                                TextField(value = pWeight, onValueChange = { pWeight = it }, label = { Text("Peso (kg)") })
                            }
                        },
                        confirmButton = {
                            androidx.compose.material3.Button(enabled = pName.isNotBlank(), onClick = {
                                viewModel.addPet(pName, pBreed, pAge.toIntOrNull() ?: 1, pWeight.toDoubleOrNull() ?: 1.0)
                                showAddPetDialog = false
                            }) { Text("Registrar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddPetDialog = false }) { Text("Cancelar") }
                        }
                    )
                }

                if (showScheduleDialog) {
                    val context = LocalContext.current
                    val calendar = Calendar.getInstance()
                    
                    var selPetId by remember { mutableStateOf(selectedPet?.id ?: pets.firstOrNull()?.id ?: 0L) }
                    var selBranch by remember { mutableStateOf("") }
                    var selDate by remember { mutableStateOf("") }
                    var selHour by remember { mutableStateOf("") }
                    var selDoctor by remember { mutableStateOf("") }
                    var selService by remember { mutableStateOf("") }
                    var selNotes by remember { mutableStateOf("") }

                    val branches = listOf("Norte", "Sur", "Centro")
                    val doctors = listOf("Dr. García", "Dra. Martínez")
                    val services = listOf("Baño", "Corte", "Spa")

                    val datePickerDialog = DatePickerDialog(
                        context,
                        { _, year, month, day ->
                            val cal = Calendar.getInstance().apply { set(year, month, day) }
                            val sdf = SimpleDateFormat("dd MMM, yyyy", Locale("es", "ES"))
                            selDate = sdf.format(cal.time)
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).apply {
                        datePicker.minDate = System.currentTimeMillis()
                    }

                    val timePickerDialog = TimePickerDialog(
                        context,
                        { _, hour, minute ->
                            if (hour < 6 || hour > 22) {
                                Toast.makeText(context, "El horario de atención es de 6:00 AM a 10:00 PM.", Toast.LENGTH_SHORT).show()
                                selHour = "" 
                            } else {
                                val amPm = if (hour < 12) "AM" else "PM"
                                val displayHour = if (hour % 12 == 0) 12 else hour % 12
                                val fmtMin = if (minute < 10) "0$minute" else minute.toString()
                                selHour = "$displayHour:$fmtMin $amPm"
                            }
                        },
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE),
                        false
                    )

                    val isFormValid by remember {
                        derivedStateOf {
                            selPetId != 0L && selBranch.isNotBlank() && selDate.isNotBlank() && selHour.isNotBlank()
                        }
                    }

                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showScheduleDialog = false },
                        title = { Text(if (schedulingType == "MEDICAL") "🩺 Agendar Cita Médica" else "🛁 Agendar Estética", style = MaterialTheme.typography.headlineSmall) },
                        text = {
                            LazyColumn(modifier = Modifier.heightIn(max = 450.dp)) {
                                item {
                                    Text("1. Selecciona tu mascota:", style = MaterialTheme.typography.titleSmall)
                                    pets.forEach { p ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selPetId = p.id }.padding(vertical = 4.dp)) {
                                            RadioButton(selected = (p.id == selPetId), onClick = { selPetId = p.id })
                                            Text(p.name, modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                                    Text("2. ¿En qué sucursal?", style = MaterialTheme.typography.titleSmall)
                                    branches.forEach { b ->
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selBranch = b }.padding(vertical = 4.dp)) {
                                            RadioButton(selected = (b == selBranch), onClick = { selBranch = b })
                                            Text(b, modifier = Modifier.padding(start = 8.dp))
                                        }
                                    }
                                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                                    Text("3. Fecha y Hora:", style = MaterialTheme.typography.titleSmall)
                                    
                                    Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }.padding(top = 8.dp)) {
                                        TextField(
                                            value = selDate,
                                            onValueChange = {},
                                            label = { Text("Fecha de la cita") },
                                            modifier = Modifier.fillMaxWidth(),
                                            readOnly = true,
                                            enabled = false,
                                            colors = TextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = ComposeColor.Transparent,
                                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }

                                    Box(modifier = Modifier.fillMaxWidth().clickable { timePickerDialog.show() }.padding(top = 8.dp)) {
                                        TextField(
                                            value = selHour,
                                            onValueChange = {},
                                            label = { Text("Hora de la cita") },
                                            modifier = Modifier.fillMaxWidth(),
                                            readOnly = true,
                                            enabled = false,
                                            colors = TextFieldDefaults.colors(
                                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                                disabledContainerColor = ComposeColor.Transparent,
                                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                    
                                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                                    if (schedulingType == "MEDICAL") {
                                        Text("4. Veterinario disponible:", style = MaterialTheme.typography.titleSmall)
                                        doctors.forEach { d ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selDoctor = d }.padding(vertical = 4.dp)) {
                                                RadioButton(selected = (d == selDoctor), onClick = { selDoctor = d })
                                                Text(d, modifier = Modifier.padding(start = 8.dp))
                                            }
                                        }
                                    } else {
                                        Text("4. Servicio deseado:", style = MaterialTheme.typography.titleSmall)
                                        services.forEach { s ->
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selService = s }.padding(vertical = 4.dp)) {
                                                RadioButton(selected = (s == selService), onClick = { selService = s })
                                                Text(s, modifier = Modifier.padding(start = 8.dp))
                                            }
                                        }
                                    }
                                    TextField(value = selNotes, onValueChange = { selNotes = it }, label = { Text(if (schedulingType == "MEDICAL") "Motivo de la consulta" else "Requerimientos") }, modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
                                }
                            }
                        },
                        confirmButton = {
                            androidx.compose.material3.Button(
                                enabled = isFormValid, 
                                onClick = {
                                    val finalTitle = if (schedulingType == "MEDICAL") "Consulta Médica" else selService
                                    val newAppt = Appointment(
                                        userId = "", 
                                        petId = selPetId,
                                        title = finalTitle,
                                        date = selDate,
                                        hour = selHour,
                                        doctor = if (schedulingType == "MEDICAL") selDoctor else "Estilista",
                                        branch = selBranch,
                                        notes = selNotes,
                                        type = schedulingType,
                                        isPast = false
                                    )
                                    viewModel.addAppointment(newAppt)
                                    showScheduleDialog = false
                                    Toast.makeText(this@MainActivity, "¡Cita agendada correctamente!", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ComposeColor(ContextCompat.getColor(this@MainActivity, currentTheme.primary)),
                                    contentColor = ComposeColor.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                            ) { 
                                Text("CONFIRMAR CITA", fontWeight = FontWeight.Bold) 
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showScheduleDialog = false }) { Text("Cancelar") }
                        }
                    )
                }

                val isLog = user != null
                val layoutToUse = if (!isLog && currentLayout != R.layout.screen_styles) R.layout.screen_login else currentLayout

                key(layoutToUse, currentTheme) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            val view = LayoutInflater.from(context).inflate(layoutToUse, null)
                            view.setBackgroundColor(Color.WHITE)
                            applyThemeToView(view, currentTheme)
                            
                            if (layoutToUse != R.layout.screen_login) {
                                setupGlobalInteractions(view, layoutToUse, currentTheme) { currentLayout = it }
                            }
                            
                            when (layoutToUse) {
                                R.layout.screen_login -> setupLoginScreen(view, viewModel)
                                R.layout.screen_home -> setupHomeScreen(view, user, selectedPet, pets, viewModel, currentTheme, notifications, onAddPet = { showAddPetDialog = true })
                                R.layout.screen_appointments -> {
                                    view.findViewById<View>(R.id.appointment_section_header)?.findViewById<TextView>(R.id.tv_title)?.text = "Citas"
                                    setupAppointmentsScreen(view, pets, appointments, selectedPet, viewModel, currentTheme, 
                                        onSwitch = { currentLayout = R.layout.screen_bath },
                                        onSchedule = { schedulingType = "MEDICAL"; showScheduleDialog = true },
                                        onAddPet = { showAddPetDialog = true }
                                    )
                                }
                                R.layout.screen_pet_card -> {
                                    view.findViewById<View>(R.id.profile_section_header)?.findViewById<TextView>(R.id.tv_title)?.text = "Cartilla"
                                    setupPetCardScreen(view, selectedPet, pets, procedures, viewModel, currentTheme, onAddPet = { showAddPetDialog = true })
                                }
                                R.layout.screen_profile -> {
                                    view.findViewById<View>(R.id.profile_section_header)?.findViewById<TextView>(R.id.tv_title)?.text = "Mi Perfil"
                                    setupProfileScreen(view, user, pets, viewModel, currentTheme, onEdit = { showEditDialog = true }, onChangePhoto = { imagePicker.launch("image/*") })
                                }
                                R.layout.screen_bath -> {
                                    view.findViewById<View>(R.id.bath_section_header)?.findViewById<TextView>(R.id.tv_title)?.text = "Baños"
                                    setupBathScreen(view, pets, currentTheme, 
                                        onBack = { currentLayout = R.layout.screen_appointments }, 
                                        onSchedule = { schedulingType = "GROOMING"; showScheduleDialog = true },
                                        onAddPet = { showAddPetDialog = true }
                                    )
                                }
                                R.layout.screen_notifications -> setupNotificationsScreen(view, notifications, currentTheme) { currentLayout = it }
                                R.layout.screen_styles -> setupStylesScreen(view, currentTheme) { currentTheme = it }
                            }
                            view
                        },
                        update = { view ->
                            val btnN = view.findViewById<FrameLayout>(R.id.btn_notifications)
                            if (btnN != null) {
                                updateNotificationBadge(btnN, unreadCount)
                                if (unreadCount > 0 && (lastUnreadCount == null || unreadCount > (lastUnreadCount ?: 0))) shakeBell(btnN)
                                lastUnreadCount = unreadCount
                            }
                            
                            when (layoutToUse) {
                                R.layout.screen_home -> setupHomeScreen(view, user, selectedPet, pets, viewModel, currentTheme, notifications, onAddPet = { showAddPetDialog = true })
                                R.layout.screen_appointments -> setupAppointmentsScreen(view, pets, appointments, selectedPet, viewModel, currentTheme, 
                                    onSwitch = { currentLayout = R.layout.screen_bath },
                                    onSchedule = { schedulingType = "MEDICAL"; showScheduleDialog = true },
                                    onAddPet = { showAddPetDialog = true }
                                )
                                R.layout.screen_pet_card -> setupPetCardScreen(view, selectedPet, pets, procedures, viewModel, currentTheme, onAddPet = { showAddPetDialog = true })
                                R.layout.screen_bath -> setupBathScreen(view, pets, currentTheme, 
                                    onBack = { currentLayout = R.layout.screen_appointments }, 
                                    onSchedule = { schedulingType = "GROOMING"; showScheduleDialog = true },
                                    onAddPet = { showAddPetDialog = true }
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    private fun applyEmptyState(container: ViewGroup, theme: PawsTheme, onAddPet: () -> Unit) {
        container.removeAllViews()
        val context = container.context
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(60, 100, 60, 100)
            layoutParams = LinearLayout.LayoutParams(-1, -1)
        }

        layout.addView(TextView(context).apply {
            text = "Aún no tienes mascotas registradas. Para acceder a esta sección, primero registra a un compañero peludo."
            textSize = 18f
            setTextColor(Color.GRAY)
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 60)
            setTypeface(null, Typeface.ITALIC)
        })

        val btnContainer = FrameLayout(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.bg_rounded_pink)
            setPadding(40, 20, 40, 20)
            isClickable = true
            isFocusable = true
            layoutParams = LinearLayout.LayoutParams(-2, -2)
        }
        
        val innerLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        
        innerLayout.addView(ImageView(context).apply {
            setImageResource(android.R.drawable.ic_menu_add)
            setColorFilter(ContextCompat.getColor(context, R.color.cherry_red))
            layoutParams = LinearLayout.LayoutParams(60, 60).apply { marginEnd = 20 }
        })
        
        innerLayout.addView(TextView(context).apply {
            text = "Registrar Mascota"
            setTextColor(ContextCompat.getColor(context, R.color.cherry_red))
            setTypeface(null, Typeface.BOLD)
        })
        
        btnContainer.addView(innerLayout)
        btnContainer.setOnClickListener { onAddPet() }
        layout.addView(btnContainer)

        container.addView(layout)
    }

    private fun updateNotificationBadge(btn: FrameLayout, count: Int) {
        btn.background = null
        var badge = btn.findViewWithTag<TextView>("badge")
        if (count > 0) {
            if (badge == null) {
                badge = TextView(this).apply {
                    tag = "badge"; textSize = 9f; setTypeface(null, Typeface.BOLD); setTextColor(Color.WHITE); gravity = Gravity.CENTER
                    val size = (16 * resources.displayMetrics.density).toInt()
                    layoutParams = FrameLayout.LayoutParams(size, size).apply { gravity = Gravity.TOP or Gravity.END }
                    background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.RED) }
                }
                btn.addView(badge)
            }
            badge.text = if (count > 9) "9+" else count.toString(); badge.visibility = View.VISIBLE
        } else { badge?.visibility = View.GONE }
    }

    private fun shakeBell(btn: FrameLayout) {
        val bell = btn.getChildAt(0)
        ObjectAnimator.ofFloat(bell, "rotation", 0f, 20f, -20f, 20f, -20f, 10f, -10f, 0f).apply { duration = 500; start() }
    }

    private fun getPawBitmap(): android.graphics.Bitmap {
        val size = (44 * resources.displayMetrics.density).toInt()
        val bitmap = android.graphics.Bitmap.createBitmap(size, size, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textSize = 24 * resources.displayMetrics.density; textAlign = android.graphics.Paint.Align.CENTER }
        canvas.drawText("🐾", canvas.width / 2f, (canvas.height / 2f) - ((paint.descent() + paint.ascent()) / 2f), paint)
        return bitmap
    }

    private fun showNotificationDetail(n: Notification) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(60, 60, 60, 60)
            background = GradientDrawable().apply { shape = GradientDrawable.RECTANGLE; cornerRadius = 60f; setColor(Color.WHITE) }
        }
        layout.addView(TextView(this).apply { text = n.title; textSize = 22f; setTypeface(null, Typeface.BOLD); setTextColor(Color.BLACK); setPadding(0, 0, 0, 30) })
        layout.addView(TextView(this).apply { text = "De: ${n.sender}"; textSize = 14f; setTextColor(Color.GRAY); setPadding(0, 0, 0, 20) })
        layout.addView(TextView(this).apply { text = "${n.date} - ${n.hour}"; textSize = 13f; setTextColor(Color.DKGRAY); setPadding(0, 0, 0, 40) })
        layout.addView(TextView(this).apply { text = n.message; textSize = 16f; setTextColor(Color.BLACK); setPadding(0, 0, 0, 60) })
        val btn = android.widget.Button(this).apply { text = "Cerrar" }
        layout.addView(btn)
        val dialog = AlertDialog.Builder(this).setView(layout).create().apply { window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(Color.TRANSPARENT)) }
        btn.setOnClickListener { dialog.dismiss() }
        dialog.show(); viewModel.markAsRead(n.id)
    }

    private fun setupGlobalInteractions(view: View, currentL: Int, theme: PawsTheme, onNavigate: (Int) -> Unit) {
        val ids = mapOf(R.id.nav_home to R.layout.screen_home, R.id.nav_citas to R.layout.screen_appointments, R.id.nav_cartilla to R.layout.screen_pet_card, R.id.nav_perfil to R.layout.screen_profile, R.id.nav_estilos to R.layout.screen_styles)
        ids.forEach { (id, layout) ->
            val btn = view.findViewById<View>(id) ?: return@forEach
            val sel = (layout == currentL)
            val color = if (sel) { if (id == R.id.nav_cartilla) ContextCompat.getColor(this, theme.accent) else ContextCompat.getColor(this, theme.primary) } else ContextCompat.getColor(this, R.color.gray)
            updateNavButtonAppearance(btn, color, sel)
            btn.setOnClickListener { onNavigate(layout) }
        }
        view.findViewById<View>(R.id.btn_notifications)?.setOnClickListener { onNavigate(R.layout.screen_notifications) }
    }

    private fun updateNavButtonAppearance(btn: View, color: Int, sel: Boolean) {
        if (btn is ViewGroup) {
            for (i in 0 until btn.childCount) {
                val c = btn.getChildAt(i)
                if (c is ImageView) c.setColorFilter(color)
                if (c is TextView) { c.setTextColor(color); c.alpha = if (sel) 1.0f else 0.6f }
                if (c is ViewGroup) updateNavButtonAppearance(c, color, sel)
            }
        }
    }

    /**
     * Configura la pantalla de inicio con alertas ordenadas y vinculación de datos completa.
     */
    private fun setupHomeScreen(v: View, u: User?, p: Pet?, pets: List<Pet>, vm: PawsViewModel, t: PawsTheme, notifs: List<Notification>, onAddPet: () -> Unit) {
        v.findViewById<TextView>(R.id.tv_greeting)?.text = "¡Hola, ${u?.name ?: "Usuario"}!"
        
        if (pets.isEmpty()) {
            v.findViewById<TextView>(R.id.tv_pet_message)?.text = "¡Bienvenido a PawsCare! Registra a tu primer peludo para comenzar 🐾"
        } else {
            v.findViewById<TextView>(R.id.tv_pet_message)?.text = "${p?.name ?: "Tu mascota"} te extraña 🐾"
        }

        // Asegura que el logo sea un círculo azul claro en el tema OCEAN
        if (t == PawsTheme.OCEAN) {
            v.findViewById<View>(R.id.frame_logo)?.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ContextCompat.getColor(this@MainActivity, R.color.light_blue))
            }
        }
        
        val pc = v.findViewById<ViewGroup>(R.id.layout_pets_placeholder)
        pc?.removeAllViews()
        
        if (pets.isEmpty()) {
            val btn = LayoutInflater.from(this).inflate(R.layout.item_pet, pc, false)
            btn.findViewById<TextView>(R.id.pet_name)?.text = "+ Registrar"
            btn.setOnClickListener { onAddPet() }
            pc?.addView(btn)
        } else {
            pets.forEach { pet ->
                val item = LayoutInflater.from(v.context).inflate(R.layout.item_pet, pc, false)
                applyThemeToView(item, t)
                item.findViewById<TextView>(R.id.pet_name)?.text = pet.name
                if (pet.id == p?.id) item.background.mutate().setTint(ContextCompat.getColor(this, t.accentBg))
                
                item.setOnClickListener { vm.selectPet(pet) }
                item.setOnLongClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Eliminar Mascota")
                        .setMessage("¿Deseas eliminar el perfil de ${pet.name}?")
                        .setPositiveButton("Eliminar") { _, _ -> vm.removePet(pet) }
                        .setNegativeButton("Cancelar", null)
                        .show()
                    true
                }
                pc?.addView(item)
            }
        }
        
        val ac = v.findViewById<ViewGroup>(R.id.layout_alerts_placeholder)
        ac?.removeAllViews()

        val sortedNotifs = notifs.sortedWith(compareByDescending<Notification> { it.date }.thenByDescending { it.hour })
        
        sortedNotifs.take(3).forEach { n ->
            val item = LayoutInflater.from(v.context).inflate(R.layout.item_default_a, ac, false)
            applyThemeToView(item, t)
            
            item.findViewById<TextView>(R.id.tv_message_theme)?.text = n.title
            item.findViewById<TextView>(R.id.tv_time)?.text = n.hour
            item.findViewById<TextView>(R.id.tv_message)?.text = n.message
            
            if (n.type == "WELCOME") item.findViewById<ImageView>(R.id.item_icon)?.setImageBitmap(getPawBitmap())
            
            item.setOnClickListener { showNotificationDetail(n) }
            ac?.addView(item)
        }
    }

    private fun setupNotificationsScreen(v: View, notifs: List<Notification>, t: PawsTheme, onNav: (Int) -> Unit) {
        v.findViewById<View>(R.id.back)?.setOnClickListener { onNav(R.layout.screen_home) }
        val pc = v.findViewById<ViewGroup>(R.id.layout_notifications_placeholder) as? LinearLayout
        pc?.orientation = LinearLayout.VERTICAL

        val sortedNotifs = notifs.sortedWith(compareByDescending<Notification> { it.date }.thenByDescending { it.hour })

        fun style(tvId: Int, label: String, c: Int) {
            v.findViewById<TextView>(tvId)?.apply { text = if (c > 0) "$label ($c)" else label; textSize = 13f }
        }
        style(R.id.tv_filter_all, "Todas", sortedNotifs.count { !it.isRead })
        style(R.id.tv_filter_vaccines, "Vacunas", sortedNotifs.count { it.type == "VACCINE" && !it.isRead })
        style(R.id.tv_filter_bath, "Baños", sortedNotifs.count { it.type == "BATH" && !it.isRead })

        fun render(l: List<Notification>, cat: String) {
            pc?.removeAllViews()
            pc?.addView(TextView(this).apply { text = cat; setPadding(40, 20, 40, 20); background = GradientDrawable().apply { cornerRadius = 24f; setColor(ContextCompat.getColor(this@MainActivity, R.color.light_blue)) }; setTextColor(ContextCompat.getColor(this@MainActivity, R.color.dark_blue)); textSize = 14f; setTypeface(null, Typeface.BOLD); layoutParams = LinearLayout.LayoutParams(-2, -2).apply { setMargins(0, 0, 0, 30) } })
            l.forEach { n ->
                val item = LayoutInflater.from(v.context).inflate(R.layout.item_default_a, pc, false)
                applyThemeToView(item, t)
                item.findViewById<TextView>(R.id.tv_message_theme)?.text = n.title
                item.findViewById<TextView>(R.id.tv_message)?.text = n.message
                item.findViewById<TextView>(R.id.tv_time)?.text = n.date
                if (n.type == "WELCOME") item.findViewById<ImageView>(R.id.item_icon)?.setImageBitmap(getPawBitmap())
                item.setOnClickListener { showNotificationDetail(n) }
                pc?.addView(item)
            }
        }
        v.findViewById<View>(R.id.tv_filter_all)?.setOnClickListener { render(sortedNotifs, "Todas") }
        v.findViewById<View>(R.id.tv_filter_vaccines)?.setOnClickListener { render(sortedNotifs.filter { it.type == "VACCINE" }, "Vacunas") }
        v.findViewById<View>(R.id.tv_filter_bath)?.setOnClickListener { render(sortedNotifs.filter { it.type == "BATH" }, "Baños") }
        render(sortedNotifs, "Todas")
    }

    /**
     * Refina la pantalla de citas para que las tarjetas de "Próximas Citas" 
     * coincidan con el estilo de las alertas del Home.
     */
    private fun setupAppointmentsScreen(v: View, pets: List<Pet>, appts: List<Appointment>, p: Pet?, vm: PawsViewModel, t: PawsTheme, onSwitch: () -> Unit, onSchedule: () -> Unit, onAddPet: () -> Unit) {
        // Obtenemos el contenedor de las tarjetas de citas próximas
        val root = v.findViewById<View>(R.id.type_appointment)?.parent as? LinearLayout ?: return
        
        if (pets.isEmpty()) {
            applyEmptyState(root, t, onAddPet)
            v.findViewById<View>(R.id.add_appointment)?.visibility = View.GONE
            return
        }

        v.findViewById<View>(R.id.add_appointment)?.visibility = View.VISIBLE
        v.findViewById<TextView>(R.id.tv_switch_appointments)?.apply { 
            background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_card_blue)
            setTextColor(ContextCompat.getColor(this@MainActivity, t.primary)) 
        }
        v.findViewById<TextView>(R.id.tv_switch_grooming)?.apply { 
            background = null
            setTextColor(Color.GRAY)
            setOnClickListener { onSwitch() } 
        }
        
        val btn = v.findViewById<View>(R.id.add_appointment)
        btn?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_pink)
        btn?.findViewById<TextView>(R.id.btn_action_text)?.apply { 
            text = "Agendar Cita"; setTextColor(ContextCompat.getColor(this@MainActivity, R.color.cherry_red)) 
        }
        btn?.findViewById<ImageView>(R.id.btn_action_icon)?.setColorFilter(ContextCompat.getColor(this, R.color.cherry_red))
        btn?.setOnClickListener { onSchedule() }

        val up = appts.filter { !it.isPast && it.petId == p?.id }
        val old = appts.filter { it.isPast && it.petId == p?.id }
        
        // Limpiamos mensajes de estado vacío previos
        root.findViewWithTag<View>("empty_appointments_msg")?.let { root.removeView(it) }

        if (up.isEmpty()) {
            v.findViewById<View>(R.id.type_appointment)?.visibility = View.GONE
            v.findViewById<View>(R.id.doctor)?.visibility = View.GONE
            
            // Estado vacío centrado y con estilo refinado
            val emptyTv = TextView(this).apply { 
                tag = "empty_appointments_msg"
                text = "No hay citas próximas para ${p?.name ?: "esta mascota"}"
                gravity = Gravity.CENTER
                textSize = 15f
                setTextColor(Color.GRAY)
                setTypeface(null, Typeface.ITALIC)
                setPadding(0, 100, 0, 100)
                layoutParams = LinearLayout.LayoutParams(-1, -2)
            }
            root.addView(emptyTv)
        } else {
            v.findViewById<View>(R.id.type_appointment)?.visibility = View.VISIBLE
            v.findViewById<View>(R.id.doctor)?.visibility = View.VISIBLE
            
            val next = up.first()
            val nextView = v.findViewById<View>(R.id.type_appointment)
            val docView = v.findViewById<View>(R.id.doctor)
            
            // Vinculación para la Tarjeta de Tipo (nextView - item_default_b)
            nextView?.findViewById<TextView>(R.id.tv_message_theme)?.text = next.title
            nextView?.findViewById<TextView>(R.id.tv_message)?.text = "Para ${p?.name}"
            nextView?.findViewById<TextView>(R.id.tv_time)?.text = next.hour
            
            // Icono dinámico según el tipo de cita
            val iconNext = nextView?.findViewById<ImageView>(R.id.item_icon)
            if (next.type == "MEDICAL") {
                iconNext?.setImageResource(R.drawable.ic_extra_ambulancia)
            } else {
                iconNext?.setImageResource(R.drawable.ic_extra_bano)
            }
            iconNext?.setColorFilter(ContextCompat.getColor(this, t.primary))

            // Vinculación para la Tarjeta de Doctor (docView - item_default_a)
            docView?.findViewById<TextView>(R.id.tv_message_theme)?.text = next.doctor
            docView?.findViewById<TextView>(R.id.tv_message)?.text = "Sucursal: ${next.branch} | Notas: ${next.notes}"
            docView?.findViewById<TextView>(R.id.tv_time)?.text = next.date // Limpiamos "Conf" y ponemos la fecha
            
            // Icono distintivo para el profesional
            docView?.findViewById<ImageView>(R.id.item_icon)?.let { iv ->
                iv.setImageBitmap(getPawBitmap())
                iv.setColorFilter(ContextCompat.getColor(this, t.primary))
            }

            val longClick = View.OnLongClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Cancelar Cita")
                    .setMessage("¿Deseas cancelar la cita de ${p?.name} para el ${next.date}?")
                    .setPositiveButton("Sí, cancelar") { _, _ -> vm.cancelAppointment(next) }
                    .setNegativeButton("No", null)
                    .show()
                true
            }
            nextView?.setOnLongClickListener(longClick)
            docView?.setOnLongClickListener(longClick)
        }

        val container = v.findViewById<ViewGroup>(R.id.layout_old_appointments_placeholder)
        container?.removeAllViews()
        if (old.isEmpty()) {
            container?.addView(TextView(this).apply { text = "No hay historial de citas pasadas"; gravity = Gravity.CENTER; setPadding(0, 30, 0, 30); setTextColor(Color.LTGRAY) })
        } else {
            old.forEach { a ->
                val item = LayoutInflater.from(v.context).inflate(R.layout.item_default_b, container, false)
                applyThemeToView(item, t)
                item.findViewById<TextView>(R.id.tv_message_theme)?.text = a.title
                item.findViewById<TextView>(R.id.tv_message)?.text = "Doctor: ${a.doctor}"
                item.findViewById<TextView>(R.id.tv_time)?.text = a.date
                item.setOnLongClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("Eliminar Registro")
                        .setMessage("¿Deseas eliminar este registro del historial?")
                        .setPositiveButton("Eliminar") { _, _ -> vm.cancelAppointment(a) }
                        .setNegativeButton("Cancelar", null)
                        .show()
                    true
                }
                container?.addView(item)
            }
        }
    }

    private fun setupPetCardScreen(v: View, p: Pet?, pets: List<Pet>, procs: List<Procedure>, vm: PawsViewModel, t: PawsTheme, onAddPet: () -> Unit) {
        val rootContent = v.findViewById<View>(R.id.pet_info)?.parent as? LinearLayout
        
        if (pets.isEmpty() && rootContent != null) {
            applyEmptyState(rootContent, t, onAddPet)
            v.findViewById<View>(R.id.add_appointment)?.visibility = View.GONE
            return
        }

        v.findViewById<View>(R.id.add_appointment)?.visibility = View.VISIBLE
        val pc = v.findViewById<ViewGroup>(R.id.layout_pets_placeholder)
        pc?.removeAllViews()
        pets.forEach { pet ->
            val item = LayoutInflater.from(v.context).inflate(R.layout.item_pet, pc, false)
            applyThemeToView(item, t)
            item.findViewById<TextView>(R.id.pet_name)?.text = pet.name
            if (pet.id == p?.id) item.background.mutate().setTint(ContextCompat.getColor(this, t.accentBg))
            item.setOnClickListener { vm.selectPet(pet) }
            pc?.addView(item)
        }
        
        val btn = v.findViewById<View>(R.id.add_appointment)
        btn?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_pink)
        btn?.findViewById<TextView>(R.id.btn_action_text)?.apply { text = "Registrar Mascota"; setTextColor(ContextCompat.getColor(this@MainActivity, R.color.cherry_red)) }
        btn?.findViewById<ImageView>(R.id.btn_action_icon)?.setColorFilter(ContextCompat.getColor(this, R.color.cherry_red))
        btn?.setOnClickListener { onAddPet() }

        if (p == null) return
        v.findViewById<TextView>(R.id.pet_name)?.text = p.name
        val info = v.findViewById<View>(R.id.pet_info)
        info.findViewById<TextView>(R.id.pet_breed)?.text = p.breed
        info.findViewById<TextView>(R.id.pet_age)?.text = "${p.age} años"

        val cc = v.findViewById<ViewGroup>(R.id.rv_complete_procedures)
        cc?.removeAllViews()
        val comp = procs.filter { it.isCompleted }
        if (comp.isEmpty()) cc?.addView(TextView(this).apply { text = "No hay registros"; gravity = Gravity.CENTER; setPadding(0, 20, 0, 20); setTextColor(Color.GRAY); layoutParams = LinearLayout.LayoutParams(-1, -2) })
        else comp.forEach { proc -> val item = LayoutInflater.from(v.context).inflate(R.layout.item_complete_procedures, cc, false); applyThemeToView(item, t); item.findViewById<TextView>(R.id.btn_action_text)?.text = proc.name; cc.addView(item) }
        
        val penc = v.findViewById<ViewGroup>(R.id.layout_old_appointments_placeholder)
        penc?.removeAllViews()
        val pend = procs.filter { !it.isCompleted }
        if (pend.isEmpty()) penc?.addView(TextView(this).apply { text = "Todo al día"; gravity = Gravity.CENTER; setPadding(0, 20, 0, 20); setTextColor(Color.GRAY); layoutParams = LinearLayout.LayoutParams(-1, -2) })
        else pend.forEach { proc -> 
            val item = LayoutInflater.from(v.context).inflate(R.layout.item_default_b, penc, false)
            applyThemeToView(item, t)
            item.findViewById<TextView>(R.id.tv_message_theme)?.text = proc.name
            item.setOnClickListener {
                AlertDialog.Builder(this)
                    .setMessage("¿Marcar ${proc.name} como completado?")
                    .setPositiveButton("Sí") { _, _ -> vm.completeProcedure(proc) }
                    .setNegativeButton("No", null)
                    .show()
            }
            penc.addView(item) 
        }
    }

    private fun setupProfileScreen(v: View, u: User?, pets: List<Pet>, vm: PawsViewModel, t: PawsTheme, onEdit: () -> Unit, onChangePhoto: () -> Unit) {
        val nameTv = v.findViewById<TextView>(R.id.user)
        nameTv?.text = u?.name ?: "Usuario"
        
        v.findViewById<TextView>(R.id.tv_user_email)?.text = u?.email
        v.findViewById<TextView>(R.id.tv_user_phone)?.text = u?.phone ?: "Sin teléfono"
        v.findViewById<TextView>(R.id.tv_user_address)?.text = u?.address ?: "Sin dirección"
        
        val iv = v.findViewById<ImageView>(R.id.img_avatar)
        u?.photoUrl?.takeIf { it.isNotEmpty() }?.let { url -> iv?.load(url) { crossfade(true); transformations(CircleCropTransformation()) } }
        
        v.findViewById<View>(R.id.fl_avatar_container)?.setOnClickListener { onChangePhoto() }
        v.findViewById<View>(R.id.edit_user)?.setOnClickListener { onEdit() }
        v.findViewById<View>(R.id.close_session)?.setOnClickListener { vm.logout() }
        
        val container = v.findViewById<ViewGroup>(R.id.layout_styles_placeholder)
        container?.removeAllViews()
        if (pets.isEmpty()) container?.addView(TextView(this).apply { text = "No hay mascotas"; textSize = 14f; setTextColor(Color.GRAY); setPadding(0, 40, 0, 40); gravity = Gravity.CENTER; layoutParams = LinearLayout.LayoutParams(-1, -2) })
        else pets.forEach { pet -> 
            val item = LayoutInflater.from(v.context).inflate(R.layout.item_pet_info_simple, container, false)
            applyThemeToView(item, t)
            item.findViewById<TextView>(R.id.pet_name)?.text = pet.name
            item.findViewById<TextView>(R.id.pet_breed)?.text = pet.breed
            item.setOnLongClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Eliminar")
                    .setPositiveButton("Sí") { _, _ -> vm.removePet(pet) }
                    .setNegativeButton("No", null)
                    .show()
                true
            }
            container.addView(item) 
        }
    }

    private fun setupBathScreen(v: View, pets: List<Pet>, t: PawsTheme, onBack: () -> Unit, onSchedule: () -> Unit, onAddPet: () -> Unit) {
        val root = v.findViewById<View>(R.id.pet_onService)?.parent as? ViewGroup
        
        if (pets.isEmpty() && root != null) {
            applyEmptyState(root, t, onAddPet)
            v.findViewById<View>(R.id.add_appointment)?.visibility = View.GONE
            return
        }

        v.findViewById<View>(R.id.add_appointment)?.visibility = View.VISIBLE
        v.findViewById<TextView>(R.id.tv_switch_appointments)?.apply { background = null; setTextColor(Color.GRAY); setOnClickListener { onBack() } }
        v.findViewById<TextView>(R.id.tv_switch_grooming)?.apply { background = ContextCompat.getDrawable(this@MainActivity, R.drawable.bg_card_blue); setTextColor(ContextCompat.getColor(this@MainActivity, t.primary)) }
        val btn = v.findViewById<View>(R.id.add_appointment)
        btn?.background = ContextCompat.getDrawable(this, R.drawable.bg_rounded_pink)
        btn?.findViewById<TextView>(R.id.btn_action_text)?.apply { text = "Agendar Cita"; setTextColor(ContextCompat.getColor(this@MainActivity, R.color.cherry_red)) }
        btn?.findViewById<ImageView>(R.id.btn_action_icon)?.setColorFilter(ContextCompat.getColor(this, R.color.cherry_red))
        btn?.setOnClickListener { onSchedule() }

        v.findViewById<View>(R.id.pet_onService)?.visibility = View.GONE
        root?.findViewWithTag<View>("empty_b")?.let { root.removeView(it) }
        root?.addView(TextView(this).apply { tag = "empty_b"; text = "No hay mascotas en servicio"; gravity = Gravity.CENTER; setPadding(0, 40, 0, 40); setTextColor(Color.GRAY); layoutParams = LinearLayout.LayoutParams(-1, -2) })
    }

    private fun setupStylesScreen(v: View, t: PawsTheme, onApply: (PawsTheme) -> Unit) {
        var selected = t
        val cards = mapOf(R.id.theme_card_ocean to PawsTheme.OCEAN, R.id.theme_card_forest to PawsTheme.FOREST, R.id.theme_card_sunset to PawsTheme.SUNSET)
        cards.forEach { (id, theme) -> v.findViewById<View>(id)?.setOnClickListener { selected = theme } }
        v.findViewById<View>(R.id.apply_style)?.setOnClickListener { onApply(selected) }
    }

    private fun setupLoginScreen(v: View, vm: PawsViewModel) {
        val etN = v.findViewById<android.widget.EditText>(R.id.et_name)
        val etE = v.findViewById<android.widget.EditText>(R.id.et_email)
        val etP = v.findViewById<android.widget.EditText>(R.id.et_password)
        val btn = v.findViewById<View>(R.id.btn_login)
        val tvT = v.findViewById<TextView>(R.id.tv_toggle_auth)
        var isLog = true
        tvT?.setOnClickListener { isLog = !isLog; v.findViewById<TextView>(R.id.tv_login_title)?.text = if (isLog) "Bienvenido" else "Crea tu cuenta"; etN?.visibility = if (isLog) View.GONE else View.VISIBLE; btn?.findViewById<TextView>(R.id.btn_action_text)?.text = if (isLog) "Iniciar Sesión" else "Registrarse" }
        btn?.setOnClickListener {
            val email = etE?.text.toString().trim(); val pass = etP?.text.toString().trim(); val name = etN?.text.toString().trim()
            if (email.isEmpty() || pass.isEmpty() || (!isLog && name.isEmpty())) return@setOnClickListener
            if (isLog) vm.login(email, pass) { _, _ -> } else vm.register(email, pass, name) { _, _ -> }
        }
    }

    private fun applyThemeToView(v: View, t: PawsTheme, pId: String = "") {
        if (v.tag == "palette" || v.tag == "badge") return
        val id = try { resources.getResourceEntryName(v.id).lowercase() } catch (e: Exception) { pId }
        val pri = ContextCompat.getColor(this, t.primary); val acc = ContextCompat.getColor(this, t.accent); val abg = ContextCompat.getColor(this, t.accentBg)
        if (v.background != null && !id.contains("theme_card") && !id.contains("bottom_nav")) { 
            if (id != "btn_notifications") v.background.mutate().setTint(if (id.contains("btn") || id.contains("apply") || id.contains("edit") || id.contains("cartilla")) abg else Color.WHITE) 
        }
        if (v is TextView) { v.setTextColor(if (id.contains("btn") || id.contains("apply") || id.contains("cartilla")) acc else pri)
            if (v.text.contains("🐾")) { val span = android.text.SpannableString(v.text.toString().replace("🐾", "🐾\uFE0E")); span.setSpan(android.text.style.ForegroundColorSpan(pri), 0, span.length, 0); v.text = span } }
        if (v is ImageView && !id.contains("nav")) v.setColorFilter(pri)
        if (v is ViewGroup) { for (i in 0 until v.childCount) applyThemeToView(v.getChildAt(i), t, id) }
    }
}
