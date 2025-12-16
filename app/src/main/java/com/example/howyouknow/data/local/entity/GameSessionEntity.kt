package com.example.howyouknow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.howyouknow.data.local.converter.Converters

@Entity(tableName = "game_sessions")
@TypeConverters(Converters::class)
data class GameSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val userId: String,
    val partnerId: String,
    val questionIds: List<String>,
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val comodinesUsed: Int = 0,
    val answersJson: String? = null, // JSON string de respuestas
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

