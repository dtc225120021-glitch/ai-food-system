package com.ai.food.recognition.presentation.auth

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ai.food.recognition.base.BaseActivity
import com.ai.food.recognition.databinding.ActivityAuthBinding
import com.ai.food.recognition.ext.applySystemBarsPadding
import com.ai.food.recognition.ext.gone
import com.ai.food.recognition.ext.visible
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.presentation.main.MainActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthActivity : BaseActivity<ActivityAuthBinding>() {

    private val viewModel: AuthViewModel by viewModel()

    override fun inflateBinding() = ActivityAuthBinding.inflate(layoutInflater)

    fun goToHome() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    fun showLoading() {
        binding?.layoutLoading?.visible()
    }

    fun hideLoading() {
        binding?.layoutLoading?.gone()
    }

    override fun enableApplySystemBarsPadding() {
        withBinding(binding) {
            main.applySystemBarsPadding(top = false, bottom = true)
        }
    }

    override fun initializeViews() {
        super.initializeViews()

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect UI effects if added in the future
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        else -> {}
                    }
                }
            }
        }
    }
}
