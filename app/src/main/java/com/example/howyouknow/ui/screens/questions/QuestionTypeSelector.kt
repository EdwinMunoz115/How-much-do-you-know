package com.example.howyouknow.ui.screens.questions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.howyouknow.data.model.QuestionType

@Composable
fun QuestionTypeSelector(
    selectedType: QuestionType,
    onTypeSelected: (QuestionType) -> Unit
) {
    Column {
        Text(
            text = "Tipo de pregunta",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuestionType.values().forEach { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { onTypeSelected(type) },
                    label = { Text(getTypeLabel(type)) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private fun getTypeLabel(type: QuestionType): String {
    return when (type) {
        QuestionType.MULTIPLE_CHOICE -> "Opción Múltiple"
        QuestionType.YES_NO -> "Sí/No"
        QuestionType.OPEN -> "Abierta"
        QuestionType.RANKING -> "Ranking"
        QuestionType.SURVEY -> "Encuesta"
    }
}

