package com.ai.food.recognition.presentation.splash

import com.ai.food.recognition.base.BaseViewModel
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.data.remote.dto.RefreshTokenRequest
import com.ai.food.recognition.domain.model.local.LocalStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull

class SplashDRE {
    data class State(
        val isLoading: Boolean = false
    )

    sealed class Intent {
        object CheckToken : Intent()
    }

    sealed class Effect {
        object NavigateToHome : Effect()
        object NavigateToAuth : Effect()
    }
}

class SplashViewModel(
    private val authApi: AuthApi,
    private val dataStorage: LocalStorage
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(SplashDRE.State())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<SplashDRE.Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onIntent(intent: SplashDRE.Intent) {
        when (intent) {
            is SplashDRE.Intent.CheckToken -> checkToken()
        }
    }

    private fun checkToken() {
        launchSafe(showLoading = false) {
            val refreshToken = dataStorage.refreshToken.firstOrNull()

            var isLoggedIn = false
            if (!refreshToken.isNullOrEmpty()) {
                try {
                    val response = authApi.refreshToken(RefreshTokenRequest(refreshToken))
                    if (response.isSuccessful) {
                        val data = response.body()
                        val newAccessToken = data?.data?.accessToken ?: ""
                        val newRefreshToken = data?.data?.refreshToken ?: ""

                        dataStorage.saveToken(newAccessToken)
                        dataStorage.saveRefreshToken(newRefreshToken)

                        SessionManager.accessToken = newAccessToken
                        SessionManager.refreshToken = newRefreshToken

                        isLoggedIn = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            if (isLoggedIn) {
                _uiEffect.emit(SplashDRE.Effect.NavigateToHome)
            } else {
                _uiEffect.emit(SplashDRE.Effect.NavigateToAuth)
            }
        }
    }
}
