package com.example.howyouknow.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.howyouknow.data.model.User
import com.example.howyouknow.data.repository.LocalAuthRepository
import com.example.howyouknow.data.repository.LocalPairingRepository
import com.example.howyouknow.data.repository.LocalUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PairingUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val isPaired: Boolean = false,
    val connectionSuccess: Boolean = false
)

class PairingViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = LocalAuthRepository(application)
    private val userRepository = LocalUserRepository(application)
    private val pairingRepository = LocalPairingRepository(application)

    private val _uiState = MutableStateFlow(PairingUiState())
    val uiState: StateFlow<PairingUiState> = _uiState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                Log.d("PairingViewModel", "Cargando usuario actual...")
                val currentUser = authRepository.getCurrentUser()
                if (currentUser != null) {
                    Log.d("PairingViewModel", "Usuario autenticado encontrado: ${currentUser.userId}, código: ${currentUser.invitationCode}")
                    
                    // Asegurar que el usuario tenga código de invitación
                    userRepository.ensureInvitationCode(currentUser.userId)
                    
                    val result = userRepository.getUser(currentUser.userId)
                    result.fold(
                        onSuccess = { user ->
                            var finalUser = user
                            // Si el usuario no tiene código o está vacío, usar el del LocalUser
                            if (finalUser != null && (finalUser.invitationCode.isBlank() || finalUser.invitationCode.isEmpty())) {
                                Log.w("PairingViewModel", "Usuario sin código de invitación, usando código del LocalUser")
                                finalUser = finalUser.copy(invitationCode = currentUser.invitationCode)
                            }
                            Log.d("PairingViewModel", "Usuario cargado exitosamente: ${finalUser?.name}, código: ${finalUser?.invitationCode}, pareja: ${finalUser?.partnerId}")
                            _uiState.value = _uiState.value.copy(
                                currentUser = finalUser,
                                isPaired = finalUser?.partnerId != null
                            )
                        },
                        onFailure = { exception ->
                            Log.e("PairingViewModel", "Error al cargar usuario: ${exception.message}")
                            // Si falla, usar el LocalUser directamente
                            val localUser = authRepository.getCurrentUser()
                            if (localUser != null) {
                                val user = User(
                                    userId = localUser.userId,
                                    name = localUser.name,
                                    email = localUser.email,
                                    age = localUser.age,
                                    partnerId = localUser.partnerId,
                                    invitationCode = localUser.invitationCode.ifBlank { 
                                        // Generar código si está vacío
                                        java.util.UUID.randomUUID().toString().substring(0, 6).uppercase()
                                    },
                                    totalPoints = localUser.totalPoints
                                )
                                _uiState.value = _uiState.value.copy(
                                    currentUser = user,
                                    isPaired = user.partnerId != null
                                )
                            }
                        }
                    )
                } else {
                    Log.w("PairingViewModel", "No hay usuario autenticado")
                }
            } catch (e: Exception) {
                Log.e("PairingViewModel", "Excepción al cargar usuario: ${e.message}", e)
            }
        }
    }

    fun connectWithCode(code: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null, connectionSuccess = false)
            
            val currentUser = authRepository.getCurrentUser()
            val currentUserId = currentUser?.userId
            if (currentUserId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No hay usuario autenticado"
                )
                return@launch
            }

            val result = pairingRepository.connectWithCode(currentUserId, code)
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        connectionSuccess = true,
                        isPaired = true
                    )
                    loadCurrentUser()
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al conectar con la pareja"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(connectionSuccess = false)
    }
}

