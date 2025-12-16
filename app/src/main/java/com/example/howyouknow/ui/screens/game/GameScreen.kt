package com.example.howyouknow.ui.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                    if (!uiState.gameMenuVisible && uiState.session != null) {
                        Text(
                            text = "Puntos: ${uiState.session?.score ?: 0}",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            // Menú inicial
            uiState.gameMenuVisible -> {
                GameMenuScreen(
                    onStartQuestions = { viewModel.startQuestionsGame() },
                    modifier = Modifier.padding(paddingValues)
                )
            }
            // Cargando
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            // Error
            uiState.errorMessage != null -> {
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
            }
            // Juego activo
            uiState.currentQuestion != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    // Timer y progreso
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentIndex = uiState.session?.currentQuestionIndex ?: 0
                        val totalQuestions = uiState.questions.size
                        Text(
                            text = "Pregunta ${currentIndex + 1} de $totalQuestions",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Timer
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.timeRemaining <= 10) {
                                    Color(0xFFFF5252) // Rojo cuando queda poco tiempo
                                } else {
                                    MaterialTheme.colorScheme.primaryContainer
                                }
                            )
                        ) {
                            Text(
                                text = "${uiState.timeRemaining}s",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                color = if (uiState.timeRemaining <= 10) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    
                    LinearProgressIndicator(
                        progress = { 
                            val currentIndex = uiState.session?.currentQuestionIndex ?: 0
                            val totalQuestions = uiState.questions.size
                            (currentIndex + 1).toFloat() / totalQuestions 
                        },
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

                    Spacer(modifier = Modifier.weight(1f))

                    // Botón enviar respuesta
                    Button(
                        onClick = { viewModel.submitAnswer() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = uiState.selectedAnswer != null
                    ) {
                        Text("Enviar Respuesta", style = MaterialTheme.typography.titleMedium)
                    }
                }
        }
    }
}

@Composable
private fun GameMenuScreen(
    onStartQuestions: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Elige una opción",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // Botón Preguntas (arriba, alargado)
        Button(
            onClick = onStartQuestions,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Preguntas",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }

        // Botón Casa (abajo, deshabilitado)
        Button(
            onClick = { /* Próximamente */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            enabled = false,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "Casa - Próximamente",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
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

