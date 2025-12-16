package com.example.howyouknow.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.howyouknow.data.model.MediaItem
import com.example.howyouknow.data.model.MediaType
import java.io.File
import java.util.UUID

object MediaHandler {
    fun createImageUri(context: Context): Uri? {
        return try {
            val imageFile = File(context.filesDir, "images/${UUID.randomUUID()}.jpg")
            imageFile.parentFile?.mkdirs()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
        } catch (e: Exception) {
            null
        }
    }

    fun createVideoUri(context: Context): Uri? {
        return try {
            val videoFile = File(context.filesDir, "videos/${UUID.randomUUID()}.mp4")
            videoFile.parentFile?.mkdirs()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                videoFile
            )
        } catch (e: Exception) {
            null
        }
    }

    fun createAudioUri(context: Context): Uri? {
        return try {
            val audioFile = File(context.filesDir, "audios/${UUID.randomUUID()}.mp3")
            audioFile.parentFile?.mkdirs()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                audioFile
            )
        } catch (e: Exception) {
            null
        }
    }

    fun createMediaItemFromUri(uri: Uri, type: MediaType): MediaItem {
        return MediaItem(
            id = UUID.randomUUID().toString(),
            type = type,
            uri = uri.toString()
        )
    }

    fun getGifUrl(query: String): String {
        // Placeholder - en producción usarías una API como Giphy
        // Por ahora retornamos una URL de ejemplo
        return "https://api.giphy.com/v1/gifs/search?api_key=YOUR_API_KEY&q=$query"
    }
}

