package com.example.pawscare2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.pawscare2.PawsTheme
import com.example.pawscare2.PawsViewModel

@Composable
fun LoginScreen(
    viewModel: PawsViewModel,
    currentTheme: PawsTheme,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("CUSTOMER") } // "CUSTOMER" o "VETERINARIAN"
    var clinicCode by remember { mutableStateOf("") } // Código de seguridad para veterinarios
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(accentBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐾", fontSize = 36.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PawsCare",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                )

                Text(
                    text = if (isLoginMode) "¡Bienvenido de nuevo!" else "Crea tu cuenta",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )

                Spacer(modifier = Modifier.height(20.dp))

                if (!isLoginMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre Completo") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Tipo de Cuenta:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = (selectedRole == "CUSTOMER"),
                            onClick = { selectedRole = "CUSTOMER" },
                            label = { Text("Cliente 🐶", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = (selectedRole == "VETERINARIAN"),
                            onClick = { selectedRole = "VETERINARIAN" },
                            label = { Text("Veterinario 🩺", fontSize = 13.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    if (selectedRole == "VETERINARIAN") {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = clinicCode,
                            onValueChange = { clinicCode = it },
                            label = { Text("Código Clínica / Cédula (ej. VET2026)") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "💡 Código de prueba para clínica: VET2026",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Start).padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Correo Electrónico") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        isLoading = true
                        if (isLoginMode) {
                            viewModel.login(email, password) { _, _ -> isLoading = false }
                        } else {
                            viewModel.register(email, password, name, selectedRole, clinicCode) { _, _ -> isLoading = false }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && (isLoginMode || (name.isNotBlank() && (selectedRole != "VETERINARIAN" || clinicCode.isNotBlank()))),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = if (isLoginMode) "Iniciar Sesión" else "Registrarse",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.clickable { isLoginMode = !isLoginMode },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLoginMode) "¿No tienes cuenta? " else "¿Ya tienes cuenta? ",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = if (isLoginMode) "Regístrate" else "Inicia Sesión",
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
