package com.ai.food.recognition.base

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    protected var binding: VB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (isSplashScreenEnabled()) {
            val splashScreen = installSplashScreen()

            splashScreen.setKeepOnScreenCondition {
                // viewModel.isLoading.value
                return@setKeepOnScreenCondition false
            }
        }
        enableEdgeToEdge()
        binding = inflateBinding()
        setContentView(binding?.root)
        enableApplySystemBarsPadding()
        initializeViews()

        collectLaunchWhenStarted()
    }

    override fun onStart() {
        super.onStart()
        retrieveData()
    }

    open fun retrieveData() {}

    open fun collectLaunchWhenStarted() {}

    open fun isSplashScreenEnabled() = false

    abstract fun inflateBinding(): VB

    open fun enableApplySystemBarsPadding() {
        // Override if needed
    }

    open fun initializeViews() {}

    override fun onResume() {
        super.onResume()
        didUpdateViews()
    }

    open fun didUpdateViews() {}

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        binding = null
        super.onDestroy()
    }
}