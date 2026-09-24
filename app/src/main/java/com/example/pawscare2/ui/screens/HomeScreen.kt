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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.pawscare2.PawsTheme
import com.example.pawscare2.PawsViewModel
import com.example.pawscare2.R
import com.example.pawscare2.model.Notification
import com.example.pawscare2.model.Pet
import com.example.pawscare2.model.User
import com.example.pawscare2.ui.components.EmptyStateCard
import com.example.pawscare2.ui.components.PetSelectorRow

// Pantalla principal de la aplicación con soporte para clientes y veterinarios
@Composable
fun HomeScreen(
    user: User?,
    pets: List<Pet>,
    selectedPet: Pet?,
    notifications: List<Notification>,
    viewModel: PawsViewModel,
    currentTheme: PawsTheme,
    onNavigateToAppointments: () -> Unit,
    onNavigateToBath: () -> Unit,
    onNavigateToPetCard: () -> Unit,
    onAddPetClick: () -> Unit,
    onEditPetClick: (Pet) -> Unit,
    onNotificationClick: (Notification) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))

    val isVet = user?.role == "VETERINARIAN"
    var searchQuery by remember { mutableStateOf("") }

    // Filtrado de pacientes para el veterinario en tiempo real
    val filteredPets = if (searchQuery.isBlank()) {
        pets
    } else {
        pets.filter { pet ->
            pet.name.contains(searchQuery, ignoreCase = true) ||
                    pet.breed.contains(searchQuery, ignoreCase = true) ||
                    pet.species.contains(searchQuery, ignoreCase = true) ||
                    pet.microchip.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            // Banner superior de saludo
            Card(
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = if (isVet) "Panel del Doctor ${user?.name ?: ""}" else "Saludo principal para ${user?.name ?: ""}"
                    }
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = if (isVet) "¡Hola, Dr(a). ${user?.name ?: "Veterinario"}!" else "¡Hola, ${user?.name ?: "Usuario"}!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isVet) "Modo Veterinario 🩺" else "Perfil Cliente 🐶",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Normal
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (pets.isEmpty()) "Registra a tu peludo para comenzar 🐾" else "${selectedPet?.name ?: "Tu mascota"} te manda un saludo 🐾",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.White.copy(alpha = 0.9f))
                    )
                }
            }
        }

        // Panel exclusivo para Veterinarios
        if (isVet) {
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Dashboard Clínico PawsCare 📊",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = primaryColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${pets.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = primaryColor)
                                Text("Pacientes 🐾", fontSize = 11.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${notifications.size}", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = primaryColor)
                                Text("Alertas Clínicas 🔔", fontSize = 11.sp, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Buscador de Pacientes en la Base de Datos
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text("🔍 Buscar Paciente (Nombre, Raza, Folio)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Buscador de pacientes clínicos por nombre, raza o microchip" }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = onAddPetClick,
                            colors = ButtonDefaults.buttonColors(containerColor = accentBgColor),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("➕ Registrar Nuevo Paciente en Clínica", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isVet) "Pacientes de la Clínica (${filteredPets.size}) 🐾" else "Tus Mascotas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (pets.isEmpty()) {
                EmptyStateCard(
                    title = "¡Aún no tienes mascotas registradas!",
                    description = "Agrega a tu perro o gato para llevar el control de citas, estética y cartilla médica.",
                    buttonText = "Registrar Mascota",
                    currentTheme = currentTheme,
                    onButtonClick = onAddPetClick
                )
            } else {
                PetSelectorRow(
                    pets = filteredPets,
                    selectedPet = selectedPet,
                    currentTheme = currentTheme,
                    onPetSelect = { viewModel.selectPet(it) },
                    onAddPetClick = onAddPetClick
                )

                selectedPet?.let { pet ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onNavigateToPetCard() }
                            .semantics {
                                contentDescription = "Paciente ${pet.name}, especie ${pet.species}, raza ${pet.breed}, edad ${pet.age} años. Toca para ver cartilla médica."
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = pet.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = primaryColor
                                )
                                Text(
                                    text = "${pet.getIconEmoji()} ${pet.breed} • ${pet.age} años • ${pet.weight} kg",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                                if (pet.microchip.isNotBlank()) {
                                    Text(
                                        text = "Folio: ${pet.microchip}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "📋 Toca para ver su cartilla médica",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { onEditPetClick(pet) }
                                        .padding(8.dp)
                                        .semantics { contentDescription = "Boton para editar datos de ${pet.name}" }
                                ) {
                                    Text("✏️", fontSize = 18.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(accentBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(pet.getIconEmoji(), fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Acceso Rápido",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToAppointments() }
                        .semantics { contentDescription = "Acceso a sección de Citas Médicas" }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_extra_ambulancia),
                            contentDescription = "Citas Médicas",
                            tint = primaryColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Citas Médicas", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = primaryColor)
                        Text("Agendar consulta", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onNavigateToBath() }
                        .semantics { contentDescription = "Acceso a sección de Estética y Spa" }
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_extra_bano),
                            contentDescription = "Estética y Spa",
                            tint = accentColor,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Estética y Spa", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = accentColor)
                        Text("Ver estado de baño", fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Alertas Recientes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (notifications.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Sin alertas pendientes 🔔", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        } else {
            items(notifications.take(3)) { notification ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onNotificationClick(notification) }
                        .semantics { contentDescription = "Notificación: ${notification.title}. ${notification.message}" }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(accentBgColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(notification.getIconEmoji(), fontSize = 20.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = notification.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = primaryColor
                            )
                            Text(
                                text = notification.message,
                                fontSize = 12.sp,
                                color = Color.DarkGray,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = notification.hour,
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
