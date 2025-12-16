package com.example.howyouknow.data.repository

import android.content.Context
import com.example.howyouknow.data.local.AppDatabase
import com.example.howyouknow.data.local.entity.UserEntity
import com.example.howyouknow.data.model.User
import java.util.UUID

class LocalUserRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()

    private fun createTimestamp(millis: Long): com.google.firebase.Timestamp {
        return try {
            val seconds = millis / 1000
            val nanos = ((millis % 1000) * 1000000).toInt()
            com.google.firebase.Timestamp(seconds, nanos)
        } catch (e: Exception) {
            // Si Firebase no está disponible, usar timestamp actual
            com.google.firebase.Timestamp.now()
        }
    }

    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val userEntity = userDao.getUserById(userId)
            val user = userEntity?.let {
                User(
                    userId = it.userId,
                    name = it.name,
                    email = it.email,
                    age = it.age,
                    partnerId = it.partnerId,
                    invitationCode = it.invitationCode,
                    totalPoints = it.totalPoints,
                    createdAt = createTimestamp(it.createdAt)
                )
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByInvitationCode(code: String): Result<User?> {
        return try {
            val userEntity = userDao.getUserByInvitationCode(code)
            val user = userEntity?.let {
                User(
                    userId = it.userId,
                    name = it.name,
                    email = it.email,
                    age = it.age,
                    partnerId = it.partnerId,
                    invitationCode = it.invitationCode,
                    totalPoints = it.totalPoints,
                    createdAt = createTimestamp(it.createdAt)
                )
            }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connectPartners(userId1: String, userId2: String): Result<Unit> {
        return try {
            userDao.updatePartnerId(userId1, userId2)
            userDao.updatePartnerId(userId2, userId1)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPoints(userId: String, points: Int): Result<Unit> {
        return try {
            userDao.addPoints(userId, points)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun ensureInvitationCode(userId: String): Result<String> {
        return try {
            val userEntity = userDao.getUserById(userId)
            if (userEntity != null && (userEntity.invitationCode.isBlank() || userEntity.invitationCode.isEmpty())) {
                // Generar nuevo código si no existe
                val newCode = java.util.UUID.randomUUID().toString().substring(0, 6).uppercase()
                userDao.updateInvitationCode(userId, newCode)
                Result.success(newCode)
            } else {
                Result.success(userEntity?.invitationCode ?: "")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

