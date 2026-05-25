package com.ai.food.recognition.presentation.auth

import com.ai.food.recognition.base.BaseViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthDRE {
    data class State(
        val isLoading: Boolean = false
    )

    sealed class Intent {
        // Defined for future use if AuthActivity needs to handle actions
    }

    sealed class Effect {
        // Defined for future use
    }
}

class AuthViewModel : BaseViewModel() {

    private val _uiState = MutableStateFlow(AuthDRE.State())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<AuthDRE.Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onIntent(intent: AuthDRE.Intent) {
        when (intent) {
            // handle intents here
            else -> {}
        }
    }
}