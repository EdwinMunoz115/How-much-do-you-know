package com.example.howyouknow.data.model

enum class MediaType {
    IMAGE,
    VIDEO,
    AUDIO,
    GIF
}

data class MediaItem(
    val id: String = "",
    val type: MediaType = MediaType.IMAGE,
    val uri: String = "", // URI local o URL
    val thumbnailUri: String? = null // Para videos
)

