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
import com.example.pawscare2.model.Pet
import com.example.pawscare2.model.Procedure
import com.example.pawscare2.model.User
import com.example.pawscare2.ui.components.EmptyStateCard
import com.example.pawscare2.ui.components.PetSelectorRow

@Composable
fun PetCardScreen(
    pets: List<Pet>,
    selectedPet: Pet?,
    procedures: List<Procedure>,
    user: User?,
    viewModel: PawsViewModel,
    currentTheme: PawsTheme,
    onAddPetClick: () -> Unit,
    onEditPetClick: (Pet) -> Unit,
    onDeletePetClick: ((Pet) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))

    val isVet = user?.role == "VETERINARIAN"
    val completedProcs = procedures.filter { it.isCompleted }
    val pendingProcs = procedures.filter { !it.isCompleted }

    var showAddProcedureDialog by remember { mutableStateOf(false) }
    var procName by remember { mutableStateOf("") }
    var procDoctor by remember { mutableStateOf("") }
    var procDate by remember { mutableStateOf("") }

    if (showAddProcedureDialog && selectedPet != null) {
        AlertDialog(
            onDismissRequest = { showAddProcedureDialog = false },
            title = { Text("Añadir Procedimiento Médico", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Mascota: ${selectedPet.name}", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = procName,
                        onValueChange = { procName = it },
                        label = { Text("Nombre (ej. Vacuna Rabia)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = procDoctor,
                        onValueChange = { procDoctor = it },
                        label = { Text("Doctor / Clinica") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = procDate,
                        onValueChange = { procDate = it },
                        label = { Text("Fecha (ej. 24 Sep 2026)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.addProcedure(selectedPet.id, procName, procDoctor, procDate)
                        showAddProcedureDialog = false
                        procName = ""; procDoctor = ""; procDate = ""
                    },
                    enabled = procName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) { Text("Guardar") }
            },
            dismissButton = {
                TextButton(onClick = { showAddProcedureDialog = false }) { Text("Cancelar") }
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
            PetSelectorRow(
                pets = pets,
                selectedPet = selectedPet,
                currentTheme = currentTheme,
                onPetSelect = { viewModel.selectPet(it) },
                onAddPetClick = onAddPetClick
            )
        }

        if (pets.isEmpty() && !isVet) {
            item {
                EmptyStateCard(
                    title = "¡No hay mascotas registradas!",
                    description = "Registra a tu mascota para acceder a su cartilla de vacunación y registro médico.",
                    buttonText = "Registrar Mascota",
                    currentTheme = currentTheme,
                    onButtonClick = onAddPetClick
                )
            }
        } else if (selectedPet != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${selectedPet.getIconEmoji()} ${selectedPet.name}",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                    )
                                    if (selectedPet.isVerified) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = Color(0xFFE8F5E9)
                                        ) {
                                            Text(
                                                text = "VERIFICADO ✓",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF2E7D32)
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${selectedPet.breed} • ${selectedPet.age} años • ${selectedPet.weight} kg",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                                if (selectedPet.microchip.isNotBlank()) {
                                    Text(
                                        text = "Folio Registro: ${selectedPet.microchip}",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onEditPetClick(selectedPet) }) {
                                    Text("✏️", fontSize = 18.sp)
                                }
                                if (onDeletePetClick != null) {
                                    IconButton(onClick = { onDeletePetClick(selectedPet) }) {
                                        Text("🗑️", fontSize = 18.sp)
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(accentBgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(selectedPet.getIconEmoji(), fontSize = 22.sp)
                                }
                            }
                        }

                        if (isVet) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { showAddProcedureDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = accentBgColor),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_extra_add),
                                    contentDescription = "Añadir",
                                    tint = accentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Añadir Procedimiento Médico", color = accentColor, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Procedimientos y Vacunas Completadas",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (completedProcs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No hay procedimientos o vacunas completadas aún", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(completedProcs) { proc ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_extra_check),
                                contentDescription = "Completado",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(proc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryColor)
                                if (proc.doctor.isNotBlank()) Text("Doctor: ${proc.doctor}", fontSize = 12.sp, color = Color.Gray)
                            }
                            if (proc.date.isNotBlank()) Text(proc.date, fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Próximas Vacunas / Pendientes",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (pendingProcs.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("¡Todo al día! Sin pendientes 🐾", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(pendingProcs) { proc ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .then(
                                if (isVet) Modifier.clickable { viewModel.completeProcedure(proc) }
                                else Modifier
                            )
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(proc.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = primaryColor)
                                if (isVet) Text("Toca para marcar como completado", fontSize = 11.sp, color = accentColor)
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = accentBgColor
                            ) {
                                Text(
                                    text = "Pendiente",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
