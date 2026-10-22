package com.example.howyouknow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.howyouknow.data.model.MediaItem
import com.example.howyouknow.data.model.MediaType
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
    val questionType: QuestionType = QuestionType.RANKING,
    val options: List<String> = listOf("", "", ""),
    val mediaItems: List<MediaItem> = List(3) { emptyMediaItem() },
    val correctAnswer: Any? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val partnerId: String? = null
)

private fun emptyMediaItem(): MediaItem = MediaItem(type = MediaType.IMAGE, uri = "")

class QuestionViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = LocalAuthRepository(application)
    private val userRepository = LocalUserRepository(application)
    private val questionRepository = LocalQuestionRepository(application)

    private val _uiState = MutableStateFlow(QuestionUiState())
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    init {
        loadPartnerId()
        syncCorrectAnswerWithOptions()
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
        val current = _uiState.value
        val newOptions = when (type) {
            QuestionType.YES_NO -> listOf("Si", "No")
            QuestionType.OPEN -> emptyList()
            QuestionType.RANKING -> ensureMinimumItems(current.options, 3)
            else -> ensureMinimumItems(current.options, 2)
        }
        val newMedia = when (type) {
            QuestionType.RANKING -> syncMediaWithOptions(current.mediaItems, newOptions.size)
            else -> current.mediaItems
        }

        _uiState.value = current.copy(
            questionType = type,
            options = newOptions,
            mediaItems = newMedia,
            correctAnswer = null
        )
        syncCorrectAnswerWithOptions()
    }

    fun addOption() {
        val currentOptions = _uiState.value.options.toMutableList()
        currentOptions.add("")
        _uiState.value = _uiState.value.copy(
            options = currentOptions,
            mediaItems = syncMediaWithOptions(_uiState.value.mediaItems, currentOptions.size)
        )
        syncCorrectAnswerWithOptions()
    }

    fun removeOption(index: Int) {
        val currentOptions = _uiState.value.options.toMutableList()
        if (currentOptions.size > 2 && index in currentOptions.indices) {
            currentOptions.removeAt(index)
            val currentMedia = _uiState.value.mediaItems.toMutableList()
            if (index in currentMedia.indices) {
                currentMedia.removeAt(index)
            }
            _uiState.value = _uiState.value.copy(
                options = currentOptions,
                mediaItems = syncMediaWithOptions(currentMedia, currentOptions.size)
            )
            syncCorrectAnswerWithOptions()
        }
    }

    fun updateOption(index: Int, value: String) {
        val currentOptions = _uiState.value.options.toMutableList()
        if (index in currentOptions.indices) {
            currentOptions[index] = value
            _uiState.value = _uiState.value.copy(options = currentOptions)
            syncCorrectAnswerWithOptions()
        }
    }

    fun updateMediaUri(index: Int, uri: String) {
        val currentMedia = syncMediaWithOptions(_uiState.value.mediaItems, _uiState.value.options.size).toMutableList()
        if (index in currentMedia.indices) {
            currentMedia[index] = currentMedia[index].copy(uri = uri.trim())
            _uiState.value = _uiState.value.copy(mediaItems = currentMedia)
        }
    }

    fun setCorrectAnswer(answer: Any?) {
        _uiState.value = _uiState.value.copy(correctAnswer = answer)
    }

    fun moveRankingItem(fromIndex: Int, toIndex: Int) {
        val options = _uiState.value.options.toMutableList()
        if (fromIndex !in options.indices || toIndex !in options.indices) {
            return
        }

        val moved = options.removeAt(fromIndex)
        options.add(toIndex, moved)
        _uiState.value = _uiState.value.copy(correctAnswer = options.filter { it.isNotBlank() })
    }

    fun saveQuestion() {
        viewModelScope.launch {
            val currentUser = authRepository.getCurrentUser()
            val userId = currentUser?.userId
            val partnerId = _uiState.value.partnerId

            if (userId == null || partnerId == null) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Primero conecta a otro jugador para compartir retos."
                )
                return@launch
            }

            if (_uiState.value.questionText.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "El titulo del reto no puede estar vacio"
                )
                return@launch
            }

            val optionMediaPairs = _uiState.value.options.mapIndexedNotNull { index, option ->
                option.trim().takeIf { it.isNotBlank() }?.let { trimmed ->
                    trimmed to _uiState.value.mediaItems.getOrElse(index) { emptyMediaItem() }
                }
            }
            val cleanOptions = optionMediaPairs.map { it.first }
            if (cleanOptions.size < 2) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Agrega al menos dos opciones para el reto"
                )
                return@launch
            }

            val cleanMedia = optionMediaPairs.map { it.second }
            if (_uiState.value.questionType == QuestionType.RANKING && cleanMedia.any { it.uri.isBlank() }) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Cada opcion del top necesita una imagen"
                )
                return@launch
            }

            val correctAnswer = when (_uiState.value.questionType) {
                QuestionType.RANKING -> {
                    (_uiState.value.correctAnswer as? List<*>)?.mapNotNull { it?.toString() } ?: cleanOptions
                }
                else -> _uiState.value.correctAnswer
            }

            val question = Question(
                creatorId = userId,
                partnerId = partnerId,
                questionText = _uiState.value.questionText,
                questionType = _uiState.value.questionType,
                options = cleanOptions,
                mediaItems = cleanMedia,
                correctAnswer = correctAnswer
            )

            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = questionRepository.createQuestion(question)

            result.fold(
                onSuccess = {
                    _uiState.value = QuestionUiState(
                        successMessage = "Reto guardado exitosamente",
                        partnerId = _uiState.value.partnerId
                    )
                },
                onFailure = { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "Error al guardar el reto"
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

    private fun syncCorrectAnswerWithOptions() {
        if (_uiState.value.questionType == QuestionType.RANKING) {
            _uiState.value = _uiState.value.copy(
                correctAnswer = _uiState.value.options.filter { it.isNotBlank() }
            )
        }
    }

    private fun ensureMinimumItems(options: List<String>, minItems: Int): List<String> {
        val normalized = if (options.isEmpty()) mutableListOf<String>() else options.toMutableList()
        while (normalized.size < minItems) {
            normalized.add("")
        }
        return normalized
    }

    private fun syncMediaWithOptions(media: List<MediaItem>, optionCount: Int): List<MediaItem> {
        val mutable = media.toMutableList()
        while (mutable.size < optionCount) {
            mutable.add(emptyMediaItem())
        }
        return mutable.take(optionCount)
    }
}
