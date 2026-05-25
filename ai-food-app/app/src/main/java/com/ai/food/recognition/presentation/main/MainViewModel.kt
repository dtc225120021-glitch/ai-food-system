package com.ai.food.recognition.presentation.main

import android.util.Log
import com.ai.food.recognition.base.BaseViewModel
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.data.remote.dto.DailyReportDto
import com.ai.food.recognition.data.remote.dto.NutritionGoal
import com.ai.food.recognition.data.remote.dto.UserConfig
import com.ai.food.recognition.data.remote.dto.UserProfile
import com.ai.food.recognition.domain.model.local.LocalStorage
import com.ai.food.recognition.domain.repository.ScriptRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

class MainDRE {
    data class State(
        val isLoading: Boolean = false,
        val userProfile: UserProfile? = null,
        val userConfig: UserConfig? = null,
        val nutritionGoal: NutritionGoal? = null,
        val dailyReport: DailyReportDto? = null,
        val error: String? = null
    )

    sealed class Intent {
        object GetProfile : Intent()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
    }
}

class MainViewModel(
    private val repository: ScriptRepository,
    private val dataStorage: LocalStorage,
    private val authApi: AuthApi
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(MainDRE.State())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<MainDRE.Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    init {
        onIntent(MainDRE.Intent.GetProfile)
    }

    fun onIntent(intent: MainDRE.Intent) {
        when (intent) {
            is MainDRE.Intent.GetProfile -> getProfile()
        }
    }

    private fun getProfile() {
        launchSafe(showLoading = false) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val response = authApi.getProfile()
                if (response.isSuccessful) {
                    val profileData = response.body()?.data
                    val user = profileData?.user
                    val config = profileData?.config
                    val nutritionGoal = profileData?.nutritionGoal
                    val dailyReport = profileData?.dailyReport

                    if (user != null) {
                        SessionManager.email = user.email
                        SessionManager.fullName = user.fullName
                        SessionManager.avatarUri = user.avatar
                    }
                    if (config != null) {
                        if (config.dailyCalorieGoal != null) {
                            SessionManager.calories = config.dailyCalorieGoal
                        }
                    }
                    if (nutritionGoal != null) {
                        if (nutritionGoal.dailyCalorieGoal != null) {
                            SessionManager.calories = nutritionGoal.dailyCalorieGoal
                        }
                        SessionManager.carbs = nutritionGoal.dailyCarbsGoal ?: 0
                        SessionManager.proteins = nutritionGoal.dailyProteinGoal ?: 0
                        SessionManager.fats = nutritionGoal.dailyFatGoal ?: 0
                    }
                    if (dailyReport != null) {
                        SessionManager.consumedCalories = dailyReport.consumedCalories ?: 0
                        SessionManager.consumedCarbs = dailyReport.consumedCarbs ?: 0
                        SessionManager.consumedProteins = dailyReport.consumedProtein ?: 0
                        SessionManager.consumedFats = dailyReport.consumedFat ?: 0
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        userProfile = user,
                        userConfig = config,
                        nutritionGoal = nutritionGoal,
                        dailyReport = dailyReport
                    )
                    Log.d("MainViewModel", "Loaded user profile, config, and daily goals successfully.")
                } else {
                    val errMsg = "Failed to fetch user profile: Lỗi ${response.code()}"
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errMsg)
                    _uiEffect.emit(MainDRE.Effect.ShowError(errMsg))
                }
            } catch (e: Exception) {
                val errMsg = e.message ?: "Unknown error"
                _uiState.value = _uiState.value.copy(isLoading = false, error = errMsg)
                _uiEffect.emit(MainDRE.Effect.ShowError(errMsg))
            }
        }
    }

    fun getScripts() {
        launchSafe {
            repository.getScripts().collect { list ->
                list.forEach {
                    Log.d("TAGs", "name = ${it.name}")
                }
            }
        }
    }

    fun addScript(name: String) {
        launchSafe {
            repository.insertScript(name = name, age = 1)
        }
    }
}


