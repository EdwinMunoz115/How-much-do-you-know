package com.example.howyouknow.data.model

data class Answer(
    val questionId: String = "",
    val userAnswer: Any? = null, // Puede ser String, Int, List<String>, etc.
    val isCorrect: Boolean = false,
    val pointsEarned: Int = 0,
    val comodinUsed: Boolean = false,
    val secondChanceUsed: Boolean = false
)

