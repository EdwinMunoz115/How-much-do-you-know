package com.example.howyouknow.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot

enum class QuestionType {
    MULTIPLE_CHOICE, // Opción múltiple
    YES_NO, // Sí/No
    OPEN, // Pregunta abierta
    RANKING, // Ordenar/ranking
    SURVEY // Encuesta
}

data class Question(
    val questionId: String = "",
    val creatorId: String = "",
    val partnerId: String = "",
    val questionText: String = "",
    val questionType: QuestionType = QuestionType.MULTIPLE_CHOICE,
    val options: List<String> = emptyList(), // Para opciones múltiples, ranking, etc.
    val mediaItems: List<MediaItem> = emptyList(),
    val correctAnswer: Any? = null, // Puede ser String, Int, List<String>, etc.
    val createdAt: Timestamp = Timestamp.now()
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): Question? {
            return try {
                val typeString = document.getString("questionType") ?: "MULTIPLE_CHOICE"
                val questionType = try {
                    QuestionType.valueOf(typeString)
                } catch (e: Exception) {
                    QuestionType.MULTIPLE_CHOICE
                }

                val optionsList = document.get("options") as? List<*> ?: emptyList<Any>()
                val options = optionsList.mapNotNull { it as? String }

                val mediaList = document.get("mediaItems") as? List<*> ?: emptyList<Any>()
                val mediaItems = mediaList.mapNotNull { mediaMap ->
                    if (mediaMap is Map<*, *>) {
                        val typeString = mediaMap["type"] as? String ?: "IMAGE"
                        MediaItem(
                            id = mediaMap["id"] as? String ?: "",
                            type = try {
                                MediaType.valueOf(typeString)
                            } catch (e: Exception) {
                                MediaType.IMAGE
                            },
                            uri = mediaMap["uri"] as? String ?: "",
                            thumbnailUri = mediaMap["thumbnailUri"] as? String
                        )
                    } else null
                }

                Question(
                    questionId = document.id,
                    creatorId = document.getString("creatorId") ?: "",
                    partnerId = document.getString("partnerId") ?: "",
                    questionText = document.getString("questionText") ?: "",
                    questionType = questionType,
                    options = options,
                    mediaItems = mediaItems,
                    correctAnswer = document.get("correctAnswer"),
                    createdAt = document.getTimestamp("createdAt") ?: Timestamp.now()
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    fun toMap(): Map<String, Any> {
        return mapOf(
            "creatorId" to creatorId,
            "partnerId" to partnerId,
            "questionText" to questionText,
            "questionType" to questionType.name,
            "options" to options,
            "mediaItems" to mediaItems.map { media ->
                mapOf(
                    "id" to media.id,
                    "type" to media.type.name,
                    "uri" to media.uri,
                    "thumbnailUri" to (media.thumbnailUri ?: "")
                )
            },
            "correctAnswer" to (correctAnswer ?: ""),
            "createdAt" to createdAt
        )
    }
}

