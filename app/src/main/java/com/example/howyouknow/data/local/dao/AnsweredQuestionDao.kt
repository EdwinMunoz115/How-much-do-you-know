package com.example.howyouknow.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.example.howyouknow.data.local.entity.AnsweredQuestionEntity

@Dao
interface AnsweredQuestionDao {
    @Query("SELECT questionId FROM answered_questions WHERE userId = :userId")
    suspend fun getAnsweredQuestionIds(userId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnsweredQuestion(answeredQuestion: AnsweredQuestionEntity)
}

