package com.ai.food.recognition.domain.model.local

import kotlinx.coroutines.flow.Flow

interface LocalStorage {
    val token: Flow<String?>
    val refreshToken: Flow<String?>
    val isLogin: Flow<Boolean>

    suspend fun saveToken(token: String)
    suspend fun saveRefreshToken(token: String)
    suspend fun setLogin(isLogin: Boolean)
    suspend fun clear()
}