package com.example.howyouknow.ui.screens.questions

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.howyouknow.data.model.QuestionType
import com.example.howyouknow.ui.viewmodel.QuestionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateQuestionScreen(
    viewModel: QuestionViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val emptyIndex = uiState.mediaItems.indexOfFirst { item -> item.uri.isBlank() }
            if (emptyIndex >= 0) {
                viewModel.updateMediaUri(emptyIndex, it.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Creador de retos") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeroPanel()

            QuestionTypeSelector(
                selectedType = uiState.questionType,
                onTypeSelected = { viewModel.updateQuestionType(it) }
            )

            OutlinedTextField(
                value = uiState.questionText,
                onValueChange = { viewModel.updateQuestionText(it) },
                label = { Text("Titulo del reto") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                placeholder = { Text("Ej. Ordena las comidas que mas me gustan") }
            )

            when (uiState.questionType) {
                QuestionType.RANKING -> {
                    RankingBuilder(
                        options = uiState.options,
                        mediaUris = uiState.mediaItems.map { it.uri },
                        onLabelChange = { index, value -> viewModel.updateOption(index, value) },
                        onMediaChange = { index, value -> viewModel.updateMediaUri(index, value) },
                        onAddItem = { viewModel.addOption() },
                        onRemoveItem = { index -> viewModel.removeOption(index) },
                        onMoveUp = { index -> viewModel.moveRankingItem(index, (index - 1).coerceAtLeast(0)) },
                        onMoveDown = { index ->
                            val lastIndex = uiState.options.lastIndex
                            viewModel.moveRankingItem(index, (index + 1).coerceAtMost(lastIndex))
                        },
                        onPickImage = { imagePicker.launch("image/*") }
                    )
                }

                QuestionType.MULTIPLE_CHOICE, QuestionType.SURVEY -> {
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
                    Text(
                        text = "Usa este modo para retos de texto libre o respuestas cortas.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (uiState.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            if (uiState.successMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Text(
                        text = uiState.successMessage ?: "",
                        modifier = Modifier.padding(14.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Button(
                onClick = { viewModel.saveQuestion() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Guardar reto")
            }
        }
    }
}

@Composable
private fun HeroPanel() {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.secondary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "Construye el reto principal",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Puedes pegar URLs de imagenes o cargar fotos desde tu dispositivo para crear un top visual.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun RankingBuilder(
    options: List<String>,
    mediaUris: List<String>,
    onLabelChange: (Int, String) -> Unit,
    onMediaChange: (Int, String) -> Unit,
    onAddItem: () -> Unit,
    onRemoveItem: (Int) -> Unit,
    onMoveUp: (Int) -> Unit,
    onMoveDown: (Int) -> Unit,
    onPickImage: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = "Top visual",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "El orden en esta lista sera la respuesta correcta: arriba significa mas gusto.",
            style = MaterialTheme.typography.bodyMedium
        )

        options.forEachIndexed { index, option ->
            RankingItemCard(
                index = index,
                label = option,
                imageUri = mediaUris.getOrElse(index) { "" },
                onLabelChange = { onLabelChange(index, it) },
                onImageUriChange = { onMediaChange(index, it) },
                onRemove = { onRemoveItem(index) },
                onMoveUp = { onMoveUp(index) },
                onMoveDown = { onMoveDown(index) },
                onPickImage = onPickImage,
                canRemove = options.size > 2
            )
        }

        Button(
            onClick = onAddItem,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Agregar posicion al top")
        }
    }
}

@Composable
private fun RankingItemCard(
    index: Int,
    label: String,
    imageUri: String,
    onLabelChange: (String) -> Unit,
    onImageUriChange: (String) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onPickImage: () -> Unit,
    canRemove: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#${index + 1} en el ranking real",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MiniAction("Subir", onMoveUp)
                    MiniAction("Bajar", onMoveDown)
                    if (canRemove) {
                        MiniAction("Quitar", onRemove)
                    }
                }
            }

            OutlinedTextField(
                value = label,
                onValueChange = onLabelChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nombre de la opcion") },
                placeholder = { Text("Ej. Sushi, pizza, hamburguesa") }
            )

            OutlinedTextField(
                value = imageUri,
                onValueChange = onImageUriChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("URL o URI de la imagen") },
                placeholder = { Text("https://... o content://...") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(onClick = onPickImage) {
                    Text("Cargar imagen")
                }
                Text(
                    text = "Tambien puedes pegar una URL de Google u otra fuente.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            if (imageUri.isNotBlank()) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = label,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Sin imagen aun")
                }
            }
        }
    }
}

@Composable
private fun MiniAction(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.clip(RoundedCornerShape(999.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        onClick = onClick
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
