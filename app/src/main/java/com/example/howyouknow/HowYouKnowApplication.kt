package com.example.howyouknow

import android.app.Application

class HowYouKnowApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Usando Room para base de datos local - no se necesita Firebase
    }
}

