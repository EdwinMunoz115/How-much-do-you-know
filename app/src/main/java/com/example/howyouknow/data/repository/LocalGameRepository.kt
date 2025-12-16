package com.example.howyouknow.data.repository

import android.content.Context
import com.example.howyouknow.data.local.AppDatabase
import com.example.howyouknow.data.local.entity.GameSessionEntity
import com.example.howyouknow.data.model.Answer
import com.example.howyouknow.data.model.GameSession
import com.google.firebase.Timestamp
import com.google.gson.Gson
import java.util.UUID

class LocalGameRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val gameSessionDao = db.gameSessionDao()
    private val gson = Gson()

    suspend fun createGameSession(
        userId: String,
        partnerId: String,
        questionIds: List<String>
    ): Result<String> {
        return try {
            val sessionId = UUID.randomUUID().toString()
            val session = GameSessionEntity(
                sessionId = sessionId,
                userId = userId,
                partnerId = partnerId,
                questionIds = questionIds,
                currentQuestionIndex = 0,
                score = 0,
                comodinesUsed = 0,
                answersJson = null,
                completed = false
            )
            gameSessionDao.insertSession(session)
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameSession(sessionId: String): Result<GameSession?> {
        return try {
            val entity = gameSessionDao.getSessionById(sessionId)
            val session = entity?.let { entityToSession(it) }
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGameSession(session: GameSession): Result<Unit> {
        return try {
            val entity = sessionToEntity(session)
            gameSessionDao.updateSession(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addAnswer(sessionId: String, answer: Answer): Result<Unit> {
        return try {
            val sessionResult = getGameSession(sessionId)
            val session = sessionResult.getOrNull() ?: return Result.failure(Exception("Sesión no encontrada"))
            
            val updatedAnswers = session.answers.toMutableList()
            updatedAnswers.add(answer)
            
            val updatedSession = session.copy(
                answers = updatedAnswers,
                score = session.score + answer.pointsEarned,
                currentQuestionIndex = session.currentQuestionIndex + 1,
                comodinesUsed = if (answer.comodinUsed) session.comodinesUsed + 1 else session.comodinesUsed
            )
            
            updateGameSession(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun completeGameSession(sessionId: String): Result<Unit> {
        return try {
            gameSessionDao.completeSession(sessionId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun entityToSession(entity: GameSessionEntity): GameSession {
        val answers = entity.answersJson?.let {
            try {
                gson.fromJson(it, Array<Answer>::class.java).toList()
            } catch (e: Exception) {
                emptyList()
            }
        } ?: emptyList()

        return GameSession(
            sessionId = entity.sessionId,
            userId = entity.userId,
            partnerId = entity.partnerId,
            questionIds = entity.questionIds,
            currentQuestionIndex = entity.currentQuestionIndex,
            score = entity.score,
            comodinesUsed = entity.comodinesUsed,
            answers = answers,
            completed = entity.completed,
            createdAt = Timestamp(entity.createdAt / 1000, ((entity.createdAt % 1000) * 1000000).toInt())
        )
    }

    private fun sessionToEntity(session: GameSession): GameSessionEntity {
        return GameSessionEntity(
            sessionId = session.sessionId,
            userId = session.userId,
            partnerId = session.partnerId,
            questionIds = session.questionIds,
            currentQuestionIndex = session.currentQuestionIndex,
            score = session.score,
            comodinesUsed = session.comodinesUsed,
            answersJson = if (session.answers.isNotEmpty()) {
                gson.toJson(session.answers)
            } else null,
            completed = session.completed,
            createdAt = session.createdAt.seconds * 1000 + session.createdAt.nanoseconds / 1000000
        )
    }
}

