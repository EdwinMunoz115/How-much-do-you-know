package com.example.howyouknow.data.repository

import com.example.howyouknow.data.model.Question
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class QuestionRepository {
    private val db = FirebaseFirestore.getInstance()
    private val questionsCollection = db.collection("questions")

    suspend fun createQuestion(question: Question): Result<String> {
        return try {
            val docRef = questionsCollection.add(question.toMap()).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuestion(questionId: String): Result<Question?> {
        return try {
            val document = questionsCollection.document(questionId).get().await()
            val question = Question.fromDocument(document)
            Result.success(question)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuestionsByCreator(creatorId: String): Result<List<Question>> {
        return try {
            val query = questionsCollection
                .whereEqualTo("creatorId", creatorId)
                .get()
                .await()
            val questions = query.documents.mapNotNull { Question.fromDocument(it) }
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuestionsForPartner(partnerId: String): Result<List<Question>> {
        return try {
            val query = questionsCollection
                .whereEqualTo("partnerId", partnerId)
                .get()
                .await()
            val questions = query.documents.mapNotNull { Question.fromDocument(it) }
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateQuestion(question: Question): Result<Unit> {
        return try {
            questionsCollection.document(question.questionId).update(question.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteQuestion(questionId: String): Result<Unit> {
        return try {
            questionsCollection.document(questionId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getQuestionsByIds(questionIds: List<String>): Result<List<Question>> {
        return try {
            if (questionIds.isEmpty()) {
                return Result.success(emptyList())
            }
            
            // Firestore limita las consultas "in" a 10 elementos
            val questions = mutableListOf<Question>()
            questionIds.chunked(10).forEach { chunk ->
                val query = questionsCollection
                    .whereIn("__name__", chunk)
                    .get()
                    .await()
                questions.addAll(query.documents.mapNotNull { Question.fromDocument(it) })
            }
            Result.success(questions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

