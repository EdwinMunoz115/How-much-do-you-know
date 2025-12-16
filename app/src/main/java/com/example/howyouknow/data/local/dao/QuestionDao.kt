package com.example.howyouknow.data.local.dao

import androidx.room.*
import com.example.howyouknow.data.local.entity.QuestionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Query("SELECT * FROM questions WHERE questionId = :questionId")
    suspend fun getQuestionById(questionId: String): QuestionEntity?

    @Query("SELECT * FROM questions WHERE creatorId = :creatorId ORDER BY createdAt DESC")
    suspend fun getQuestionsByCreator(creatorId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE partnerId = :partnerId ORDER BY createdAt DESC")
    suspend fun getQuestionsForPartner(partnerId: String): List<QuestionEntity>

    @Query("SELECT * FROM questions WHERE questionId IN (:questionIds)")
    suspend fun getQuestionsByIds(questionIds: List<String>): List<QuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: QuestionEntity)

    @Update
    suspend fun updateQuestion(question: QuestionEntity)

    @Delete
    suspend fun deleteQuestion(question: QuestionEntity)
}

