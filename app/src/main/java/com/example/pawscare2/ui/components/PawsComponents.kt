package com.example.pawscare2.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.pawscare2.PawsTheme
import com.example.pawscare2.R
import com.example.pawscare2.model.Pet

// Componentes reutilizables de la interfaz de PawsCare con accesibilidad TalkBack
@Composable
fun PawsHeader(
    title: String,
    unreadCount: Int,
    currentTheme: PawsTheme,
    onNotificationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))

    val rotation = remember { Animatable(0f) }
    LaunchedEffect(unreadCount) {
        if (unreadCount > 0) {
            rotation.animateTo(15f, animationSpec = spring())
            rotation.animateTo(-15f, animationSpec = spring())
            rotation.animateTo(10f, animationSpec = spring())
            rotation.animateTo(0f, animationSpec = spring())
        }
    }

    Surface(
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(accentBgColor)
                        .semantics { contentDescription = "Logo PawsCare" },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "P",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = primaryColor
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onNotificationClick() }
                    .padding(8.dp)
                    .semantics {
                        contentDescription = if (unreadCount > 0) "Campana de notificaciones. Tienes $unreadCount mensajes sin leer." else "Campana de notificaciones sin mensajes nuevos"
                    }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.icon_main_notifi_false),
                    contentDescription = "Notificaciones",
                    tint = primaryColor,
                    modifier = Modifier
                        .size(28.dp)
                        .rotate(rotation.value)
                )
                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

enum class NavigationTab(val route: Int, val title: String, val iconRes: Int) {
    HOME(R.layout.screen_home, "Inicio", R.drawable.icon_main_inicio),
    APPOINTMENTS(R.layout.screen_appointments, "Citas", R.drawable.icon_main_citas),
    PET_CARD(R.layout.screen_pet_card, "Cartilla", R.drawable.icon_main_cartilla),
    PROFILE(R.layout.screen_profile, "Perfil", R.drawable.icon_main_perfil),
    STYLES(R.layout.screen_styles, "Estilos", R.drawable.icon_main_estilos)
}

@Composable
fun PawsBottomNavigation(
    currentScreen: Int,
    currentTheme: PawsTheme,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))
    val unselectedColor = Color.Gray

    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavigationTab.entries.forEach { tab ->
                val isSelected = currentScreen == tab.route
                val tabColor = when {
                    isSelected && tab == NavigationTab.PET_CARD -> accentColor
                    isSelected -> primaryColor
                    else -> unselectedColor
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(tab.route) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .semantics { contentDescription = "Navegar a ${tab.title}. ${if (isSelected) "Pestaña activa" else "Toca para abrir"}" }
                ) {
                    Icon(
                        painter = painterResource(id = tab.iconRes),
                        contentDescription = tab.title,
                        tint = tabColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = tabColor
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyStateCard(
    title: String,
    description: String,
    buttonText: String,
    currentTheme: PawsTheme,
    onButtonClick: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryButtonText: String? = null,
    onSecondaryButtonClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))
    val accentColor = Color(ContextCompat.getColor(context, currentTheme.accent))

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(accentBgColor),
                contentAlignment = Alignment.Center
            ) {
                Text("🐾", fontSize = 32.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onButtonClick,
                    colors = ButtonDefaults.buttonColors(containerColor = accentBgColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = buttonText,
                        color = accentColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (secondaryButtonText != null && onSecondaryButtonClick != null) {
                    OutlinedButton(
                        onClick = onSecondaryButtonClick,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = secondaryButtonText,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PetSelectorRow(
    pets: List<Pet>,
    selectedPet: Pet?,
    currentTheme: PawsTheme,
    onPetSelect: (Pet) -> Unit,
    onAddPetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))
    val accentBgColor = Color(ContextCompat.getColor(context, currentTheme.accentBg))

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(pets) { pet ->
            val isSelected = pet.id == selectedPet?.id
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isSelected) accentBgColor else Color.White,
                shadowElevation = if (isSelected) 4.dp else 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onPetSelect(pet) }
                    .semantics { contentDescription = "Seleccionar mascota ${pet.name}. ${if (isSelected) "Mascota seleccionada actualmente" else ""}" }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(pet.getIconEmoji(), fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = pet.name,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) primaryColor else Color.DarkGray
                    )
                }
            }
        }
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onAddPetClick() }
                    .semantics { contentDescription = "Boton para registrar nueva mascota" }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text("+ Registrar", fontWeight = FontWeight.Bold, color = primaryColor)
                }
            }
        }
    }
}
