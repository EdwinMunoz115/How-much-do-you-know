package com.example.howyouknow.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocalPairingRepository(context: Context) {
    private val userRepository = LocalUserRepository(context)

    suspend fun connectWithCode(userId: String, code: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val partnerResult = userRepository.getUserByInvitationCode(code)
                val partner = partnerResult.getOrNull()

                if (partner == null) {
                    return@withContext Result.failure(Exception("Codigo de invitacion no valido"))
                }

                if (partner.userId == userId) {
                    return@withContext Result.failure(Exception("No puedes conectarte contigo mismo"))
                }

                if (partner.partnerId != null) {
                    return@withContext Result.failure(Exception("Este usuario ya tiene otra conexion activa"))
                }

                val currentUserResult = userRepository.getUser(userId)
                val currentUser = currentUserResult.getOrNull()

                if (currentUser == null) {
                    return@withContext Result.failure(Exception("Usuario no encontrado"))
                }

                if (currentUser.partnerId != null) {
                    return@withContext Result.failure(Exception("Ya tienes una conexion activa"))
                }

                userRepository.connectPartners(userId, partner.userId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
