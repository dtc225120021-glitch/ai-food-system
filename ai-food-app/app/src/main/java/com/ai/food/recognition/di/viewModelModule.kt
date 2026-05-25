package com.ai.food.recognition.di

import com.ai.food.recognition.presentation.auth.AuthViewModel
import com.ai.food.recognition.presentation.auth.ForgotPasswordViewModel
import com.ai.food.recognition.presentation.auth.LoginViewModel
import com.ai.food.recognition.presentation.auth.RegisterViewModel
import com.ai.food.recognition.presentation.config.UserConfigViewModel
import com.ai.food.recognition.presentation.main.MainViewModel
import com.ai.food.recognition.presentation.splash.SplashViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { UserConfigViewModel(get()) }
    viewModel { RegisterViewModel(get()) }
    viewModel { LoginViewModel(get(), get()) }
    viewModel { ForgotPasswordViewModel() }
    viewModel { AuthViewModel() }
    viewModel { SplashViewModel(get(), get()) }
    viewModel { com.ai.food.recognition.presentation.profile.ProfileViewModel(get(), get()) }
    viewModel { com.ai.food.recognition.presentation.history.HistoryViewModel(get()) }
    viewModel { com.ai.food.recognition.presentation.capture.CropImageViewModel(get()) }
}