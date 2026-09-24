package com.example.pawscare2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.pawscare2.PawsTheme

@Composable
fun StylesScreen(
    currentTheme: PawsTheme,
    onApplyTheme: (PawsTheme) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val primaryColor = Color(ContextCompat.getColor(context, currentTheme.primary))

    var selectedTheme by remember { mutableStateOf(currentTheme) }

    val themes = listOf(
        PawsTheme.OCEAN to Triple("Océano Azul 🌊", "Refrescante y profesional", "Primary: Azul • Accent: Rosa"),
        PawsTheme.FOREST to Triple("Bosque Verde 🌲", "Natural y relajante", "Primary: Verde • Accent: Amarillo"),
        PawsTheme.SUNSET to Triple("Atardecer Cereza 🌅", "Cálido y enérgico", "Primary: Rojo • Accent: Azul")
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA)),
        contentPadding = PaddingValues(20.dp)
    ) {
        item {
            Text(
                text = "Personalizar Apariencia",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Elige la paleta de colores de PawsCare que más te guste.",
                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        items(themes) { (theme, details) ->
            val isSelected = selectedTheme == theme
            val themePrimary = Color(ContextCompat.getColor(context, theme.primary))
            val themeAccent = Color(ContextCompat.getColor(context, theme.accent))
            val themeAccentBg = Color(ContextCompat.getColor(context, theme.accentBg))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .then(
                        if (isSelected) Modifier.border(2.dp, themePrimary, RoundedCornerShape(18.dp))
                        else Modifier
                    )
                    .clickable { selectedTheme = theme }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = details.first,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = themePrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = details.second,
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(themePrimary)
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(themeAccent)
                            )
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(themeAccentBg)
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { selectedTheme = theme },
                        colors = RadioButtonDefaults.colors(selectedColor = themePrimary)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { onApplyTheme(selectedTheme) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text("Aplicar Tema Seleccionado ✨", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
