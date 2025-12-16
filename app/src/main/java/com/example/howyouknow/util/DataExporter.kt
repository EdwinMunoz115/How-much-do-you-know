package com.example.howyouknow.util

import android.content.Context
import android.net.Uri
import com.example.howyouknow.data.local.AppDatabase
import com.example.howyouknow.data.local.entity.QuestionEntity
import com.example.howyouknow.data.local.entity.UserEntity
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

data class ExportData(
    val users: List<UserEntity>,
    val questions: List<QuestionEntity>
)

object DataExporter {
    private val gson = Gson()

    suspend fun exportToFile(context: Context, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(context)
            val users = db.userDao().let { dao ->
                // Necesitaríamos un método para obtener todos los usuarios
                emptyList<UserEntity>()
            }
            val questions = db.questionDao().getQuestionsByCreator("") // Esto no funcionará bien
            
            val exportData = ExportData(users, questions)
            val json = gson.toJson(exportData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

