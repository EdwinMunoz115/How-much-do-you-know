package com.example.howyouknow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.howyouknow.data.model.MediaItem
import com.example.howyouknow.data.model.Question
import com.example.howyouknow.data.model.QuestionType
import com.example.howyouknow.data.repository.LocalAuthRepository
import com.example.howyouknow.data.repository.LocalQuestionRepository
import com.example.howyouknow.data.repository.LocalUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuestionUiState(
    val isLoading: Boolean = false,
    val questionText: String = "",
    val questionType: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val options: List<String> = listOf("", ""),
    val mediaItems: List<MediaItem> = emptyList(),
    val correctAnswer: Any? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val partnerId: String? = null
)

class QuestionViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = LocalAuthRepository(application)
    private val userRepository = LocalUserRepository(application)
    private val questionRepository = LocalQuestionRepository(application)

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    init {
        loadPartnerId()
    }

    private fun loadPartnerId() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            val userId = currentUser?.userId ?: return@launch
            val result = userRepository.getUser(userId)
            result.fold(
                onSuccess = { user ->
                    _uiState.value = _uiState.value.copy(partnerId = user?.partnerId)
                },
                onFailure = { }
            )
        }
    }

    fun updateQuestionText(text: String) {
        _uiState.value = _uiState.value.copy(questionText = text)
    }

    fun updateQuestionType(type: QuestionType) {
        val newOptions = when (type) {
            QuestionType.YES_NO -> listOf("Sí", "No")
            QuestionType.OPEN -> emptyList()
            else -> if (_uiState.value.options.isEmpty()) listOf("", "") else _uiState.value.options
        }
        _uiState.value = _uiState.value.copy(
            questionType = type,
            options = newOptions,
            correctAnswer = null
        )
    }

    fun addOption() {
        val currentOptions = _uiState.value.options.toMutableList()
        currentOptions.add("")
        _uiState.value = _uiState.value.copy(options = currentOptions)
    }

    fun removeOption(index: Int) {
        val currentOptions = _uiState.value.options.toMutableList()
        if (currentOptions.size > 2) {
            currentOptions.removeAt(index)
            _uiState.value = _uiState.value.copy(options = currentOptions)
        }
    }

    fun updateOption(index: Int, value: String) {
        val currentOptions = _uiState.value.options.toMutableList()
        if (index < currentOptions.size) {
            currentOptions[index] = value
            _uiState.value = _uiState.value.copy(options = currentOptions)
        }
    }

    fun setCorrectAnswer(answer: Any?) {
        _uiState.value = _uiState.value.copy(correctAnswer = answer)
    }

    fun addMediaItem(mediaItem: MediaItem) {
        val currentMedia = _uiState.value.mediaItems.toMutableList()
        currentMedia.add(mediaItem)
        _uiState.value = _uiState.value.copy(mediaItems = currentMedia)
    }

    fun removeMediaItem(index: Int) {
        val currentMedia = _uiState.value.mediaItems.toMutableList()
        currentMedia.removeAt(index)
        _uiState.value = _uiState.value.copy(mediaItems = currentMedia)
    }

    fun saveQuestion() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            val userId = currentUser?.userId
            val partnerId = _uiState.value.partnerId

            if (userId == null || partnerId == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "No tienes pareja conectada"
                )
                return@launch
            }

            if (_uiState.value.questionText.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "La pregunta no puede estar vacía"
                )
                return@launch
            }

            val question = Question(
                creatorId = userId,
                partnerId = partnerId,
                questionText = _uiState.value.questionText,
                questionType = _uiState.value.questionType,
                options = _uiState.value.options.filter { it.isNotBlank() },
                mediaItems = _uiState.value.mediaItems,
                correctAnswer = _uiState.value.correctAnswer
            )

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = questionRepository.createQuestion(question)
            
            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Pregunta guardada exitosamente",
                        questionText = "",
                        options = listOf("", ""),
                        mediaItems = emptyList(),
                        correctAnswer = null
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al guardar la pregunta"
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

