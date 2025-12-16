package com.example.howyouknow.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class GameSession(
    val sessionId: String = "",
    val userId: String = "",
    val partnerId: String = "",
    val questionIds: List<String> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val comodinesUsed: Int = 0,
    val answers: List<Answer> = emptyList(),
    val completed: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): GameSession? {
            return try {
                val questionIdsList = document.get("questionIds") as? List<*> ?: emptyList<Any>()
                val questionIds = questionIdsList.mapNotNull { it as? String }

                val answersList = document.get("answers") as? List<*> ?: emptyList<Any>()
                val answers = answersList.mapNotNull { answerMap ->
                    if (answerMap is Map<*, *>) {
                        Answer(
                            questionId = answerMap["questionId"] as? String ?: "",
                            userAnswer = answerMap["userAnswer"],
                            isCorrect = answerMap["isCorrect"] as? Boolean ?: false,
                            pointsEarned = (answerMap["pointsEarned"] as? Long)?.toInt() ?: 0,
                            comodinUsed = answerMap["comodinUsed"] as? Boolean ?: false,
                            secondChanceUsed = answerMap["secondChanceUsed"] as? Boolean ?: false
                        )
                    } else null
                }

                GameSession(
                    sessionId = document.id,
                    userId = document.getString("userId") ?: "",
                    partnerId = document.getString("partnerId") ?: "",
                    questionIds = questionIds,
                    currentQuestionIndex = (document.getLong("currentQuestionIndex") ?: 0).toInt(),
                    score = (document.getLong("score") ?: 0).toInt(),
                    comodinesUsed = (document.getLong("comodinesUsed") ?: 0).toInt(),
                    answers = answers,
                    completed = document.getBoolean("completed") ?: false,
                    createdAt = document.getTimestamp("createdAt") ?: Timestamp.now()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "partnerId" to partnerId,
            "questionIds" to questionIds,
            "currentQuestionIndex" to currentQuestionIndex,
            "score" to score,
            "comodinesUsed" to comodinesUsed,
            "answers" to answers.map { answer ->
                mapOf(
                    "questionId" to answer.questionId,
                    "userAnswer" to (answer.userAnswer ?: ""),
                    "isCorrect" to answer.isCorrect,
                    "pointsEarned" to answer.pointsEarned,
                    "comodinUsed" to answer.comodinUsed,
                    "secondChanceUsed" to answer.secondChanceUsed
                )
            },
            "completed" to completed,
            "createdAt" to createdAt
        )
    }
}

