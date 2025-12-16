package com.example.howyouknow.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Título principal
        Text(
            text = "How much do you know",
            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 8.dp),
            color = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "¿Qué tanto conoces a tu pareja?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 48.dp),
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )

        // Botón Mi Perfil
        Button(
            onClick = onNavigateToProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("Mi Perfil", style = MaterialTheme.typography.titleMedium)
        }

        // Botón Crear Preguntas (solo si tiene pareja)
        if (hasPartner) {
            Button(
                onClick = onNavigateToCreateQuestion,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Crear Preguntas", style = MaterialTheme.typography.titleMedium)
            }
        }

        // Botón Jugar
        Button(
            onClick = onNavigateToGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = if (hasPartner) 16.dp else 0.dp),
            enabled = hasPartner,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (hasPartner) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text("Jugar", style = MaterialTheme.typography.titleMedium)
        }

        // Botón corazón para conectar pareja
        if (!hasPartner) {
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Conecta con tu pareja para jugar",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    FloatingActionButton(
                        onClick = onNavigateToPairing,
                        modifier = Modifier.size(64.dp),
                        containerColor = Color(0xFFFF6B9D), // Rosa/rojo para temática de amor
                        contentColor = Color.White
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Conectar con pareja",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}

