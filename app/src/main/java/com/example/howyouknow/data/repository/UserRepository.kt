package com.example.howyouknow.data.repository

import com.example.howyouknow.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.UUID

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    suspend fun createUser(
        userId: String,
        name: String,
        email: String,
        age: Int
    ): Result<User> {
        return try {
            val invitationCode = generateInvitationCode()
            val user = User(
                userId = userId,
                name = name,
                email = email,
                age = age,
                invitationCode = invitationCode,
                totalPoints = 0,
                createdAt = Timestamp.now()
            )
            
            usersCollection.document(userId).set(user.toMap()).await()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUser(userId: String): Result<User?> {
        return try {
            val document = usersCollection.document(userId).get().await()
            val user = User.fromDocument(document)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserByInvitationCode(code: String): Result<User?> {
        return try {
            val query = usersCollection.whereEqualTo("invitationCode", code).get().await()
            val user = query.documents.firstOrNull()?.let { User.fromDocument(it) }
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            usersCollection.document(user.userId).update(user.toMap()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun connectPartners(userId1: String, userId2: String): Result<Unit> {
        return try {
            val batch = db.batch()
            batch.update(usersCollection.document(userId1), "partnerId", userId2)
            batch.update(usersCollection.document(userId2), "partnerId", userId1)
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun addPoints(userId: String, points: Int): Result<Unit> {
        return try {
            val userDoc = usersCollection.document(userId)
            db.runTransaction { transaction ->
                val snapshot = transaction.get(userDoc)
                val currentPoints = snapshot.getLong("totalPoints")?.toInt() ?: 0
                transaction.update(userDoc, "totalPoints", currentPoints + points)
            }.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateInvitationCode(): String {
        return UUID.randomUUID().toString().take(8).uppercase()
    }
}

