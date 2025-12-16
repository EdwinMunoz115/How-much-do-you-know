package com.example.howyouknow.data.repository

import android.content.Context
import com.example.howyouknow.data.local.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalPairingRepository(context: Context) {
    private val userRepository = LocalUserRepository(context)

    suspend fun connectWithCode(userId: String, code: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                // Buscar usuario por código de invitación
                val partnerResult = userRepository.getUserByInvitationCode(code)
                val partner = partnerResult.getOrNull()
                
                if (partner == null) {
                    return@withContext Result.failure(Exception("Código de invitación no válido"))
                }

                if (partner.userId == userId) {
                    return@withContext Result.failure(Exception("No puedes conectarte contigo mismo"))
                }

                if (partner.partnerId != null) {
                    return@withContext Result.failure(Exception("Este usuario ya tiene una pareja conectada"))
                }

                // Obtener usuario actual
                val currentUserResult = userRepository.getUser(userId)
                val currentUser = currentUserResult.getOrNull()
                
                if (currentUser == null) {
                    return@withContext Result.failure(Exception("Usuario no encontrado"))
                }

                if (currentUser.partnerId != null) {
                    return@withContext Result.failure(Exception("Ya tienes una pareja conectada"))
                }

                // Conectar parejas
                userRepository.connectPartners(userId, partner.userId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

