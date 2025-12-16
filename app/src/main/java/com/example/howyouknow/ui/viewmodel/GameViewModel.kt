package com.example.howyouknow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.howyouknow.data.local.AppDatabase
import com.example.howyouknow.data.local.entity.AnsweredQuestionEntity
import com.example.howyouknow.data.model.Answer
import com.example.howyouknow.data.model.GameSession
import com.example.howyouknow.data.model.Question
import com.example.howyouknow.data.repository.LocalAuthRepository
import com.example.howyouknow.data.repository.LocalGameRepository
import com.example.howyouknow.data.repository.LocalQuestionRepository
import com.example.howyouknow.data.repository.LocalUserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = false,
    val gameMenuVisible: Boolean = true, // Frame inicial con botones
    val session: GameSession? = null,
    val currentQuestion: Question? = null,
    val questions: List<Question> = emptyList(),
    val selectedAnswer: Any? = null,
    val timeRemaining: Int = 60, // Timer de 60 segundos
    val questionStartTime: Long = 0, // Tiempo de inicio de la pregunta actual
    val answerTimes: Map<String, Long> = emptyMap(), // Tiempo por pregunta
    val questionsAnswered: List<String> = emptyList(), // IDs de preguntas ya respondidas
    val errorMessage: String? = null,
    val gameCompleted: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = LocalAuthRepository(application)
    private val userRepository = LocalUserRepository(application)
    private val questionRepository = LocalQuestionRepository(application)
    private val gameRepository = LocalGameRepository(application)
    private val answeredQuestionDao = AppDatabase.getDatabase(application).answeredQuestionDao()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    // Función para iniciar el juego desde el menú
    fun startQuestionsGame() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                gameMenuVisible = false,
                errorMessage = null
            )

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

            // Obtener preguntas creadas por la pareja
            val questionsResult = questionRepository.getQuestionsForPartner(userId)
            val allQuestions = questionsResult.getOrNull() ?: emptyList()

            if (allQuestions.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No hay preguntas disponibles. Tu pareja debe crear preguntas primero."
                )
                return@launch
            }

            // Obtener preguntas ya respondidas
            val answeredQuestionIds = answeredQuestionDao.getAnsweredQuestionIds(userId).toSet()
            
            // Filtrar preguntas no respondidas y tomar 5 aleatorias
            val unansweredQuestions = allQuestions.filter { it.questionId !in answeredQuestionIds }
            val selectedQuestions = if (unansweredQuestions.size >= 5) {
                unansweredQuestions.shuffled().take(5)
            } else {
                unansweredQuestions.shuffled()
            }

            if (selectedQuestions.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ya has respondido todas las preguntas disponibles."
                )
                return@launch
            }

            // Crear sesión de juego
            val questionIds = selectedQuestions.map { it.questionId }
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
                questions = selectedQuestions,
                currentQuestion = selectedQuestions.firstOrNull(),
                questionStartTime = System.currentTimeMillis(),
                timeRemaining = 60
            )

            // Iniciar timer
            startTimer()
        }
    }

    // Función original mantenida para compatibilidad
    fun startGame() {
        // Por defecto mostrar el menú
        _uiState.value = _uiState.value.copy(gameMenuVisible = true)
    }

    // Timer de 60 segundos
    private var timerJob: kotlinx.coroutines.Job? = null
    
    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && !_uiState.value.gameCompleted) {
                delay(1000)
                if (!_uiState.value.gameCompleted) {
                    val newTime = _uiState.value.timeRemaining - 1
                    _uiState.value = _uiState.value.copy(timeRemaining = newTime)
                    
                    if (newTime <= 0) {
                        // Tiempo agotado, finalizar juego
                        finishGame()
                        break
                    }
                } else {
                    break
                }
            }
        }
    }

    private fun finishGame() {
        viewModelScope.launch {
            timerJob?.cancel()
            val session = _uiState.value.session ?: return@launch
            gameRepository.completeGameSession(session.sessionId)
            _uiState.value = _uiState.value.copy(gameCompleted = true)
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
            
            // Calcular tiempo de respuesta
            val timeSpent = System.currentTimeMillis() - _uiState.value.questionStartTime

            val isCorrect = checkAnswer(currentQuestion, selectedAnswer)
            // +100 puntos por respuesta correcta a la primera
            val points = if (isCorrect) 100 else 0

            val answer = Answer(
                questionId = currentQuestion.questionId,
                userAnswer = selectedAnswer,
                isCorrect = isCorrect,
                pointsEarned = points,
                comodinUsed = false,
                secondChanceUsed = false,
                timeSpent = timeSpent
            )

            // Guardar respuesta
            gameRepository.addAnswer(session.sessionId, answer)

            // Marcar pregunta como respondida
            val currentUserId = authRepository.getCurrentUser()?.userId
            if (currentUserId != null) {
                answeredQuestionDao.insertAnsweredQuestion(
                    AnsweredQuestionEntity(
                        userId = currentUserId,
                        questionId = currentQuestion.questionId
                    )
                )
            }

            // Actualizar puntos del usuario si es correcta
            if (points > 0) {
                userRepository.addPoints(session.userId, points)
            }

            // Guardar tiempo de respuesta
            val updatedAnswerTimes = _uiState.value.answerTimes + (currentQuestion.questionId to timeSpent)
            val updatedAnswered = _uiState.value.questionsAnswered + currentQuestion.questionId

            // Recargar sesión actualizada
            val updatedSessionResult = gameRepository.getGameSession(session.sessionId)
            val updatedSession = updatedSessionResult.getOrNull() ?: session

            // Actualizar estado local
            _uiState.value = _uiState.value.copy(
                session = updatedSession,
                answerTimes = updatedAnswerTimes,
                questionsAnswered = updatedAnswered
            )

            // Pasar a la siguiente pregunta
            moveToNextQuestion()
        }
    }

    // Funciones de comodines y segunda oportunidad removidas según nuevos requerimientos

    private fun moveToNextQuestion() {
        val session = _uiState.value.session ?: return
        val questions = _uiState.value.questions
        val nextIndex = session.currentQuestionIndex + 1

        if (nextIndex >= questions.size || _uiState.value.timeRemaining <= 0) {
            // Juego completado
            viewModelScope.launch {
                gameRepository.completeGameSession(session.sessionId)
                _uiState.value = _uiState.value.copy(gameCompleted = true)
            }
        } else {
            // Pasar a la siguiente pregunta
            _uiState.value = _uiState.value.copy(
                currentQuestion = questions[nextIndex],
                selectedAnswer = null,
                questionStartTime = System.currentTimeMillis()
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

    // Función removida - ahora siempre +100 por respuesta correcta
}

