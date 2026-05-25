package com.ai.food.recognition.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.ai.food.recognition.data.local.entity.Script
import kotlinx.coroutines.flow.Flow

@Dao
interface ScriptDao {
    @Insert
    suspend fun insertScript(script: Script)

    @Suppress("HardCodedStringLiteral")
    @Query("SELECT * FROM script")
    fun getAll(): Flow<List<Script>>
}
