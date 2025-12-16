package com.example.howyouknow.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

data class User(
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val age: Int = 0,
    val partnerId: String? = null,
    val invitationCode: String = "",
    val totalPoints: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): User? {
            return try {
                User(
                    userId = document.id,
                    name = document.getString("name") ?: "",
                    email = document.getString("email") ?: "",
                    age = document.getLong("age")?.toInt() ?: 0,
                    partnerId = document.getString("partnerId"),
                    invitationCode = document.getString("invitationCode") ?: "",
                    totalPoints = document.getLong("totalPoints")?.toInt() ?: 0,
                    createdAt = document.getTimestamp("createdAt") ?: Timestamp.now()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "name" to name,
            "email" to email,
            "age" to age,
            "partnerId" to (partnerId ?: ""),
            "invitationCode" to invitationCode,
            "totalPoints" to totalPoints,
            "createdAt" to createdAt
        )
    }
}

