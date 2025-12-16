package com.example.howyouknow.ui.screens.questions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun YesNoAnswerSelector(
    onSelectAnswer: (String) -> Unit,
    selectedAnswer: String?
) {
    Column {
        Text(
            text = "Respuesta correcta",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedAnswer == "Sí",
                    onClick = { onSelectAnswer("Sí") }
                )
                Text("Sí", modifier = Modifier.padding(start = 8.dp))
            }

            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selectedAnswer == "No",
                    onClick = { onSelectAnswer("No") }
                )
                Text("No", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

