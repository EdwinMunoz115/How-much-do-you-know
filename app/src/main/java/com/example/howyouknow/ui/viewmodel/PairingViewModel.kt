package com.example.howyouknow.ui.viewmodel

import android.app.Application
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
            val currentUser = authRepository.getCurrentUser()
            if (currentUser != null) {
                val result = userRepository.getUser(currentUser.userId)
                result.fold(
                    onSuccess = { user ->
                        _uiState.value = _uiState.value.copy(
                            currentUser = user,
                            isPaired = user?.partnerId != null
                        )
                    },
                    onFailure = { }
                )
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

