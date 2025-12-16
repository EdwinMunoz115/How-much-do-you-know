package com.example.howyouknow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "answered_questions")
data class AnsweredQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val questionId: String,
    val answeredAt: Long = System.currentTimeMillis()
)

