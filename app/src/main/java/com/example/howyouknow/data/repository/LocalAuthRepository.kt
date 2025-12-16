package com.example.howyouknow.data.repository

import android.content.Context
import android.util.Log
import com.example.howyouknow.data.local.AppDatabase
import com.example.howyouknow.data.local.entity.UserEntity
import com.example.howyouknow.util.PasswordHasher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocalUser(
    val userId: String,
    val name: String,
    val email: String,
    val age: Int,
    val partnerId: String? = null,
    val invitationCode: String,
    val totalPoints: Int = 0
)

class LocalAuthRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userDao = db.userDao()

    private val _currentUser = MutableStateFlow<LocalUser?>(null)
    val currentUser: StateFlow<LocalUser?> = _currentUser.asStateFlow()

    suspend fun register(email: String, password: String, name: String, age: Int): Result<LocalUser> {
        return try {
            // Verificar si el email ya existe
            val existingUser = userDao.getUserByEmail(email)
            if (existingUser != null) {
                return Result.failure(Exception("El correo electrónico ya está registrado"))
            }

            val userId = java.util.UUID.randomUUID().toString()
            val invitationCode = generateInvitationCode()
            val passwordHash = PasswordHasher.hash(password)

            val userEntity = UserEntity(
                userId = userId,
                name = name,
                email = email,
                age = age,
                invitationCode = invitationCode,
                passwordHash = passwordHash,
                totalPoints = 0
            )

            userDao.insertUser(userEntity)
            Log.d("LocalAuthRepository", "Usuario guardado en base de datos: $userId, email: $email")

            val user = LocalUser(
                userId = userId,
                name = name,
                email = email,
                age = age,
                invitationCode = invitationCode,
                totalPoints = 0
            )

            _currentUser.value = user
            Log.d("LocalAuthRepository", "Usuario registrado exitosamente: ${user.name}")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(email: String, password: String): Result<LocalUser> {
        return try {
            Log.d("LocalAuthRepository", "Intentando login para email: $email")
            val userEntity = userDao.getUserByEmail(email)
            if (userEntity == null) {
                Log.w("LocalAuthRepository", "Usuario no encontrado para email: $email")
                return Result.failure(Exception("Correo electrónico o contraseña incorrectos"))
            }

            Log.d("LocalAuthRepository", "Usuario encontrado: ${userEntity.name}, verificando contraseña...")
            if (!PasswordHasher.verify(password, userEntity.passwordHash)) {
                Log.w("LocalAuthRepository", "Contraseña incorrecta para email: $email")
                return Result.failure(Exception("Correo electrónico o contraseña incorrectos"))
            }

            val user = LocalUser(
                userId = userEntity.userId,
                name = userEntity.name,
                email = userEntity.email,
                age = userEntity.age,
                partnerId = userEntity.partnerId,
                invitationCode = userEntity.invitationCode,
                totalPoints = userEntity.totalPoints
            )

            _currentUser.value = user
            Log.d("LocalAuthRepository", "Login exitoso: ${user.name}, userId: ${user.userId}, partnerId: ${user.partnerId}")
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        val previousUser = _currentUser.value?.name
        _currentUser.value = null
        Log.d("LocalAuthRepository", "Logout realizado para: $previousUser")
    }

    fun getCurrentUser(): LocalUser? {
        return _currentUser.value
    }

    suspend fun loadCurrentUser(userId: String) {
        val userEntity = userDao.getUserById(userId)
        if (userEntity != null) {
            _currentUser.value = LocalUser(
                userId = userEntity.userId,
                name = userEntity.name,
                email = userEntity.email,
                age = userEntity.age,
                partnerId = userEntity.partnerId,
                invitationCode = userEntity.invitationCode,
                totalPoints = userEntity.totalPoints
            )
        }
    }

    private fun generateInvitationCode(): String {
        return java.util.UUID.randomUUID().toString().substring(0, 6).uppercase()
    }
}

