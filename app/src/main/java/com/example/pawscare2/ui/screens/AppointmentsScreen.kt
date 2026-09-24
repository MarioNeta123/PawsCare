package com.example.pawscare2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.pawscare2.PawsTheme
import com.example.pawscare2.PawsViewModel
import com.example.pawscare2.R
import com.example.pawscare2.model.Appointment
import com.example.pawscare2.model.Pet
import com.example.pawscare2.model.User
import com.example.pawscare2.ui.components.EmptyStateCard

@Composable
fun AppointmentsScreen(
    pets: List<Pet>,
    appointments: List<Appointment>,
    selectedPet: Pet?,
    user: User?,
    viewModel: PawsViewModel,
    currentTheme: PawsTheme,
    onNavigateToBath: () -> Unit,
    onScheduleClick: (String) -> Unit,
    onAddPetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))

    val isVet = user?.role == "VETERINARIAN"
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Consultas Médicas, 1 = Estética

    val medicalAppts = appointments.filter { it.type == "MEDICAL" && (isVet || it.petId == selectedPet?.id || selectedPet == null) }
    val upcomingAppts = medicalAppts.filter { !it.isPast }
    val historyAppts = medicalAppts.filter { it.isPast }

    var selectedApptForDetail by remember { mutableStateOf<Appointment?>(null) }
    var apptToCancel by remember { mutableStateOf<Appointment?>(null) }

    // Diálogo con Detalles en Grande de la Cita Médica
    selectedApptForDetail?.let { appt ->
        AlertDialog(
            onDismissRequest = { selectedApptForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(accentBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🩺", fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(appt.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryColor)
                        Text("Mascota: ${appt.petName}", fontSize = 14.sp, color = Color.Gray)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFEEEEEE))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📅 Fecha:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                        Text(appt.date, fontSize = 14.sp, color = primaryColor, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("⏰ Hora:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                        Text(appt.hour, fontSize = 14.sp, color = primaryColor, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("👨‍⚕️ Atendido por:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                        Text(appt.doctor.ifBlank { "Veterinario asignado" }, fontSize = 14.sp, color = Color.DarkGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("📍 Sucursal:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                        Text(appt.branch.ifBlank { "Sucursal Principal" }, fontSize = 14.sp, color = Color.DarkGray)
                    }

                    if (appt.notes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("📝 Motivo / Notas:", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF5F5F5),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = appt.notes,
                                modifier = Modifier.padding(12.dp),
                                fontSize = 13.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!appt.isPast) {
                        Button(
                            onClick = {
                                apptToCancel = appt
                                selectedApptForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Cancelar Cita", color = Color.Red, fontWeight = FontWeight.Bold)
                        }
                    }
                    Button(
                        onClick = { selectedApptForDetail = null },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        )
    }

    // Diálogo de Confirmación de Cancelación
    apptToCancel?.let { appt ->
        AlertDialog(
            onDismissRequest = { apptToCancel = null },
            title = { Text("Cancelar Cita", fontWeight = FontWeight.Bold) },
            text = { Text("¿Confirmas que deseas cancelar la cita de ${appt.petName} del ${appt.date} a las ${appt.hour}?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelAppointment(appt)
                        apptToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Sí, Cancelar Cita", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { apptToCancel = null }) { Text("No, Conservar") }
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Pestañas Médica vs Estética
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == 0) accentBgColor else Color.Transparent)
                            .clickable { selectedTab = 0 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🩺 Consultas Médicas",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 0) primaryColor else Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedTab == 1) accentBgColor else Color.Transparent)
                            .clickable {
                                selectedTab = 1
                                onNavigateToBath()
                            }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🛁 Estética y Spa",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTab == 1) primaryColor else Color.Gray,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        item {
            // Botón Agendar Cita
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = accentBgColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onScheduleClick("MEDICAL") }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_extra_add),
                        contentDescription = "Agendar",
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Agendar Consulta Médica",
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        fontSize = 15.sp
                    )
                }
            }
        }

        if (pets.isEmpty() && !isVet) {
            item {
                EmptyStateCard(
                    title = "¡No hay mascotas registradas!",
                    description = "Para agendar una cita médica, primero debes registrar a tu mascota.",
                    buttonText = "Registrar Mascota",
                    currentTheme = currentTheme,
                    onButtonClick = onAddPetClick
                )
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Próximas Citas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (upcomingAppts.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tienes citas médicas próximas 🩺",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(upcomingAppts) { appt ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { selectedApptForDetail = appt }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(accentBgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🩺", fontSize = 20.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = appt.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = "Para: ${appt.petName}",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = accentBgColor
                                ) {
                                    Text(
                                        text = appt.hour,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = Color(0xFFF0F0F0))
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Doctor: ${appt.doctor}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = "Sucursal: ${appt.branch} • ${appt.date}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Text(
                                        text = "🔍 Toca para ver detalles completos",
                                        fontSize = 11.sp,
                                        color = accentColor
                                    )
                                }

                                TextButton(onClick = { apptToCancel = appt }) {
                                    Text("Cancelar", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Historial de Citas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (historyAppts.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin historial de citas anteriores", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(historyAppts) { appt ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedApptForDetail = appt }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(appt.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryColor)
                                Text("Mascota: ${appt.petName} • Doctor: ${appt.doctor}", fontSize = 12.sp, color = Color.DarkGray)
                            }
                            Text(appt.date, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
