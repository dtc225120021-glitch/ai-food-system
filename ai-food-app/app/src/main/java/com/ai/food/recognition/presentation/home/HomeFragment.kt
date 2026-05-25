package com.ai.food.recognition.presentation.home

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import com.ai.food.recognition.R
import com.ai.food.recognition.base.BaseFragment
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.dto.UserProfile
import com.ai.food.recognition.databinding.FragmentHomeBinding
import com.ai.food.recognition.ext.loadImage
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.presentation.config.UserConfigActivity
import com.ai.food.recognition.presentation.main.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.activityViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class HomeFragment : BaseFragment<FragmentHomeBinding>() {

    private val imageLoader: ImageLoader by inject()
    private val mainViewModel: MainViewModel by activityViewModel()

    override fun inflateBinding(layoutInflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(layoutInflater, container, false)

    override fun collectLaunchWhenStarted() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                mainViewModel.uiState.collect { state ->
                    if (state.userProfile != null || state.userConfig != null) {
                        renderProfile()
                    }
                }
            }
        }
    }

    override fun initializeViews() {
        withBinding(binding) {
            // Setup click listeners for target goals configuration
            cardConfigure.setOnClickListener {
                startActivity(Intent(requireContext(), UserConfigActivity::class.java))
            }
            ivConfigure.setOnClickListener {
                startActivity(Intent(requireContext(), UserConfigActivity::class.java))
            }

            // Setup click listeners for Profile
            cardAvatar.setOnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        com.ai.food.recognition.presentation.profile.ProfileActivity::class.java
                    )
                )
            }
            ivAvatar.setOnClickListener {
                startActivity(
                    Intent(
                        requireContext(),
                        com.ai.food.recognition.presentation.profile.ProfileActivity::class.java
                    )
                )
            }

            // Click listeners for Macronutrients Tooltips
            layoutCarbsClick.setOnClickListener {
                val target = if (SessionManager.carbs > 0) SessionManager.carbs else 400
                showMacroTooltip(
                    title = "Đường & Tinh bột (Carbohydrate)",
                    description = "Tinh bột là nguồn cung cấp năng lượng chính cho cơ thể hoạt động suốt cả ngày.",
                    consumed = SessionManager.consumedCarbs,
                    target = target,
                )
            }
            layoutProteinClick.setOnClickListener {
                val target = if (SessionManager.proteins > 0) SessionManager.proteins else 400
                showMacroTooltip(
                    title = "Chất đạm (Protein)",
                    description = "Protein giúp xây dựng và phát triển cơ bắp, hỗ trợ phục hồi các tế bào cơ thể.",
                    consumed = SessionManager.consumedProteins,
                    target = target,
                )
            }
            layoutFatClick.setOnClickListener {
                val target = if (SessionManager.fats > 0) SessionManager.fats else 400
                showMacroTooltip(
                    title = "Chất béo (Lipid / Fats)",
                    description = "Chất béo lành mạnh giúp hấp thụ vitamin tốt hơn và đóng vai trò quan trọng trong việc bảo vệ các cơ quan cơ thể.",
                    consumed = SessionManager.consumedFats,
                    target = target,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        renderProfile()
    }

    @SuppressLint("SetTextI18n")
    private fun renderProfile() {
        val dailyCalories = if (SessionManager.calories > 0) SessionManager.calories else 2000
        val dailyCarbs = if (SessionManager.carbs > 0) SessionManager.carbs else 400
        val dailyProteins = if (SessionManager.proteins > 0) SessionManager.proteins else 400
        val dailyFats = if (SessionManager.fats > 0) SessionManager.fats else 400

        val userName = mainViewModel.uiState.value.userProfile?.fullName
        val imageUrl = mainViewModel.uiState.value.userProfile?.avatar ?: ""
        val fullImageUrl = if (imageUrl.startsWith("http")) {
            imageUrl
        } else {
            "${SessionManager.BASE_URL}$imageUrl"
        }

        withBinding(binding) {
            tvUserName.text = userName
            ivAvatar.loadImage(
                url = fullImageUrl,
                imageLoader = imageLoader
            )
            tvTargetCalo.text = "/ %,d kcal".format(dailyCalories)
        }

        withBinding(binding) {
            layoutNoItem.isVisible = true

            val totalConsumed = SessionManager.consumedCalories
            val consumedProtein = SessionManager.consumedProteins
            val consumedFat = SessionManager.consumedFats
            val consumedCarbs = SessionManager.consumedCarbs

            tvTotalCalo.text = totalConsumed.toString()
            progressBarCircle.progress = ((totalConsumed / dailyCalories.toFloat()) * 100).toInt()

            progressCarbs.progress = ((consumedCarbs / dailyCarbs.toFloat()) * 100).toInt()

            progressProtein.progress = ((consumedProtein / dailyProteins.toFloat()) * 100).toInt()

            progressFat.progress = ((consumedFat / dailyFats.toFloat()) * 100).toInt()
        }
    }

    private fun showMacroTooltip(title: String, description: String, consumed: Int, target: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setBackground(ContextCompat.getDrawable(requireContext(), R.drawable.bg_shape_white_corner_radius_10))
            .setMessage(
                "$description\n\n" +
                        "• Đã tiêu thụ: $consumed g\n" +
                        "• Mục tiêu hằng ngày: $target g\n" +
                        "• Còn lại cần nạp: ${maxOf(0, target - consumed)} g"
            )
            .setPositiveButton("Đã hiểu") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}

