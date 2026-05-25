package com.ai.food.recognition.presentation.config

import com.ai.food.recognition.base.BaseViewModel
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.data.remote.dto.ConfigData
import com.ai.food.recognition.data.remote.dto.SetupConfigRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.math.roundToInt

data class ConfigResult(
    val calories: Int,
    val carbs: Int,
    val protein: Int,
    val fats: Int
)

class UserConfigViewModel(
    private val authApi: AuthApi
) : BaseViewModel() {

    private val _calculationSuccess = MutableSharedFlow<ConfigResult>()
    val calculationSuccess: SharedFlow<ConfigResult> = _calculationSuccess

    private val _configLoaded = MutableSharedFlow<ConfigData>()
    val configLoaded: SharedFlow<ConfigData> = _configLoaded

    fun loadUserConfig() {
        launchSafe(showLoading = true) {
            try {
                val response = authApi.getConfig()
                if (response.isSuccessful && response.body()?.success == true) {
                    val configData = response.body()?.data
                    if (configData != null) {
                        _configLoaded.emit(configData)
                    }
                }
            } catch (e: Exception) {
                // Handle silently
            }
        }
    }

    fun calculateAndSaveConfig(
        gender: String, // "Male" or "Female"
        weight: Double, // kg
        height: Double, // cm
        age: Int,       // years
        goal: String,   // "Lose", "Maintain", "Gain"
        activityLevel: String
    ) {
        launchSafe(showLoading = true) {
            try {
                val request = SetupConfigRequest(
                    age = age,
                    gender = gender,
                    height = height,
                    weight = weight,
                    goal = goal,
                    activityLevel = activityLevel
                )
                val response = authApi.setupConfig(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val responseData = response.body()?.data
                    val nutritionGoal = responseData?.nutritionGoal

                    val targetCalories = nutritionGoal?.dailyCalorieGoal ?: 2000
                    val targetCarbs = nutritionGoal?.dailyCarbsGoal ?: 0
                    val targetProteins = nutritionGoal?.dailyProteinGoal ?: 0
                    val targetFats = nutritionGoal?.dailyFatGoal ?: 0

                    // Persist to SessionManager
                    SessionManager.calories = targetCalories
                    SessionManager.carbs = targetCarbs
                    SessionManager.proteins = targetProteins
                    SessionManager.fats = targetFats

                    // Emit success to UI
                    _calculationSuccess.emit(ConfigResult(targetCalories, targetCarbs, targetProteins, targetFats))
                } else {
                    val errMsg = response.body()?.message ?: "Thiết lập cấu hình thất bại: Lỗi ${response.code()}"
                    // Emit error through base error flow
                    throw Exception(errMsg)
                }
            } catch (e: Exception) {
                // Let launchSafe handle and emit error
                throw e
            }
        }
    }
}
