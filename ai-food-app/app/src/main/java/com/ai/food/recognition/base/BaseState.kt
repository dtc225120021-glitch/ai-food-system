package com.ai.food.recognition.base

data class BaseState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
)