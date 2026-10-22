package com.example.howyouknow.ui.screens.game

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.howyouknow.data.model.QuestionType
import com.example.howyouknow.ui.viewmodel.GameViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onGameComplete: (String) -> Unit,
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
                title = { Text(if (uiState.isPractice) "Partida de prueba" else "Arena de retos") },
                navigationIcon = {
                    Surface(
                        modifier = Modifier.padding(start = 8.dp),
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        onClick = onNavigateBack
                    ) {
                        Text("<", modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp))
                    }
                },
                actions = {
                    if (!uiState.gameMenuVisible && uiState.session != null) {
                        Text(
                            text = "${uiState.session?.score ?: 0} pts",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.gameMenuVisible -> {
                GameMenuScreen(
                    onStartQuestions = { viewModel.startQuestionsGame() },
                    onStartPractice = { viewModel.startQuestionsGame(practice = true) },
                    modifier = Modifier.padding(paddingValues)
                )
            }

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
                        text = uiState.errorMessage ?: "",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Volver")
                    }
                }
            }

            uiState.currentQuestion != null -> {
                val question = uiState.currentQuestion!!

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
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
                            text = "Reto ${currentIndex + 1} de $totalQuestions",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = if (uiState.timeRemaining <= 10) {
                                MaterialTheme.colorScheme.tertiary
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ) {
                            Text(
                                text = "${uiState.timeRemaining}s",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                color = if (uiState.timeRemaining <= 10) {
                                    MaterialTheme.colorScheme.onTertiary
                                } else {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                }
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = {
                            val currentIndex = uiState.session?.currentQuestionIndex ?: 0
                            val totalQuestions = uiState.questions.size.coerceAtLeast(1)
                            (currentIndex + 1).toFloat() / totalQuestions
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(bottom = 24.dp)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = question.questionText,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = when (question.questionType) {
                                    QuestionType.RANKING -> "Ordena las opciones de mayor a menor gusto."
                                    else -> "Responde antes de que se acabe el tiempo."
                                },
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(18.dp))

                            when (question.questionType) {
                                QuestionType.MULTIPLE_CHOICE,
                                QuestionType.YES_NO -> {
                                    question.options.forEach { option ->
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

                                QuestionType.RANKING -> {
                                    RankingAnswerEditor(
                                        labels = (uiState.selectedAnswer as? List<*>)?.mapNotNull { it?.toString() }
                                            ?: question.options,
                                        mediaByLabel = question.options.mapIndexed { index, option ->
                                            option to question.mediaItems.getOrNull(index)?.uri.orEmpty()
                                        }.toMap(),
                                        onMoveUp = { index ->
                                            viewModel.moveRankingAnswer(index, (index - 1).coerceAtLeast(0))
                                        },
                                        onMoveDown = { index ->
                                            val lastIndex = question.options.lastIndex
                                            viewModel.moveRankingAnswer(index, (index + 1).coerceAtMost(lastIndex))
                                        }
                                    )
                                }

                                QuestionType.SURVEY -> {
                                    Text(
                                        text = "Modo encuesta en preparacion.",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = { viewModel.submitAnswer() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = uiState.selectedAnswer != null
                    ) {
                        Text("Enviar respuesta", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameMenuScreen(
    onStartQuestions: () -> Unit,
    onStartPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Elige tu arena",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Top de imagenes ya esta jugable. Los demas minijuegos quedan listos en esta misma seccion.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
                )
            }
        }

        ArenaCard(
            title = "Partida de prueba",
            description = "Retos de ejemplo sin pareja. Puedes repetirlos sin sumar puntos a tu perfil.",
            cta = "Probar",
            enabled = true,
            onClick = onStartPractice
        )
        ArenaCard(
            title = "Top de imagenes",
            description = "Acomoda fotos segun el ranking real de la otra persona.",
            cta = "Entrar al reto",
            enabled = true,
            onClick = onStartQuestions
        )
        ArenaCard(
            title = "Color Clash",
            description = "Duelo por colores estilo puzzle competitivo.",
            cta = "Proximamente",
            enabled = false,
            onClick = {}
        )
        ArenaCard(
            title = "Sky Rush",
            description = "Vuelo frenetico con obstaculos y precision.",
            cta = "Proximamente",
            enabled = false,
            onClick = {}
        )
        ArenaCard(
            title = "Chess Duel",
            description = "Ajedrez para partidas mas tacticas.",
            cta = "Proximamente",
            enabled = false,
            onClick = {}
        )
        ArenaCard(
            title = "Slider Arena",
            description = "Come al rival antes de que el te encierre.",
            cta = "Proximamente",
            enabled = false,
            onClick = {}
        )
    }
}

@Composable
private fun ArenaCard(
    title: String,
    description: String,
    cta: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = description, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(modifier = Modifier.size(12.dp))
            Button(onClick = onClick, enabled = enabled) {
                Text(cta)
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
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
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

@Composable
private fun RankingAnswerEditor(
    labels: List<String>,
    mediaByLabel: Map<String, String>,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        labels.forEachIndexed { index, label ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val imageUri = mediaByLabel[label].orEmpty()
                    if (imageUri.isNotBlank()) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = label,
                            modifier = Modifier
                                .size(88.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MiniControl("Subir") { onMoveUp(index) }
                        MiniControl("Bajar") { onMoveDown(index) }
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniControl(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
