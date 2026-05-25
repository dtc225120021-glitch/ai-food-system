package com.ai.food.recognition.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ai.food.recognition.data.local.dao.ScriptDao
import com.ai.food.recognition.data.local.entity.Script

@Database(
    entities = [Script::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun scriptDao(): ScriptDao
}
