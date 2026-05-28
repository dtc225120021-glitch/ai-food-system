package com.ai.food.recognition.data.remote

import com.ai.food.recognition.data.remote.dto.ConfigResponse
import com.ai.food.recognition.data.remote.dto.RegisterRequest
import com.ai.food.recognition.data.remote.dto.RegisterResponse
import com.ai.food.recognition.data.remote.dto.LoginRequest
import com.ai.food.recognition.data.remote.dto.LoginResponse
import com.ai.food.recognition.data.remote.dto.RefreshTokenRequest
import com.ai.food.recognition.data.remote.dto.RefreshTokenResponse
import com.ai.food.recognition.data.remote.dto.ProfileResponse
import com.ai.food.recognition.data.remote.dto.UploadResponse
import com.ai.food.recognition.data.remote.dto.DetectFoodRequest
import com.ai.food.recognition.data.remote.dto.DetectFoodResponse
import com.ai.food.recognition.data.remote.dto.RecentResponse
import com.ai.food.recognition.data.remote.dto.SetupConfigRequest
import com.ai.food.recognition.data.remote.dto.SetupConfigResponse
import com.ai.food.recognition.data.remote.dto.UpdateProfileRequest
import com.ai.food.recognition.data.remote.dto.ConfirmFoodRequest
import com.ai.food.recognition.data.remote.dto.ConfirmFoodResponse
import com.ai.food.recognition.data.remote.dto.FoodResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part
import retrofit2.http.Query

interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/refresh-token")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<RefreshTokenResponse>

    @GET("user/config")
    suspend fun getConfig(): Response<ConfigResponse>

    @GET("user/profile")
    suspend fun getProfile(): Response<ProfileResponse>

    @Multipart
    @POST("upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part
    ): Response<UploadResponse>

    @POST("detect-food")
    suspend fun detectFood(
        @Body request: DetectFoodRequest
    ): Response<DetectFoodResponse>

    @GET("recents")
    suspend fun getRecents(): Response<RecentResponse>

    @POST("user/setup")
    suspend fun setupConfig(
        @Body request: SetupConfigRequest
    ): Response<SetupConfigResponse>

    @POST("user/update")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest
    ): Response<ConfigResponse>

    @POST("foods/confirm")
    suspend fun confirmFood(
        @Body request: ConfirmFoodRequest
    ): Response<ConfirmFoodResponse>

    @GET("foods")
    suspend fun getFoods(
        @Query("date") date: String? = null
    ): Response<FoodResponse>
}


