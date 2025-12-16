package com.example.howyouknow.data.repository

import com.example.howyouknow.data.model.Answer
import com.example.howyouknow.data.model.GameSession
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class GameRepository {
    private val db = FirebaseFirestore.getInstance()
    private val sessionsCollection = db.collection("gameSessions")

    suspend fun createGameSession(
        userId: String,
        partnerId: String,
        questionIds: List<String>
    ): Result<String> {
        return try {
            val session = GameSession(
                userId = userId,
                partnerId = partnerId,
                questionIds = questionIds,
                currentQuestionIndex = 0,
                score = 0,
                comodinesUsed = 0,
                answers = emptyList(),
                completed = false,
                createdAt = Timestamp.now()
            )
            val docRef = sessionsCollection.add(session.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameSession(sessionId: String): Result<GameSession?> {
        return try {
            val document = sessionsCollection.document(sessionId).get().await()
            val session = GameSession.fromDocument(document)
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGameSession(session: GameSession): Result<Unit> {
        return try {
            sessionsCollection.document(session.sessionId).update(session.toMap()).await()
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
            val sessionResult = getGameSession(sessionId)
            val session = sessionResult.getOrNull() ?: return Result.failure(Exception("Sesión no encontrada"))
            
            val updatedSession = session.copy(completed = true)
            updateGameSession(updatedSession)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

