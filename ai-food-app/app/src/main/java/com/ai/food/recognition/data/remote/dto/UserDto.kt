package com.ai.food.recognition.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ConfigResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: ConfigData? = null
)

data class ConfigData(
    @SerializedName("age") val age: Int?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("height") val height: Double?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("goal") val goal: String?,
    @SerializedName("activityLevel") val activityLevel: String?,
    @SerializedName("dailyCalorieGoal") val dailyCalorieGoal: Int?,
    @SerializedName("dailyProteinGoal") val dailyProteinGoal: Int?,
    @SerializedName("dailyFatGoal") val dailyFatGoal: Int?,
    @SerializedName("dailyCarbsGoal") val dailyCarbsGoal: Int?
)

data class ProfileResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ProfileData?
)

data class ProfileData(
    @SerializedName("user") val user: UserProfile?,
    @SerializedName("config") val config: UserConfig?,
    @SerializedName("nutritionGoal") val nutritionGoal: NutritionGoal?,
    @SerializedName("dailyReport") val dailyReport: DailyReportDto?
)

data class UserProfile(
    @SerializedName("_id") val id: String?,
    @SerializedName("full_name") val fullName: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("avatar") val avatar: String?,
    @SerializedName("createdAt") val createdAt: String?
)

data class UserConfig(
    @SerializedName("age") val age: Int?,
    @SerializedName("gender") val gender: String?,
    @SerializedName("height") val height: Double?,
    @SerializedName("weight") val weight: Double?,
    @SerializedName("goal") val goal: String?,
    @SerializedName("activityLevel") val activityLevel: String?,
    @SerializedName("dailyCalorieGoal") val dailyCalorieGoal: Int?
)

data class NutritionGoal(
    @SerializedName("_id") val id: String?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("dailyCalorieGoal") val dailyCalorieGoal: Int?,
    @SerializedName("dailyCarbsGoal") val dailyCarbsGoal: Int?,
    @SerializedName("dailyFatGoal") val dailyFatGoal: Int?,
    @SerializedName("dailyProteinGoal") val dailyProteinGoal: Int?
)

data class DailyReportDto(
    @SerializedName("userId") val userId: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("consumedCalories") val consumedCalories: Int?,
    @SerializedName("consumedProtein") val consumedProtein: Int?,
    @SerializedName("consumedFat") val consumedFat: Int?,
    @SerializedName("consumedCarbs") val consumedCarbs: Int?
)

data class SetupConfigRequest(
    @SerializedName("age") val age: Int,
    @SerializedName("gender") val gender: String,
    @SerializedName("height") val height: Double,
    @SerializedName("weight") val weight: Double,
    @SerializedName("goal") val goal: String,
    @SerializedName("activityLevel") val activityLevel: String
)

data class SetupConfigResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String? = null,
    @SerializedName("data") val data: SetupResponseData? = null
)

data class SetupResponseData(
    @SerializedName("dailyCalorieGoal") val dailyCalorieGoal: Int?,
    @SerializedName("nutritionGoal") val nutritionGoal: NutritionGoal?
)

data class UpdateProfileRequest(
    @SerializedName("fullName") val fullName: String,
    @SerializedName("gender") val gender: String,
    @SerializedName("height") val height: Double,
    @SerializedName("weight") val weight: Double,
    @SerializedName("avatar") val avatar: String? = null
)