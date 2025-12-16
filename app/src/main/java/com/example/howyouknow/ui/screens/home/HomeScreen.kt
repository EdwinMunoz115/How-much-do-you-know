package com.example.howyouknow.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToGame: () -> Unit,
    onNavigateToPairing: () -> Unit,
    hasPartner: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "How You Know",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "¿Qué tanto conoces a tu pareja?",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        Button(
            onClick = onNavigateToProfile,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(bottom = 16.dp)
        ) {
            Text("Mi Perfil")
        }

        Button(
            onClick = onNavigateToGame,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = hasPartner
        ) {
            Text("Jugar")
        }

        if (!hasPartner) {
            Text(
                text = "Conecta con tu pareja para jugar",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
            TextButton(onClick = onNavigateToPairing) {
                Text("Conectar ahora")
            }
        }
    }
}

