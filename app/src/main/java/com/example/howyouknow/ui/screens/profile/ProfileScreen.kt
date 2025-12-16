package com.example.howyouknow.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import com.example.howyouknow.data.repository.LocalUserRepository
import com.example.howyouknow.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onNavigateToPairing: () -> Unit,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel
) {
    val context = LocalContext.current
    val userRepository = remember { LocalUserRepository(context) }
    var user by remember { mutableStateOf<com.example.howyouknow.data.model.User?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val authUser = authViewModel.uiState.value.user

    LaunchedEffect(userId) {
        if (authUser != null) {
            scope.launch {
                val result = userRepository.getUser(userId)
                result.fold(
                    onSuccess = {
                        user = it
                        isLoading = false
                    },
                    onFailure = {
                        // Si falla, usar datos del authViewModel
                        user = com.example.howyouknow.data.model.User(
                            userId = authUser.userId,
                            name = authUser.name,
                            email = authUser.email,
                            age = authUser.age,
                            partnerId = authUser.partnerId,
                            invitationCode = authUser.invitationCode,
                            totalPoints = authUser.totalPoints,
                            createdAt = com.google.firebase.Timestamp.now()
                        )
                        isLoading = false
                    }
                )
            }
        } else {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar/Icon
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Usuario",
                    modifier = Modifier.size(80.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Información del usuario
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        InfoRow("Nombre", user?.name ?: "")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Correo", user?.email ?: "")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Edad", "${user?.age ?: 0} años")
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        InfoRow("Puntos totales", "${user?.totalPoints ?: 0}")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Estado de pareja
                if (user?.partnerId == null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No tienes pareja conectada",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(onClick = onNavigateToPairing) {
                                Text("Conectar con pareja")
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✓ Pareja conectada",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        onLogout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Cerrar sesión")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

