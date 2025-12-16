package com.example.howyouknow.ui.screens.questions

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionsEditor(
    options: List<String>,
    onOptionChange: (Int, String) -> Unit,
    onAddOption: () -> Unit,
    onRemoveOption: (Int) -> Unit,
    onSelectCorrectAnswer: (Any?) -> Unit,
    selectedAnswer: Any?
) {
    Column {
        Text(
            text = "Opciones",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        options.forEachIndexed { index, option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = option,
                    onValueChange = { onOptionChange(index, it) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Opción ${index + 1}") },
                    singleLine = true
                )

                IconButton(
                    onClick = { onSelectCorrectAnswer(option) },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    RadioButton(
                        selected = selectedAnswer == option,
                        onClick = { onSelectCorrectAnswer(option) }
                    )
                }

                if (options.size > 2) {
                    IconButton(onClick = { onRemoveOption(index) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar"
                        )
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onAddOption,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar opción"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar opción")
        }

        if (selectedAnswer != null) {
            Text(
                text = "Respuesta correcta: $selectedAnswer",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

