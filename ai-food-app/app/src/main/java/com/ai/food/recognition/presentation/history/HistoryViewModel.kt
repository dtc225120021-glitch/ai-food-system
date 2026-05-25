package com.ai.food.recognition.presentation.history

import android.util.Log
import com.ai.food.recognition.base.BaseViewModel
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.AuthApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

class HistoryDRE {
    data class State(
        val isLoading: Boolean = false,
        val historyList: List<HistoryItem> = emptyList(),
        val error: String? = null
    )

    sealed class Intent {
        object LoadHistory : Intent()
    }
}

class HistoryViewModel(
    private val authApi: AuthApi
) : BaseViewModel() {

    private val _uiState = MutableStateFlow(HistoryDRE.State())
    val uiState = _uiState.asStateFlow()

    fun onIntent(intent: HistoryDRE.Intent) {
        when (intent) {
            is HistoryDRE.Intent.LoadHistory -> loadHistory()
        }
    }

    private fun loadHistory() {
        launchSafe(showLoading = false) {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = authApi.getRecents()
                if (response.isSuccessful && response.body()?.success == true) {
                    val recentsList = response.body()?.data ?: emptyList()
                    val mappedList = recentsList.map { recentItem ->
                        val totalCalories = recentItem.foods?.sumOf { it.calories ?: 0 } ?: 0
                        val mainFoodName = recentItem.foods?.mapNotNull { it.name }?.joinToString(" + ") ?: "Món ăn"
                        
                        val imageUrl = recentItem.image?.let { img ->
                            when {
                                img.startsWith("http") -> img
                                img.startsWith("/") -> "${SessionManager.BASE_URL.removeSuffix("/")}$img"
                                else -> "${SessionManager.BASE_URL}$img"
                            }
                        } ?: ""

                        val scannedFoods = recentItem.foods?.map { food ->
                            ScannedFood(
                                id = food.id ?: UUID.randomUUID().toString(),
                                name = food.name ?: "",
                                calories = food.calories ?: 0,
                                isSelected = true,
                                carbs = food.carbs ?: 0,
                                protein = food.protein ?: 0,
                                fat = food.fat ?: 0
                            )
                        } ?: emptyList()

                        HistoryItem(
                            id = recentItem.id ?: UUID.randomUUID().toString(),
                            timestamp = formatIsoDateTime(recentItem.createdAt),
                            imageUrl = imageUrl,
                            mainFoodName = mainFoodName,
                            totalCalories = totalCalories,
                            foods = scannedFoods
                        )
                    }

                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            historyList = mappedList
                        )
                    }
                    Log.d("HistoryViewModel", "Fetched recents successfully, items count: ${mappedList.size}")
                } else {
                    val errMsg = "Không thể tải lịch sử: Lỗi ${response.code()}"
                    _uiState.update { it.copy(isLoading = false, error = errMsg) }
                }
            } catch (e: Exception) {
                val errMsg = e.message ?: "Lỗi không xác định"
                _uiState.update { it.copy(isLoading = false, error = errMsg) }
            }
        }
    }

    private fun formatIsoDateTime(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return ""
        return try {
            val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getTimeZone("UTC")
            }
            val date = parser.parse(isoString)
            val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).apply {
                timeZone = java.util.TimeZone.getDefault()
            }
            if (date != null) formatter.format(date) else isoString
        } catch (e: Exception) {
            try {
                val parser = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val date = parser.parse(isoString)
                val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getDefault()
                }
                if (date != null) formatter.format(date) else isoString
            } catch (ex: Exception) {
                isoString
            }
        }
    }
}

