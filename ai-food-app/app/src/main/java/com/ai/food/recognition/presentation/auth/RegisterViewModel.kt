package com.ai.food.recognition.presentation.auth

import com.ai.food.recognition.base.BaseViewModel
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.data.remote.dto.RegisterRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class RegisterDRE {
    data class State(
        val isLoading: Boolean = false
    )

    sealed class Intent {
        data class Register(val fullName: String, val email: String, val password: String) : Intent()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
        object RegisterSuccess : Effect()
    }
}

class RegisterViewModel(
    private val authApi: AuthApi
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(RegisterDRE.State())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<RegisterDRE.Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onIntent(intent: RegisterDRE.Intent) {
        when (intent) {
            is RegisterDRE.Intent.Register -> {
                register(intent.fullName, intent.email, intent.password)
            }
        }
    }

    private fun register(fullName: String, email: String, password: String) {
        launchSafe(showLoading = true) {
            try {
                val request = RegisterRequest(fullName = fullName, email = email, password = password)
                val response = authApi.register(request)

                if (response.isSuccessful) {
                    _uiEffect.emit(RegisterDRE.Effect.RegisterSuccess)
                } else {
                    _uiEffect.emit(RegisterDRE.Effect.ShowError("Đăng ký thất bại! Lỗi ${response.code()}"))
                }
            } catch (e: Exception) {
                _uiEffect.emit(RegisterDRE.Effect.ShowError("Có lỗi xảy ra: ${e.message}"))
            }
        }
    }
}
