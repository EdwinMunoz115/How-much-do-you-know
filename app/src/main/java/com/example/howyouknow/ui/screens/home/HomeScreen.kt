package com.example.howyouknow.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToPairing: () -> Unit,
    onNavigateToCreateQuestion: () -> Unit,
    hasPartner: Boolean
) {
    val heroBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item { Spacer(modifier = Modifier.height(20.dp)) }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(28.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 10.dp
                ) {
                    Column(
                        modifier = Modifier
                            .background(heroBrush)
                            .padding(24.dp)
                    ) {
                        ChipLabel("NUEVO ENFOQUE")
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Retos visuales y minijuegos competitivos",
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Crea tops con imagenes, reta a otra persona y compite por dominar el ranking.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.88f)
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PrimaryAction(
                                title = "Crear reto top",
                                subtitle = "Comidas, musica, lugares y mas",
                                onClick = onNavigateToCreateQuestion,
                                modifier = Modifier.weight(1f)
                            )
                            PrimaryAction(
                                title = "Jugar ahora",
                                subtitle = if (hasPartner) "Entrar al duelo" else "Probar sin pareja",
                                onClick = onNavigateToGame,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item { SectionTitle("Modo principal") }

            item {
                FeatureCard(
                    title = "Top de imagenes",
                    body = "Arma un ranking secreto con fotos y haz que la otra persona lo ordene de mayor a menor gusto.",
                    accent = MaterialTheme.colorScheme.primary,
                    actionLabel = "Disenar reto",
                    onClick = onNavigateToCreateQuestion
                )
            }

            item { SectionTitle("Arcade") }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ArcadeCard(
                        title = "Color Clash",
                        description = "Combina bloques por color en un duelo rapido tipo match puzzle.",
                        status = "En preparacion"
                    )
                    ArcadeCard(
                        title = "Sky Rush",
                        description = "Una carrera tipo flappy con obstaculos y ritmo mas agresivo.",
                        status = "En preparacion"
                    )
                    ArcadeCard(
                        title = "Chess Duel",
                        description = "Partidas tacticas para quienes quieran un modo mas serio.",
                        status = "En preparacion"
                    )
                    ArcadeCard(
                        title = "Slider Arena",
                        description = "Desliza, crece y encierra al rival antes de que te absorba.",
                        status = "En preparacion"
                    )
                }
            }

            item { SectionTitle("Cuenta") }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FeatureCard(
                        title = "Mi perfil",
                        body = "Consulta tu progreso, puntos y estado de conexion.",
                        accent = MaterialTheme.colorScheme.secondary,
                        actionLabel = "Abrir perfil",
                        onClick = onNavigateToProfile,
                        modifier = Modifier.weight(1f)
                    )
                    FeatureCard(
                        title = if (hasPartner) "Rival conectado" else "Conectar jugador",
                        body = if (hasPartner) {
                            "Ya puedes lanzar retos y competir."
                        } else {
                            "Usa un codigo para jugar con otra persona."
                        },
                        accent = MaterialTheme.colorScheme.tertiary,
                        actionLabel = if (hasPartner) "Ir al duelo" else "Conectar",
                        onClick = if (hasPartner) onNavigateToGame else onNavigateToPairing,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ChipLabel(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.16f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun PrimaryAction(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f)
            )
        }
    }
}

@Composable
private fun FeatureCard(
    title: String,
    body: String,
    accent: Color,
    actionLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.12f))
                    .border(1.dp, accent.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = body, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = accent)
            ) {
                Text(actionLabel)
            }
        }
    }
}

@Composable
private fun ArcadeCard(
    title: String,
    description: String,
    status: String
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                            )
                        )
                    )
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
