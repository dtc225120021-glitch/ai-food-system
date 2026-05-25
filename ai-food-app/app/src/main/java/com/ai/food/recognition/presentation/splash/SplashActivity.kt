package com.ai.food.recognition.presentation.splash

import android.annotation.SuppressLint
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ai.food.recognition.base.BaseActivity
import com.ai.food.recognition.databinding.ActivitySplashBinding
import com.ai.food.recognition.ext.applySystemBarsPadding
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.presentation.auth.AuthActivity
import com.ai.food.recognition.presentation.main.MainActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : BaseActivity<ActivitySplashBinding>() {

    private val viewModel: SplashViewModel by viewModel()

    override fun inflateBinding() = ActivitySplashBinding.inflate(layoutInflater)

    override fun enableApplySystemBarsPadding() {
        withBinding(binding) {
            main.applySystemBarsPadding(top = false)
        }
    }

    override fun initializeViews() {
        lifecycleScope.launch(Dispatchers.IO) {
            for (i in 0..100) {
                withContext(Dispatchers.Main) {
                    binding?.progressBarHorizontal?.progress = i
                }
                delay(20)
            }
            delay(200)

            viewModel.onIntent(SplashDRE.Intent.CheckToken)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is SplashDRE.Effect.NavigateToHome -> {
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        }
                        is SplashDRE.Effect.NavigateToAuth -> {
                            startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                            finish()
                        }
                    }
                }
            }
        }
    }
}