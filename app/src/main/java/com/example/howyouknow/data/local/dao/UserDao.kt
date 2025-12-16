package com.example.howyouknow.data.local.dao

import androidx.room.*
import com.example.howyouknow.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE invitationCode = :code")
    suspend fun getUserByInvitationCode(code: String): UserEntity?

    @Query("SELECT * FROM users WHERE userId = :userId")
    fun getUserByIdFlow(userId: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET totalPoints = totalPoints + :points WHERE userId = :userId")
    suspend fun addPoints(userId: String, points: Int)

    @Query("UPDATE users SET partnerId = :partnerId WHERE userId = :userId")
    suspend fun updatePartnerId(userId: String, partnerId: String?)
}

