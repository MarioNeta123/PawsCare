package com.example.pawscare2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.pawscare2.model.Notification
import com.example.pawscare2.model.User

@Composable
fun NotificationsScreen(
    notifications: List<Notification>,
    user: User?,
    viewModel: PawsViewModel,
    currentTheme: PawsTheme,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))

    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, VACCINE, BATH, APPOINTMENT
    var selectedNotifForDetail by remember { mutableStateOf<Notification?>(null) }
    var showBroadcastDialog by remember { mutableStateOf(false) }
    var broadcastTitle by remember { mutableStateOf("") }
    var broadcastMsg by remember { mutableStateOf("") }

    val filteredNotifs = when (selectedFilter) {
        "VACCINE" -> notifications.filter { it.type == "VACCINE" }
        "BATH" -> notifications.filter { it.type == "BATH" }
        "APPOINTMENT" -> notifications.filter { it.type == "APPOINTMENT" }
        else -> notifications
    }.sortedWith(compareByDescending<Notification> { it.date }.thenByDescending { it.hour })

    if (showBroadcastDialog) {
        AlertDialog(
            onDismissRequest = { showBroadcastDialog = false },
            title = { Text("📢 Enviar Comunicado Clínico", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        label = { Text("Título del aviso (ej. Recordatorio de Vacunación)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = broadcastMsg,
                        onValueChange = { broadcastMsg = it },
                        label = { Text("Mensaje / Indicaciones médicas") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.sendBroadcastNotification(broadcastTitle, broadcastMsg)
                        showBroadcastDialog = false
                        broadcastTitle = ""; broadcastMsg = ""
                    },
                    enabled = broadcastTitle.isNotBlank() && broadcastMsg.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) { Text("Enviar Comunicado") }
            },
            dismissButton = {
                TextButton(onClick = { showBroadcastDialog = false }) { Text("Cancelar") }
            }
        )
    }

    selectedNotifForDetail?.let { notif ->
        AlertDialog(
            onDismissRequest = { selectedNotifForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(notif.getIconEmoji(), fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primaryColor)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(notif.message, fontSize = 15.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Fecha: ${notif.date} • ${notif.hour}", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!notif.isRead) viewModel.markAsRead(notif.id)
                        selectedNotifForDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                ) { Text("Entendido") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
    ) {
        // Top Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Volver",
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Centro de Notificaciones",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    ),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                    Text("Marcar leídas", fontSize = 12.sp, color = accentColor, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (user?.role == "VETERINARIAN") {
            Button(
                onClick = { showBroadcastDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = accentBgColor),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("📢 Enviar Comunicado Clínico a Clientes", color = accentColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        // Filtros
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            val filters = listOf(
                "ALL" to "Todas 🔔",
                "VACCINE" to "Vacunas 💉",
                "BATH" to "Baños 🛁",
                "APPOINTMENT" to "Citas 🩺"
            )

            items(filters) { (type, label) ->
                val isSelected = selectedFilter == type
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) primaryColor else Color.White,
                    shadowElevation = if (isSelected) 2.dp else 1.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { selectedFilter = type }
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else Color.DarkGray
                    )
                }
            }
        }

        // Lista de Notificaciones
        if (filteredNotifs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No hay notificaciones en esta categoría 🔔", color = Color.Gray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp, start = 16.dp, end = 16.dp)
            ) {
                items(filteredNotifs) { notif ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (!notif.isRead) accentBgColor.copy(alpha = 0.5f) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                selectedNotifForDetail = notif
                                if (!notif.isRead) viewModel.markAsRead(notif.id)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(accentBgColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(notif.getIconEmoji(), fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notif.title,
                                    fontWeight = if (!notif.isRead) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp,
                                    color = primaryColor
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notif.message,
                                    fontSize = 12.sp,
                                    color = Color.DarkGray,
                                    maxLines = 2
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(horizontalAlignment = Alignment.End) {
                                Text(notif.date, fontSize = 10.sp, color = Color.Gray)
                                Text(notif.hour, fontSize = 10.sp, color = Color.Gray)
                                if (!notif.isRead) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(accentColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
