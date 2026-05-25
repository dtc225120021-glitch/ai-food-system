package com.ai.food.recognition.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class RegisterResponse(
    // Adjust these fields according to the actual API response
    @SerializedName("message") val message: String? = null,
    @SerializedName("status") val status: Int? = null
)

data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("data") val data: TokenResponse,
)

data class TokenResponse(
    @SerializedName("token") val accessToken: String,
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)

data class RefreshTokenResponse(
    @SerializedName("data") val data: TokenResponse,
)
