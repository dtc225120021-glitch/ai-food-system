package com.ai.food.recognition.presentation.capture

import android.graphics.Bitmap
import android.util.Log
import com.ai.food.recognition.base.BaseViewModel
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.data.remote.dto.DetectFoodRequest
import com.ai.food.recognition.data.remote.dto.UploadedFile
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream

class CropImageDRE {
    data class State(
        val isLoading: Boolean = false,
        val uploadSuccess: Boolean = false,
        val uploadedFile: UploadedFile? = null,
        val error: String? = null
    )

    sealed class Intent {
        data class UploadImage(val bitmap: Bitmap, val cacheDir: File) : Intent()
    }

    sealed class Effect {
        data class ShowError(val message: String) : Effect()
        data class NavigateToResult(
            val imagePath: String,
            val foodItemsJson: String,
            val recentId: String?
        ) : Effect()
    }
}

class CropImageViewModel(
    private val authApi: AuthApi
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(CropImageDRE.State())
    val uiState = _uiState.asStateFlow()

    private val _uiEffect = MutableSharedFlow<CropImageDRE.Effect>()
    val uiEffect = _uiEffect.asSharedFlow()

    fun onIntent(intent: CropImageDRE.Intent) {
        when (intent) {
            is CropImageDRE.Intent.UploadImage -> uploadImage(intent.bitmap, intent.cacheDir)
        }
    }

    private fun uploadImage(bitmap: Bitmap, cacheDir: File) {
        launchSafe(showLoading = false) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // 1. Save Bitmap to a temporary file
                val tempFile = File(cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
                val fos = FileOutputStream(tempFile)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
                fos.flush()
                fos.close()

                // 2. Prepare MultipartBody.Part
                val requestFile = tempFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                // 3. Execute API call
                val response = authApi.uploadFile(body)
                if (response.isSuccessful && response.body()?.success == true) {
                    val uploadedFile = response.body()?.file
                    _uiState.value = _uiState.value.copy(
                        uploadedFile = uploadedFile
                    )
                    uploadedFile?.path?.let { path ->
                        // Automatically proceed to food detection API
                        detectFood(path)
                    } ?: run {
                        val errMsg = "Upload failed: file path is empty"
                        _uiState.value = _uiState.value.copy(isLoading = false, error = errMsg)
                        _uiEffect.emit(CropImageDRE.Effect.ShowError(errMsg))
                    }
                } else {
                    val errMsg = response.body()?.message ?: "Upload failed: ${response.code()}"
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errMsg)
                    _uiEffect.emit(CropImageDRE.Effect.ShowError(errMsg))
                }
            } catch (e: Exception) {
                val errMsg = e.message ?: "Unknown error"
                _uiState.value = _uiState.value.copy(isLoading = false, error = errMsg)
                _uiEffect.emit(CropImageDRE.Effect.ShowError(errMsg))
            }
        }
    }

    private fun detectFood(path: String) {
        launchSafe(showLoading = false) {
            try {
                val response = authApi.detectFood(DetectFoodRequest(path))
                if (response.isSuccessful && response.body()?.status == true) {
                    val detectResponse = response.body()
                    val result = detectResponse?.result

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        uploadSuccess = true
                    )

                    val jsonList = Gson().toJson(result ?: emptyList<com.ai.food.recognition.data.remote.dto.FoodItem>())

                    _uiEffect.emit(
                        CropImageDRE.Effect.NavigateToResult(
                            imagePath = detectResponse?.image ?: path,
                            foodItemsJson = jsonList,
                            recentId = detectResponse?.recent?.id
                        )
                    )
                    Log.d("CropImageViewModel", "Food detection successful, json items length: ${jsonList.length}")
                } else {
                    val errMsg = "Phân tích món ăn thất bại"
                    _uiState.value = _uiState.value.copy(isLoading = false, error = errMsg)
                    _uiEffect.emit(CropImageDRE.Effect.ShowError(errMsg))
                }
            } catch (e: Exception) {
                val errMsg = e.message ?: "Unknown error"
                _uiState.value = _uiState.value.copy(isLoading = false, error = errMsg)
                _uiEffect.emit(CropImageDRE.Effect.ShowError(errMsg))
            }
        }
    }
}
