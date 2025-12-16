package com.example.howyouknow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.howyouknow.data.local.converter.Converters

@Entity(tableName = "questions")
@TypeConverters(Converters::class)
data class QuestionEntity(
    @PrimaryKey
    val questionId: String,
    val creatorId: String,
    val partnerId: String,
    val questionText: String,
    val questionType: String, // MULTIPLE_CHOICE, YES_NO, OPEN, RANKING, SURVEY
    val options: List<String>,
    val mediaItemsJson: String? = null, // JSON string de media items
    val correctAnswerJson: String? = null, // JSON string de la respuesta correcta
    val createdAt: Long = System.currentTimeMillis()
)

