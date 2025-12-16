package com.example.howyouknow.ui.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.howyouknow.data.model.QuestionType
import com.example.howyouknow.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameComplete: (String) -> Unit, // sessionId
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startGame()
    }

    LaunchedEffect(uiState.gameCompleted) {
        if (uiState.gameCompleted && uiState.session != null) {
            onGameComplete(uiState.session!!.sessionId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Juego") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                },
                actions = {
                    Text(
                        text = "Puntos: ${uiState.session?.score ?: 0}",
                        modifier = Modifier.padding(16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.errorMessage!!,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onNavigateBack) {
                    Text("Volver")
                }
            }
        } else if (uiState.currentQuestion != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Progreso
                val currentIndex = uiState.session?.currentQuestionIndex ?: 0
                val totalQuestions = uiState.questions.size
                Text(
                    text = "Pregunta ${currentIndex + 1} de $totalQuestions",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                LinearProgressIndicator(
                    progress = { (currentIndex + 1).toFloat() / totalQuestions },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .padding(bottom = 24.dp)
                )

                // Pregunta
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = uiState.currentQuestion!!.questionText,
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Mostrar opciones según el tipo
                        when (uiState.currentQuestion!!.questionType) {
                            QuestionType.MULTIPLE_CHOICE,
                            QuestionType.YES_NO -> {
                                uiState.currentQuestion!!.options.forEach { option ->
                                    AnswerOption(
                                        text = option,
                                        isSelected = uiState.selectedAnswer == option,
                                        onClick = { viewModel.selectAnswer(option) }
                                    )
                                }
                            }
                            QuestionType.OPEN -> {
                                OpenAnswerInput(
                                    onAnswerChange = { viewModel.selectAnswer(it) }
                                )
                            }
                            QuestionType.RANKING,
                            QuestionType.SURVEY -> {
                                // Para ranking, mostrar opciones ordenables
                                Text(
                                    text = "Funcionalidad de ranking próximamente",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }

                // Comodines
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val comodinesUsed = uiState.session?.comodinesUsed ?: 0
                    val comodinesAvailable = 3 - comodinesUsed
                    if (comodinesAvailable > 0 && uiState.currentQuestion!!.options.size > 3) {
                        OutlinedButton(
                            onClick = { viewModel.useComodin() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Comodín (${comodinesAvailable})")
                        }
                    } else if (comodinesAvailable <= 0) {
                        Text(
                            text = "Sin comodines disponibles",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Segunda oportunidad
                if (uiState.showSecondChance) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Respuesta incorrecta",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Puedes intentar de nuevo por -5 puntos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                            Button(
                                onClick = { viewModel.useSecondChance() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                            ) {
                                Text("Intentar de nuevo")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Botón enviar respuesta
                Button(
                    onClick = { viewModel.submitAnswer() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = uiState.selectedAnswer != null && !uiState.showSecondChance
                ) {
                    Text("Enviar Respuesta")
                }
            }
        }
    }
}

@Composable
private fun AnswerOption(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Text(
                text = text,
                modifier = Modifier.padding(start = 8.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun OpenAnswerInput(
    onAnswerChange: (String) -> Unit
) {
    var answer by remember { mutableStateOf("") }

    OutlinedTextField(
        value = answer,
        onValueChange = {
            answer = it
            onAnswerChange(it)
        },
        label = { Text("Tu respuesta") },
        modifier = Modifier.fillMaxWidth(),
        minLines = 3,
        maxLines = 5
    )
}

