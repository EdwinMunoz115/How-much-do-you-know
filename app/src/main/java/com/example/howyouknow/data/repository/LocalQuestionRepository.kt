package com.example.howyouknow.data.repository

import android.content.Context
import android.util.Log
import com.example.howyouknow.data.local.AppDatabase
import com.example.howyouknow.data.local.entity.QuestionEntity
import com.example.howyouknow.data.model.MediaItem
import com.example.howyouknow.data.model.Question
import com.example.howyouknow.data.model.QuestionType
import com.google.firebase.Timestamp
import com.google.gson.Gson
import java.util.UUID

class LocalQuestionRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val questionDao = db.questionDao()
    private val gson = Gson()

    suspend fun createQuestion(question: Question): Result<String> {
        return try {
            val questionId = UUID.randomUUID().toString()
            Log.d("LocalQuestionRepository", "Creando pregunta: creatorId=${question.creatorId}, partnerId=${question.partnerId}")
            val questionEntity = QuestionEntity(
                questionId = questionId,
                creatorId = question.creatorId,
                partnerId = question.partnerId,
                questionText = question.questionText,
                questionType = question.questionType.name,
                options = question.options,
                mediaItemsJson = if (question.mediaItems.isNotEmpty()) {
                    gson.toJson(question.mediaItems)
                } else null,
                correctAnswerJson = question.correctAnswer?.let { gson.toJson(it) }
            )
            questionDao.insertQuestion(questionEntity)
            Log.d("LocalQuestionRepository", "Pregunta guardada exitosamente con ID: $questionId")
            Result.success(questionId)
        } catch (e: Exception) {
            Log.e("LocalQuestionRepository", "Error al crear pregunta: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getQuestionsForPartner(partnerId: String): Result<List<Question>> {
        return try {
            Log.d("LocalQuestionRepository", "Buscando preguntas donde partnerId = $partnerId")
            val entities = questionDao.getQuestionsForPartner(partnerId)
            Log.d("LocalQuestionRepository", "Encontradas ${entities.size} preguntas en base de datos")
            val questions = entities.map { entityToQuestion(it) }
            Log.d("LocalQuestionRepository", "Convertidas ${questions.size} preguntas")
            Result.success(questions)
        } catch (e: Exception) {
            Log.e("LocalQuestionRepository", "Error al obtener preguntas: ${e.message}", e)
            Result.failure(e)
        }
    }

    suspend fun getQuestionsByIds(questionIds: List<String>): Result<List<Question>> {
        return try {
            if (questionIds.isEmpty()) {
                return Result.success(emptyList())
            }
            val entities = questionDao.getQuestionsByIds(questionIds)
            val questions = entities.map { entityToQuestion(it) }
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun entityToQuestion(entity: QuestionEntity): Question {
        val questionType = try {
            QuestionType.valueOf(entity.questionType)
        } catch (e: Exception) {
            QuestionType.MULTIPLE_CHOICE
        }

        val mediaItems = entity.mediaItemsJson?.let {
            try {
                gson.fromJson(it, Array<MediaItem>::class.java).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        val correctAnswer = entity.correctAnswerJson?.let {
            try {
                gson.fromJson(it, Any::class.java)
            } catch (e: Exception) {
                null
            }
        }

        return Question(
            questionId = entity.questionId,
            creatorId = entity.creatorId,
            partnerId = entity.partnerId,
            questionText = entity.questionText,
            questionType = questionType,
            options = entity.options,
            mediaItems = mediaItems,
            correctAnswer = correctAnswer,
            createdAt = Timestamp(entity.createdAt / 1000, ((entity.createdAt % 1000) * 1000000).toInt())
        )
    }
}

