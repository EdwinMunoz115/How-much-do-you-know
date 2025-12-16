package com.example.howyouknow.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val userId: String,
    val name: String,
    val email: String,
    val age: Int,
    val partnerId: String? = null,
    val invitationCode: String,
    val totalPoints: Int = 0,
    val passwordHash: String, // Hash de la contraseña
    val createdAt: Long = System.currentTimeMillis()
)

