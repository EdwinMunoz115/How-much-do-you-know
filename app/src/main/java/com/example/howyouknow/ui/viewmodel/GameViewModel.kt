package com.example.howyouknow.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.howyouknow.data.local.AppDatabase
import com.example.howyouknow.data.local.entity.AnsweredQuestionEntity
import com.example.howyouknow.data.model.Answer
import com.example.howyouknow.data.model.GameSession
import com.example.howyouknow.data.model.Question
import com.example.howyouknow.data.model.QuestionType
import com.example.howyouknow.data.repository.LocalAuthRepository
import com.example.howyouknow.data.repository.LocalGameRepository
import com.example.howyouknow.data.repository.LocalQuestionRepository
import com.example.howyouknow.data.repository.LocalUserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
    val isLoading: Boolean = false,
    val gameMenuVisible: Boolean = true,
    val session: GameSession? = null,
    val currentQuestion: Question? = null,
    val questions: List<Question> = emptyList(),
    val selectedAnswer: Any? = null,
    val timeRemaining: Int = 60,
    val questionStartTime: Long = 0,
    val answerTimes: Map<String, Long> = emptyMap(),
    val questionsAnswered: List<String> = emptyList(),
    val errorMessage: String? = null,
    val gameCompleted: Boolean = false,
    val isPractice: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = LocalAuthRepository(application)
    private val userRepository = LocalUserRepository(application)
    private val questionRepository = LocalQuestionRepository(application)
    private val gameRepository = LocalGameRepository(application)
    private val answeredQuestionDao = AppDatabase.getDatabase(application).answeredQuestionDao()

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun startQuestionsGame(practice: Boolean = false) {
        if (_uiState.value.isLoading) return
        timerJob?.cancel()
        viewModelScope.launch {
            _uiState.value = GameUiState(
                isLoading = true,
                gameMenuVisible = false,
                isPractice = practice
            )

            val currentUser = authRepository.getCurrentUser()
            val userId = currentUser?.userId
            if (userId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Inicia sesion para jugar.")
                return@launch
            }
            val userResult = userRepository.getUser(userId)
            val user = userResult.getOrNull()
            val partnerId = if (practice) "practice" else user?.partnerId

            if (partnerId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Necesitas conectar a otro jugador para entrar al duelo."
                )
                return@launch
            }

            val questionsResult = if (practice) {
                runCatching { loadPracticeQuestions() }
            } else {
                questionRepository.getQuestionsForPartner(userId)
            }
            val allQuestions = questionsResult.getOrNull() ?: emptyList()

            if (allQuestions.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Aun no hay retos disponibles. Primero crea uno o espera a que el otro jugador te envie uno."
                )
                return@launch
            }

            val answeredQuestionIds = answeredQuestionDao.getAnsweredQuestionIds(userId).toSet()
            val unansweredQuestions = if (practice) allQuestions else allQuestions.filter { it.questionId !in answeredQuestionIds }
            val selectedQuestions = if (unansweredQuestions.size >= 5) {
                unansweredQuestions.shuffled().take(5)
            } else {
                unansweredQuestions.shuffled()
            }

            if (selectedQuestions.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ya jugaste todos los retos visuales disponibles."
                )
                return@launch
            }

            val questionIds = selectedQuestions.map { it.questionId }
            val sessionResult = gameRepository.createGameSession(userId, partnerId, questionIds)
            val sessionId = sessionResult.getOrNull()

            if (sessionId == null) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No se pudo crear la sesion de juego"
                )
                return@launch
            }

            val session = gameRepository.getGameSession(sessionId).getOrNull()
            val firstQuestion = selectedQuestions.firstOrNull()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                session = session,
                questions = selectedQuestions,
                currentQuestion = firstQuestion,
                selectedAnswer = defaultAnswerForQuestion(firstQuestion),
                questionStartTime = System.currentTimeMillis(),
                timeRemaining = 60
            )

            startTimer()
        }
    }

    private suspend fun loadPracticeQuestions(): List<Question> {
        val recipient = "practice-examples-v1"
        val existing = questionRepository.getQuestionsForPartner(recipient).getOrThrow()
        if (existing.size == 3) return existing
        val colors = listOf("Rojo", "Amarillo", "Verde")
        val examples = listOf(
            Question(
                questionText = "Ordena los colores del semaforo de arriba hacia abajo",
                questionType = QuestionType.RANKING,
                options = colors,
                correctAnswer = colors,
                mediaItems = listOf("practice_red", "practice_yellow", "practice_green").map {
                    com.example.howyouknow.data.model.MediaItem(
                        uri = "android.resource://${getApplication<Application>().packageName}/drawable/$it"
                    )
                }
            ),
            Question(
                questionText = "Cuanto es 2 + 2?",
                questionType = QuestionType.MULTIPLE_CHOICE,
                options = listOf("3", "4", "5"),
                correctAnswer = "4"
            ),
            Question(
                questionText = "El Sol es una estrella?",
                questionType = QuestionType.YES_NO,
                options = listOf("Si", "No"),
                correctAnswer = "Si"
            )
        )
        return examples.map { question ->
            existing.firstOrNull { it.questionText == question.questionText } ?: run {
                val sample = question.copy(creatorId = "practice", partnerId = recipient)
                val id = questionRepository.createQuestion(sample).getOrThrow()
                sample.copy(questionId = id)
            }
        }
    }

    fun startGame() {
        timerJob?.cancel()
        _uiState.value = GameUiState()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemaining > 0 && !_uiState.value.gameCompleted) {
                delay(1000)
                if (_uiState.value.gameCompleted) break

                val newTime = _uiState.value.timeRemaining - 1
                _uiState.value = _uiState.value.copy(timeRemaining = newTime)

                if (newTime <= 0) {
                    finishGame()
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

    fun moveRankingAnswer(fromIndex: Int, toIndex: Int) {
        val currentAnswer = (_uiState.value.selectedAnswer as? List<*>)?.mapNotNull { it?.toString() }?.toMutableList()
            ?: return
        if (fromIndex !in currentAnswer.indices || toIndex !in currentAnswer.indices) {
            return
        }

        val moved = currentAnswer.removeAt(fromIndex)
        currentAnswer.add(toIndex, moved)
        _uiState.value = _uiState.value.copy(selectedAnswer = currentAnswer)
    }

    fun submitAnswer() {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            val currentQuestion = _uiState.value.currentQuestion ?: return@launch
            val selectedAnswer = _uiState.value.selectedAnswer ?: return@launch

            val timeSpent = System.currentTimeMillis() - _uiState.value.questionStartTime
            val isCorrect = checkAnswer(currentQuestion, selectedAnswer)
            val points = calculatePoints(currentQuestion, selectedAnswer)

            val answer = Answer(
                questionId = currentQuestion.questionId,
                userAnswer = selectedAnswer,
                isCorrect = isCorrect,
                pointsEarned = points,
                comodinUsed = false,
                secondChanceUsed = false,
                timeSpent = timeSpent
            )

            gameRepository.addAnswer(session.sessionId, answer)

            val currentUserId = authRepository.getCurrentUser()?.userId
            if (currentUserId != null && !_uiState.value.isPractice) {
                answeredQuestionDao.insertAnsweredQuestion(
                    AnsweredQuestionEntity(
                        userId = currentUserId,
                        questionId = currentQuestion.questionId
                    )
                )
            }

            if (points > 0 && !_uiState.value.isPractice) {
                userRepository.addPoints(session.userId, points)
            }

            val updatedAnswerTimes = _uiState.value.answerTimes + (currentQuestion.questionId to timeSpent)
            val updatedAnswered = _uiState.value.questionsAnswered + currentQuestion.questionId
            val updatedSession = gameRepository.getGameSession(session.sessionId).getOrNull() ?: session

            _uiState.value = _uiState.value.copy(
                session = updatedSession,
                answerTimes = updatedAnswerTimes,
                questionsAnswered = updatedAnswered
            )

            moveToNextQuestion()
        }
    }

    private fun moveToNextQuestion() {
        val session = _uiState.value.session ?: return
        val questions = _uiState.value.questions
        val nextIndex = session.currentQuestionIndex

        if (nextIndex >= questions.size || _uiState.value.timeRemaining <= 0) {
            viewModelScope.launch {
                gameRepository.completeGameSession(session.sessionId)
                _uiState.value = _uiState.value.copy(gameCompleted = true)
            }
        } else {
            val nextQuestion = questions[nextIndex]
            _uiState.value = _uiState.value.copy(
                currentQuestion = nextQuestion,
                selectedAnswer = defaultAnswerForQuestion(nextQuestion),
                questionStartTime = System.currentTimeMillis()
            )
        }
    }

    private fun defaultAnswerForQuestion(question: Question?): Any? {
        return when (question?.questionType) {
            QuestionType.RANKING -> question.options.shuffled()
            else -> null
        }
    }

    private fun calculatePoints(question: Question, userAnswer: Any?): Int {
        return when (question.questionType) {
            QuestionType.RANKING -> {
                val expected = (question.correctAnswer as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val actual = (userAnswer as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val exactMatches = expected.indices.count { index ->
                    index < actual.size && expected[index] == actual[index]
                }
                val bonus = if (expected == actual && expected.isNotEmpty()) 120 else 0
                exactMatches * 40 + bonus
            }

            QuestionType.MULTIPLE_CHOICE, QuestionType.YES_NO, QuestionType.OPEN -> {
                if (checkAnswer(question, userAnswer)) 100 else 0
            }

            QuestionType.SURVEY -> 40
        }
    }

    private fun checkAnswer(question: Question, userAnswer: Any?): Boolean {
        return when (question.questionType) {
            QuestionType.MULTIPLE_CHOICE,
            QuestionType.YES_NO -> userAnswer == question.correctAnswer

            QuestionType.RANKING -> {
                val expected = (question.correctAnswer as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                val actual = (userAnswer as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                expected == actual
            }

            QuestionType.SURVEY -> true

            QuestionType.OPEN -> {
                val userText = userAnswer.toString().trim().lowercase()
                val correctText = question.correctAnswer.toString().trim().lowercase()
                userText == correctText
            }
        }
    }
}
