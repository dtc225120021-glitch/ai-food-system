package com.ai.food.recognition.data.remote.dto

import com.google.gson.annotations.SerializedName

data class DetectFoodRequest(
    @SerializedName("path") val path: String
)

data class DetectFoodResponse(
    @SerializedName("status") val status: Boolean,
    @SerializedName("image") val image: String?,
    @SerializedName("result") val result: List<FoodItem>?,
    @SerializedName("recent") val recent: RecentItem?
)

data class FoodItem(
    @SerializedName("name") val name: String?,
    @SerializedName("carbs") val carbs: Int?,
    @SerializedName("protein") val protein: Int?,
    @SerializedName("fat") val fat: Int?,
    @SerializedName("calories") val calories: Int?
)

data class RecentItem(
    @SerializedName("_id") val id: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("foods") val foods: List<RecentFoodItem>?,
    @SerializedName("userId") val userId: String?,
    @SerializedName("createBy") val createBy: String?,
    @SerializedName("createdAt") val createdAt: String?
)

data class RecentFoodItem(
    @SerializedName("_id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("carbs") val carbs: Int?,
    @SerializedName("protein") val protein: Int?,
    @SerializedName("fat") val fat: Int?,
    @SerializedName("calories") val calories: Int?
)

data class RecentResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<RecentItem>?
)

data class ConfirmFoodRequest(
    @SerializedName("recentId") val recentId: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("category") val category: String,
    @SerializedName("foods") val foods: List<FoodItem>
)

data class ConfirmFoodResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("data") val data: ConfirmedFoodData?
)

data class ConfirmedFoodData(
    @SerializedName("_id") val id: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("foods") val foods: List<FoodItem>?
)

data class FoodResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<FoodLogItem>?
)

data class FoodLogItem(
    @SerializedName("_id") val id: String?,
    @SerializedName("image") val image: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("foods") val foods: List<FoodItem>?,
    @SerializedName("createdAt") val createdAt: String?
)
