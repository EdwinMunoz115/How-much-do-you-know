package com.example.howyouknow.data.local.dao

import androidx.room.*
import com.example.howyouknow.data.local.entity.GameSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {
    @Query("SELECT * FROM game_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): GameSessionEntity?

    @Query("SELECT * FROM game_sessions WHERE userId = :userId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestSessionByUser(userId: String): GameSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity)

    @Update
    suspend fun updateSession(session: GameSessionEntity)

    @Query("UPDATE game_sessions SET completed = 1 WHERE sessionId = :sessionId")
    suspend fun completeSession(sessionId: String)
}

