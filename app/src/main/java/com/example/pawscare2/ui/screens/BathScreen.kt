package com.example.pawscare2.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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

// Pantalla de monitoreo en tiempo real del progreso de estética y spa
@Composable
fun BathScreen(
    pets: List<Pet>,
    appointments: List<Appointment>,
    user: User?,
    currentTheme: PawsTheme,
    viewModel: PawsViewModel,
    onNavigateToAppointments: () -> Unit,
    onScheduleClick: (String) -> Unit,
    onAddPetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))

    val isVet = user?.role == "VETERINARIAN"
    val bathAppts = appointments.filter { it.type == "GROOMING" && !it.isPast && (isVet || it.userId == user?.id) }

    var selectedApptForUpdate by remember { mutableStateOf<Appointment?>(null) }

    selectedApptForUpdate?.let { appt ->
        AlertDialog(
            onDismissRequest = { selectedApptForUpdate = null },
            title = { Text("Actualizar Progreso de ${appt.petName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        25 to "25% - Iniciando servicio / Baño",
                        50 to "50% - En proceso / Afeitado o Corte",
                        75 to "75% - Secando / Cepillado",
                        100 to "100% - ¡Listo para recoger!"
                    ).forEach { (progress, label) ->
                        Button(
                            onClick = {
                                viewModel.updateBathProgress(appt, progress)
                                selectedApptForUpdate = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (appt.progress == progress) primaryColor else accentBgColor
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Cambiar progreso a $label" }
                        ) {
                            Text(
                                text = label,
                                color = if (appt.progress == progress) Color.White else primaryColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedApptForUpdate = null }) { Text("Cerrar") }
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
            // Pestañas
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
                            .clickable { onNavigateToAppointments() }
                            .padding(vertical = 12.dp)
                            .semantics { contentDescription = "Pestaña Consultas Médicas" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🩺 Consultas Médicas",
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(accentBgColor)
                            .padding(vertical = 12.dp)
                            .semantics { contentDescription = "Pestaña activa Estética y Spa" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🛁 Estética y Spa",
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
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
                    .clickable { onScheduleClick("GROOMING") }
                    .semantics { contentDescription = "Boton para agendar servicio de estética" }
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
                        text = "Agendar Cita de Estética",
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
                    description = "Agrega a tu mascota para solicitar un servicio de baño, corte o spa.",
                    buttonText = "Registrar Mascota",
                    currentTheme = currentTheme,
                    onButtonClick = onAddPetClick
                )
            }
        } else {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Mascotas en Servicio de Spa / Baño",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (bathAppts.isEmpty()) {
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
                                text = "No hay servicios de baño en proceso actualmente 🛁",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            } else {
                items(bathAppts) { appt ->
                    val animatedProgress by animateFloatAsState(
                        targetValue = appt.progress / 100f,
                        animationSpec = tween(durationMillis = 1000),
                        label = "BathProgress"
                    )

                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .then(
                                if (isVet) Modifier.clickable { selectedApptForUpdate = appt }
                                else Modifier
                            )
                            .semantics {
                                contentDescription = "Servicio de spa para ${appt.petName}. Estado ${appt.status}. Progreso ${appt.progress} por ciento."
                            }
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(accentBgColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🛁", fontSize = 22.sp)
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "${appt.petName} en Spa",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = primaryColor
                                        )
                                        Text(
                                            text = "Servicio: ${appt.title}",
                                            fontSize = 13.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (appt.progress >= 100) Color(0xFFE8F5E9) else accentBgColor
                                ) {
                                    Text(
                                        text = appt.status,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (appt.progress >= 100) Color(0xFF2E7D32) else accentColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Progreso", fontSize = 12.sp, color = Color.Gray)
                                Text("${appt.progress}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .semantics { contentDescription = "Indicador visual de progreso a ${appt.progress} por ciento" },
                                color = primaryColor,
                                trackColor = Color(0xFFE0E0E0)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = when {
                                    isVet -> "💡 Toca la tarjeta para actualizar el progreso"
                                    appt.progress >= 100 -> "🎉 ¡Tu mascota está lista! Pasa a recogerla."
                                    else -> "📲 Te notificaremos en cuanto termine el servicio."
                                },
                                fontSize = 12.sp,
                                fontWeight = if (appt.progress >= 100) FontWeight.Bold else FontWeight.Normal,
                                color = if (appt.progress >= 100) Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
