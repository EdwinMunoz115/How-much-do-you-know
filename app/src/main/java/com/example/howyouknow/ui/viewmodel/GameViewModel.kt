package com.example.howyouknow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.howyouknow.data.model.Answer
import com.example.howyouknow.data.model.GameSession
import com.example.howyouknow.data.model.Question
import com.example.howyouknow.data.repository.LocalAuthRepository
import com.example.howyouknow.data.repository.LocalGameRepository
import com.example.howyouknow.data.repository.LocalQuestionRepository
import com.example.howyouknow.data.repository.LocalUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = false,
    val session: GameSession? = null,
    val currentQuestion: Question? = null,
    val questions: List<Question> = emptyList(),
    val selectedAnswer: Any? = null,
    val comodinesAvailable: Int = 3,
    val comodinesUsed: Int = 0,
    val showSecondChance: Boolean = false,
    val errorMessage: String? = null,
    val gameCompleted: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = LocalAuthRepository(application)
    private val userRepository = LocalUserRepository(application)
    private val questionRepository = LocalQuestionRepository(application)
    private val gameRepository = LocalGameRepository(application)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    fun startGame() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val currentUser = authRepository.getCurrentUser()
            val userId = currentUser?.userId ?: return@launch
            val userResult = userRepository.getUser(userId)
            val user = userResult.getOrNull()
            val partnerId = user?.partnerId

            if (partnerId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No tienes pareja conectada"
                )
                return@launch
            }

            // Obtener preguntas creadas por la pareja para este usuario
            // Las preguntas tienen partnerId = userId del usuario actual
            // porque fueron creadas por su pareja (creatorId = partnerId del usuario actual)
            android.util.Log.d("GameViewModel", "Buscando preguntas para usuario: $userId, pareja: $partnerId")
            val questionsResult = questionRepository.getQuestionsForPartner(userId)
            val questions = questionsResult.getOrNull() ?: emptyList()
            android.util.Log.d("GameViewModel", "Preguntas encontradas: ${questions.size}")

            if (questions.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No hay preguntas disponibles. Tu pareja debe crear preguntas primero."
                )
                return@launch
            }

            // Crear sesión de juego
            val questionIds = questions.map { it.questionId }
            val sessionResult = gameRepository.createGameSession(userId, partnerId, questionIds)
            val sessionId = sessionResult.getOrNull()

            if (sessionId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al crear sesión de juego"
                )
                return@launch
            }

            val sessionResult2 = gameRepository.getGameSession(sessionId)
            val session = sessionResult2.getOrNull()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                session = session,
                questions = questions,
                currentQuestion = questions.firstOrNull(),
                comodinesAvailable = 3
            )
        }
    }

    fun selectAnswer(answer: Any?) {
        _uiState.value = _uiState.value.copy(selectedAnswer = answer)
    }

    fun submitAnswer() {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val currentQuestion = _uiState.value.currentQuestion ?: return@launch
            val selectedAnswer = _uiState.value.selectedAnswer ?: return@launch
            // Verificar si se usó comodín en esta pregunta
            // Si comodinesUsed en el estado es mayor que en la sesión, significa que se usó uno
            val comodinUsed = _uiState.value.comodinesUsed > session.comodinesUsed

            val isCorrect = checkAnswer(currentQuestion, selectedAnswer)
            val points = calculatePoints(isCorrect, comodinUsed, _uiState.value.showSecondChance)

            val answer = Answer(
                questionId = currentQuestion.questionId,
                userAnswer = selectedAnswer,
                isCorrect = isCorrect,
                pointsEarned = points,
                comodinUsed = comodinUsed,
                secondChanceUsed = _uiState.value.showSecondChance
            )

            val updatedComodinesUsed = if (comodinUsed) session.comodinesUsed + 1 else session.comodinesUsed

            // Si la respuesta es incorrecta y hay más de 2 opciones, ofrecer segunda oportunidad
            if (!isCorrect && currentQuestion.options.size > 2 && !_uiState.value.showSecondChance) {
                _uiState.value = _uiState.value.copy(showSecondChance = true)
                return@launch
            }

            // Guardar respuesta (esto actualiza la sesión internamente)
            gameRepository.addAnswer(session.sessionId, answer)

            // Actualizar puntos del usuario
            if (points > 0) {
                userRepository.addPoints(session.userId, points)
            }

            // Recargar sesión actualizada
            val updatedSessionResult = gameRepository.getGameSession(session.sessionId)
            val updatedSession = updatedSessionResult.getOrNull() ?: session

            // Actualizar estado local
            _uiState.value = _uiState.value.copy(
                session = updatedSession
            )

            // Pasar a la siguiente pregunta
            moveToNextQuestion()
        }
    }

    fun useComodin() {
        val currentQuestion = _uiState.value.currentQuestion ?: return
        val comodinesAvailable = 3 - _uiState.value.comodinesUsed

        if (comodinesAvailable <= 0 || currentQuestion.options.size <= 3) {
            return
        }

        // Eliminar la mitad de opciones incorrectas
        val correctAnswer = currentQuestion.correctAnswer
        val incorrectOptions = currentQuestion.options.filter { it != correctAnswer }
        val optionsToRemove = incorrectOptions.take(incorrectOptions.size / 2)
        val remainingOptions = currentQuestion.options.filter { it !in optionsToRemove }

        val updatedQuestion = currentQuestion.copy(options = remainingOptions)
        _uiState.value = _uiState.value.copy(
            currentQuestion = updatedQuestion,
            comodinesUsed = _uiState.value.comodinesUsed + 1
        )
    }

    fun useSecondChance() {
        _uiState.value = _uiState.value.copy(showSecondChance = false)
        // El usuario puede responder de nuevo, pero perderá 5 puntos
    }

    private fun moveToNextQuestion() {
        val session = _uiState.value.session ?: return
        val questions = _uiState.value.questions
        val nextIndex = session.currentQuestionIndex + 1

        if (nextIndex >= questions.size) {
            // Juego completado
            viewModelScope.launch {
                gameRepository.completeGameSession(session.sessionId)
                _uiState.value = _uiState.value.copy(gameCompleted = true)
            }
        } else {
            // Resetear comodines usados para la nueva pregunta
            val comodinesUsedInSession = session.comodinesUsed
            _uiState.value = _uiState.value.copy(
                currentQuestion = questions[nextIndex],
                selectedAnswer = null,
                showSecondChance = false,
                comodinesUsed = comodinesUsedInSession
            )
        }
    }

    private fun checkAnswer(question: Question, userAnswer: Any?): Boolean {
        return when (question.questionType) {
            com.example.howyouknow.data.model.QuestionType.MULTIPLE_CHOICE,
            com.example.howyouknow.data.model.QuestionType.YES_NO -> {
                userAnswer == question.correctAnswer
            }
            com.example.howyouknow.data.model.QuestionType.RANKING,
            com.example.howyouknow.data.model.QuestionType.SURVEY -> {
                // Para ranking, comparar listas
                if (userAnswer is List<*> && question.correctAnswer is List<*>) {
                    userAnswer == question.correctAnswer
                } else {
                    false
                }
            }
            com.example.howyouknow.data.model.QuestionType.OPEN -> {
                // Para preguntas abiertas, comparación de texto (case-insensitive)
                val userText = userAnswer.toString().trim().lowercase()
                val correctText = question.correctAnswer.toString().trim().lowercase()
                userText == correctText
            }
        }
    }

    private fun calculatePoints(isCorrect: Boolean, comodinUsed: Boolean, secondChanceUsed: Boolean): Int {
        if (!isCorrect) {
            return if (secondChanceUsed) -5 else 0
        }
        return if (comodinUsed) 50 else 100
    }
}

