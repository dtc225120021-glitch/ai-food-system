package com.ai.food.recognition.presentation.profile

import android.net.Uri
import com.ai.food.recognition.base.BaseViewModel
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.data.remote.dto.UpdateProfileRequest
import com.ai.food.recognition.domain.model.local.LocalStorage
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class ProfileDRE {
    data class State(
        val isLoading: Boolean = false,
        val email: String = "",
        val fullName: String = "",
        val avatar: String = "",
        val gender: String = "Male",
        val height: Double = 170.0,
        val weight: Double = 65.0,
        val goal: String = "",
        val targetCalories: Int = 2000
    )

    sealed class Intent {
        object LoadProfile : Intent()
        data class SaveProfile(
            val fullName: String,
            val gender: String,
            val height: Double,
            val weight: Double,
            val avatarUri: Uri?,
            val cacheDir: File,
            val contentResolver: android.content.ContentResolver
        ) : Intent()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
        data class ShowSuccess(val message: String) : Effect()
    }
}

class ProfileViewModel(
    private val dataStorage: LocalStorage,
    private val authApi: AuthApi
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(ProfileDRE.State())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<ProfileDRE.Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onIntent(intent: ProfileDRE.Intent) {
        when (intent) {
            is ProfileDRE.Intent.LoadProfile -> loadProfile()
            is ProfileDRE.Intent.SaveProfile -> saveProfile(
                intent.fullName,
                intent.gender,
                intent.height,
                intent.weight,
                intent.avatarUri,
                intent.cacheDir,
                intent.contentResolver
            )
        }
    }

    private fun loadProfile() {
        launchSafe(showLoading = true) {
            try {
                val response = authApi.getProfile()
                if (response.isSuccessful && response.body()?.success == true) {
                    val profileData = response.body()?.data
                    val user = profileData?.user
                    val config = profileData?.config

                    val email = user?.email ?: ""
                    val fullName = user?.fullName ?: ""
                    val avatar = user?.avatar ?: ""
                    val gender = config?.gender ?: "Male"
                    val height = config?.height ?: 170.0
                    val weight = config?.weight ?: 65.0

                    val goal = when (config?.goal) {
                        "Lose" -> "Giảm cân"
                        "Gain" -> "Tăng cân"
                        "Maintain" -> "Duy trì vóc dáng"
                        else -> config?.goal ?: "Chưa thiết lập"
                    }
                    val targetCalories = config?.dailyCalorieGoal ?: 2000

                    _uiState.update {
                        it.copy(
                            email = email,
                            fullName = fullName,
                            avatar = avatar,
                            gender = gender,
                            height = height,
                            weight = weight,
                            goal = goal,
                            targetCalories = targetCalories
                        )
                    }
                } else {
                    val errMsg = "Không thể tải thông tin: Lỗi ${response.code()}"
                    _uiEffect.emit(ProfileDRE.Effect.ShowError(errMsg))
                }
            } catch (e: Exception) {
                val errMsg = e.message ?: "Lỗi không xác định"
                _uiEffect.emit(ProfileDRE.Effect.ShowError(errMsg))
            }
        }
    }

    private fun saveProfile(
        fullName: String,
        gender: String,
        height: Double,
        weight: Double,
        avatarUri: Uri?,
        cacheDir: File,
        contentResolver: android.content.ContentResolver
    ) {
        launchSafe(showLoading = true) {
            try {
                var avatarPath: String? = null

                // 1. Upload new avatar if selected
                if (avatarUri != null) {
                    val file = getFileFromUri(avatarUri, cacheDir, contentResolver)
                    val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

                    val uploadResponse = authApi.uploadFile(body)
                    if (uploadResponse.isSuccessful && uploadResponse.body()?.success == true) {
                        avatarPath = uploadResponse.body()?.file?.path
                    } else {
                        val errMsg = uploadResponse.body()?.message ?: "Tải ảnh đại diện lên máy chủ thất bại"
                        _uiEffect.emit(ProfileDRE.Effect.ShowError(errMsg))
                        return@launchSafe
                    }
                }

                // 2. Save profile updates
                val request = UpdateProfileRequest(
                    fullName = fullName,
                    gender = gender,
                    height = height,
                    weight = weight,
                    avatar = avatarPath
                )
                val response = authApi.updateProfile(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _uiState.update {
                        it.copy(
                            fullName = fullName,
                            gender = gender,
                            height = height,
                            weight = weight,
                            avatar = avatarPath ?: it.avatar
                        )
                    }
                    _uiEffect.emit(ProfileDRE.Effect.ShowSuccess("Cập nhật thông tin thành công"))
                } else {
                    val errMsg = response.body()?.message ?: "Cập nhật thất bại: Lỗi ${response.code()}"
                    _uiEffect.emit(ProfileDRE.Effect.ShowError(errMsg))
                }
            } catch (e: Exception) {
                val errMsg = e.message ?: "Lỗi không xác định"
                _uiEffect.emit(ProfileDRE.Effect.ShowError(errMsg))
            }
        }
    }

    private fun getFileFromUri(uri: Uri, cacheDir: File, contentResolver: android.content.ContentResolver): File {
        val tempFile = File(cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return tempFile
    }
}
