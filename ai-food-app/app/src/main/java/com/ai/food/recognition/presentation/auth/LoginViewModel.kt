package com.ai.food.recognition.presentation.auth

import com.ai.food.recognition.base.BaseViewModel
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.data.remote.dto.LoginRequest
import com.ai.food.recognition.domain.model.local.LocalStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginDRE {
    data class State(
        val isLoading: Boolean = false
    )

    sealed class Intent {
        data class Login(val email: String, val password: String, val isRemember: Boolean) : Intent()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
        object NavigateToHome : Effect()
        object NavigateToUserConfig : Effect()
    }
}

class LoginViewModel(
    private val authApi: AuthApi,
    private val dataStorage: LocalStorage
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(LoginDRE.State())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<LoginDRE.Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onIntent(intent: LoginDRE.Intent) {
        when (intent) {
            is LoginDRE.Intent.Login -> {
                login(intent.email, intent.password, intent.isRemember)
            }
        }
    }

    private fun login(email: String, password: String, isRemember: Boolean) {
        launchSafe(showLoading = true) {
            try {
                val request = LoginRequest(email = email, password = password)
                val response = authApi.login(request)

                if (response.isSuccessful) {
                    val data = response.body()
                    val accessToken = data?.data?.accessToken ?: ""
                    val refreshToken = data?.data?.refreshToken ?: ""

                    SessionManager.accessToken = accessToken
                    SessionManager.refreshToken = refreshToken

                    if (isRemember) {
                        dataStorage.saveToken(accessToken)
                        dataStorage.saveRefreshToken(refreshToken)
                    } else {
                        dataStorage.saveToken("")
                        dataStorage.saveRefreshToken("")
                    }

                    val config = authApi.getConfig()
                    if (config.body()?.success == true) {
                        _uiEffect.emit(LoginDRE.Effect.NavigateToHome)
                    } else {
                        _uiEffect.emit(LoginDRE.Effect.NavigateToUserConfig)
                    }
                } else {
                    _uiEffect.emit(LoginDRE.Effect.ShowError("Đăng nhập thất bại: Lỗi ${response.code()}"))
                }
            } catch (e: Exception) {
                _uiEffect.emit(LoginDRE.Effect.ShowError("Đăng nhập thất bại: ${e.message}"))
            }
        }
    }
}
