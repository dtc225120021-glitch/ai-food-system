package com.ai.food.recognition.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ai.food.recognition.ext.Constants
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

open class BaseViewModel : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    /**
     * Safe coroutine launcher
     */
    protected fun launchSafe(showLoading: Boolean = true, block: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                if (showLoading) _isLoading.value = true
                block()
            } catch (e: Exception) {
                _error.emit(e.message ?: Constants.UNKNOWN_ERROR)
            } catch (e: Throwable) {
                _error.emit(e.message ?: Constants.UNKNOWN_ERROR)
            } catch (e: Error) {
                _error.emit(Constants.UNKNOWN_ERROR)
            } finally {
                if (showLoading) _isLoading.value = false
            }
        }
    }
}