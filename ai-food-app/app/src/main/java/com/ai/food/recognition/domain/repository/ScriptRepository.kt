package com.ai.food.recognition.domain.repository

import com.ai.food.recognition.data.local.entity.Script
import kotlinx.coroutines.flow.Flow

interface ScriptRepository {

    suspend fun insertScript(name: String, age: Int)

    fun getScripts(): Flow<List<Script>>
}
