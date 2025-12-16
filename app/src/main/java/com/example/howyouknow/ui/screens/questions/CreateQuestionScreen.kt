package com.example.howyouknow.ui.screens.questions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.howyouknow.data.model.QuestionType
import com.example.howyouknow.ui.viewmodel.QuestionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuestionScreen(
    viewModel: QuestionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            // Opcional: navegar de vuelta después de un delay
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear Pregunta") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Selector de tipo de pregunta
            QuestionTypeSelector(
                selectedType = uiState.questionType,
                onTypeSelected = { viewModel.updateQuestionType(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Texto de la pregunta
            OutlinedTextField(
                value = uiState.questionText,
                onValueChange = { viewModel.updateQuestionText(it) },
                label = { Text("Escribe tu pregunta") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 5
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Opciones según el tipo
            when (uiState.questionType) {
                QuestionType.MULTIPLE_CHOICE, QuestionType.RANKING, QuestionType.SURVEY -> {
                    OptionsEditor(
                        options = uiState.options,
                        onOptionChange = { index, value -> viewModel.updateOption(index, value) },
                        onAddOption = { viewModel.addOption() },
                        onRemoveOption = { index -> viewModel.removeOption(index) },
                        onSelectCorrectAnswer = { answer -> viewModel.setCorrectAnswer(answer) },
                        selectedAnswer = uiState.correctAnswer
                    )
                }
                QuestionType.YES_NO -> {
                    YesNoAnswerSelector(
                        onSelectAnswer = { answer -> viewModel.setCorrectAnswer(answer) },
                        selectedAnswer = uiState.correctAnswer as? String
                    )
                }
                QuestionType.OPEN -> {
                    // Para preguntas abiertas, no hay opciones
                    Text(
                        text = "Pregunta abierta - La respuesta será texto libre",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sección de multimedia (placeholder por ahora)
            Text(
                text = "Multimedia",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Funcionalidad de multimedia próximamente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mensajes de error/éxito
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (uiState.successMessage != null) {
                Text(
                    text = uiState.successMessage!!,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Botón guardar
            Button(
                onClick = { viewModel.saveQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading && uiState.questionText.isNotBlank()
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar Pregunta")
                }
            }
        }
    }
}

