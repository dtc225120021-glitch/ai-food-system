package com.ai.food.recognition.presentation.main

import android.content.Intent
import androidx.activity.addCallback
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import com.ai.food.recognition.base.BaseActivity
import com.ai.food.recognition.databinding.ActivityMainBinding
import com.ai.food.recognition.ext.applySystemBarsPadding
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.presentation.capture.CaptureActivity
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.ai.food.recognition.R
import androidx.navigation.findNavController

class MainActivity : BaseActivity<ActivityMainBinding>() {

    override fun inflateBinding() = ActivityMainBinding.inflate(layoutInflater)

    private val viewModel: MainViewModel by viewModel()

    override fun isSplashScreenEnabled() = true

    override fun enableApplySystemBarsPadding() {
        withBinding(binding) {
            main.applySystemBarsPadding(top = false)
        }
    }

    override fun initializeViews() {
        selectHome()
        withBinding(binding) {
            aiFoodHome.setOnClickListener {
                if (findNavController(R.id.nav_host).currentDestination?.id != R.id.homeFragment) {
                    selectHome()
                    findNavController(R.id.nav_host).navigate(R.id.homeFragment)
                }
            }
            aiFoodHistory.setOnClickListener {
                if (findNavController(R.id.nav_host).currentDestination?.id != R.id.historyFragment) {
                    selectHistory()
                    findNavController(R.id.nav_host).navigate(R.id.historyFragment)
                }
            }
            btnAddImage.setOnClickListener {
                startActivity(Intent(this@MainActivity, CaptureActivity::class.java))
            }
        }
    }

    private fun selectHome() {
        withBinding(binding) {
            aiFoodHome.selected()
            aiFoodHistory.unselect()
        }
    }

    private fun selectHistory() {
        withBinding(binding) {
            aiFoodHome.unselect()
            aiFoodHistory.selected()
        }
    }

    override fun collectLaunchWhenStarted() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { _ ->

                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { _ ->

                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

    }
}