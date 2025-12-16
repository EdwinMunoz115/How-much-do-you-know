package com.example.howyouknow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.howyouknow.data.local.converter.Converters
import com.example.howyouknow.data.local.dao.GameSessionDao
import com.example.howyouknow.data.local.dao.QuestionDao
import com.example.howyouknow.data.local.dao.UserDao
import com.example.howyouknow.data.local.entity.GameSessionEntity
import com.example.howyouknow.data.local.entity.QuestionEntity
import com.example.howyouknow.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, QuestionEntity::class, GameSessionEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun questionDao(): QuestionDao
    abstract fun gameSessionDao(): GameSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "how_you_know_database"
                )
                    .fallbackToDestructiveMigration() // Para desarrollo - elimina en producción
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

