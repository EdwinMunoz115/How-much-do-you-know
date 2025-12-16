package com.example.howyouknow.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth: FirebaseAuth? = try {
        FirebaseAuth.getInstance()
    } catch (e: Exception) {
        Log.e("AuthRepository", "Error al obtener instancia de FirebaseAuth: ${e.message}")
        null
    }

    val currentUser: FirebaseUser?
        get() = try {
            auth?.currentUser
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al obtener usuario actual: ${e.message}")
            null
        }

    suspend fun register(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (auth == null) {
                return Result.failure(Exception("Firebase no está configurado correctamente. Por favor, agrega el archivo google-services.json real desde Firebase Console."))
            }
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al registrar usuario: ${e.message}")
            // Mensaje más amigable para errores comunes
            val errorMessage = when {
                e.message?.contains("API key not valid") == true -> {
                    "Firebase no está configurado. Por favor, agrega el archivo google-services.json real desde Firebase Console y habilita Authentication."
                }
                e.message?.contains("network") == true -> {
                    "Error de conexión. Verifica tu conexión a internet."
                }
                e.message?.contains("email") == true -> {
                    "El correo electrónico ya está en uso o no es válido."
                }
                e.message?.contains("password") == true -> {
                    "La contraseña debe tener al menos 6 caracteres."
                }
                else -> {
                    e.message ?: "Error al registrar usuario. Verifica que Firebase esté configurado correctamente."
                }
            }
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            if (auth == null) {
                return Result.failure(Exception("Firebase no está configurado correctamente. Por favor, agrega el archivo google-services.json real desde Firebase Console."))
            }
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al iniciar sesión: ${e.message}")
            // Mensaje más amigable para errores comunes
            val errorMessage = when {
                e.message?.contains("API key not valid") == true -> {
                    "Firebase no está configurado. Por favor, agrega el archivo google-services.json real desde Firebase Console y habilita Authentication."
                }
                e.message?.contains("network") == true -> {
                    "Error de conexión. Verifica tu conexión a internet."
                }
                e.message?.contains("user-not-found") == true || e.message?.contains("wrong-password") == true -> {
                    "Correo electrónico o contraseña incorrectos."
                }
                else -> {
                    e.message ?: "Error al iniciar sesión. Verifica que Firebase esté configurado correctamente."
                }
            }
            Result.failure(Exception(errorMessage))
        }
    }

    fun logout() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al cerrar sesión: ${e.message}")
        }
    }

    fun isUserLoggedIn(): Boolean {
        return try {
            auth?.currentUser != null
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error al verificar sesión: ${e.message}")
            false
        }
    }
}

