package com.ai.food.recognition.presentation.auth

import com.ai.food.recognition.base.BaseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class ForgotPasswordDRE {
    data class State(
        val isLoading: Boolean = false
    )

    sealed class Intent {
        data class SendOtp(val email: String) : Intent()
        data class VerifyOtp(val otp: String) : Intent()
        data class ResetPassword(val newPass: String) : Intent()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
        object OtpSent : Effect()
        object OtpVerified : Effect()
        object PasswordReset : Effect()
    }
}

class ForgotPasswordViewModel : BaseViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordDRE.State())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ForgotPasswordDRE.Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onIntent(intent: ForgotPasswordDRE.Intent) {
        when (intent) {
            is ForgotPasswordDRE.Intent.SendOtp -> sendOtpApi(intent.email)
            is ForgotPasswordDRE.Intent.VerifyOtp -> verifyOtpApi(intent.otp)
            is ForgotPasswordDRE.Intent.ResetPassword -> resetPasswordApi(intent.newPass)
        }
    }

    private fun sendOtpApi(email: String) {
        launchSafe(showLoading = true) {
            // MOCK API CALL
            delay(1000)
            _uiEffect.emit(ForgotPasswordDRE.Effect.OtpSent)
        }
    }

    private fun verifyOtpApi(otp: String) {
        launchSafe(showLoading = true) {
            // MOCK API CALL
            delay(1000)
            _uiEffect.emit(ForgotPasswordDRE.Effect.OtpVerified)
        }
    }

    private fun resetPasswordApi(newPass: String) {
        launchSafe(showLoading = true) {
            // MOCK API CALL
            delay(1000)
            _uiEffect.emit(ForgotPasswordDRE.Effect.PasswordReset)
        }
    }
}
