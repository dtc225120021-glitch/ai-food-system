package com.ai.food.recognition.presentation.config

import android.content.res.ColorStateList
import android.graphics.Color
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ai.food.recognition.R
import com.ai.food.recognition.base.BaseActivity
import com.ai.food.recognition.databinding.ActivityUserConfigBinding
import com.ai.food.recognition.ext.applySystemBarsPadding
import com.ai.food.recognition.ext.withBinding
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.graphics.toColorInt

class UserConfigActivity : BaseActivity<ActivityUserConfigBinding>() {

    override fun inflateBinding() = ActivityUserConfigBinding.inflate(layoutInflater)

    private val viewModel: UserConfigViewModel by viewModel()

    private var selectedGender: String? = null
    private var selectedGoal: String? = null

    override fun enableApplySystemBarsPadding() {
        withBinding(binding) {
            main.applySystemBarsPadding(top = false)
        }
    }

    override fun initializeViews() {
        withBinding(binding) {
            // Setup toolbar back navigation
            cardBack.setOnClickListener { finish() }
            ivBack.setOnClickListener { finish() }

            // Populate Activity Levels exposed dropdown
            val activityLevels = arrayOf(
                "Không vận động", "Cường độ nhẹ", "Vận động vừa phải", "Cường độ cao"
            )
            val adapter =
                ArrayAdapter(this@UserConfigActivity, android.R.layout.simple_dropdown_item_1line, activityLevels)
            spinnerActivityLevel.setAdapter(adapter)

            // Gender selection clicks
            cardMale.setOnClickListener {
                selectGender("Male")
            }
            cardFemale.setOnClickListener {
                selectGender("Female")
            }

            // Goal selection clicks
            cardGoalLose.setOnClickListener {
                selectGoal("Lose")
            }
            cardGoalMaintain.setOnClickListener {
                selectGoal("Maintain")
            }
            cardGoalGain.setOnClickListener {
                selectGoal("Gain")
            }

            // Primary save button click
            btnSaveConfig.setOnClickListener {
                validateAndSave()
            }
        }
        viewModel.loadUserConfig()
    }

    private fun selectGender(gender: String) {
        val isMale = gender.equals("Male", ignoreCase = true)
        selectedGender = if (isMale) "Male" else "Female"
        val activeColor = ContextCompat.getColor(this, R.color.color_4BBE4F)
        val inactiveColor = "#E2E8F0".toColorInt()
        val labelActiveColor = ContextCompat.getColor(this, R.color.color_4BBE4F)
        val labelInactiveColor = "#64748B".toColorInt()

        binding?.let { b ->
            if (isMale) {
                setCardSelectedState(b.cardMale, activeColor, true)
                setCardSelectedState(b.cardFemale, inactiveColor, false)

                b.ivMale.imageTintList = ColorStateList.valueOf(labelActiveColor)
                b.tvMale.setTextColor(labelActiveColor)

                b.ivFemale.imageTintList = ColorStateList.valueOf(labelInactiveColor)
                b.tvFemale.setTextColor(labelInactiveColor)
            } else {
                setCardSelectedState(b.cardFemale, activeColor, true)
                setCardSelectedState(b.cardMale, inactiveColor, false)

                b.ivFemale.imageTintList = ColorStateList.valueOf(labelActiveColor)
                b.tvFemale.setTextColor(labelActiveColor)

                b.ivMale.imageTintList = ColorStateList.valueOf(labelInactiveColor)
                b.tvMale.setTextColor(labelInactiveColor)
            }
        }
    }

    private fun selectGoal(goal: String) {
        val mappedGoal = when {
            goal.equals("Lose", ignoreCase = true) -> "Lose"
            goal.equals("Gain", ignoreCase = true) -> "Gain"
            else -> "Maintain"
        }
        selectedGoal = mappedGoal
        val activeColor = ContextCompat.getColor(this, R.color.color_4BBE4F)
        val inactiveColor = "#E2E8F0".toColorInt()
        val activeTextColor = ContextCompat.getColor(this, R.color.color_4BBE4F)
        val inactiveTextColor = "#64748B".toColorInt()

        binding?.let { b ->
            // Reset all first
            setCardSelectedState(b.cardGoalLose, inactiveColor, false)
            setCardSelectedState(b.cardGoalMaintain, inactiveColor, false)
            setCardSelectedState(b.cardGoalGain, inactiveColor, false)

            b.ivGoalLose.imageTintList = ColorStateList.valueOf(inactiveTextColor)
            b.tvGoalLoseTitle.setTextColor(Color.BLACK)
            b.tvGoalLoseDesc.setTextColor(inactiveTextColor)

            b.ivGoalMaintain.imageTintList = ColorStateList.valueOf(inactiveTextColor)
            b.tvGoalMaintainTitle.setTextColor(Color.BLACK)
            b.tvGoalMaintainDesc.setTextColor(inactiveTextColor)

            b.ivGoalGain.imageTintList = ColorStateList.valueOf(inactiveTextColor)
            b.tvGoalGainTitle.setTextColor(Color.BLACK)
            b.tvGoalGainDesc.setTextColor(inactiveTextColor)

            // Select active one
            when (mappedGoal) {
                "Lose" -> {
                    setCardSelectedState(b.cardGoalLose, activeColor, true)
                    b.ivGoalLose.imageTintList = ColorStateList.valueOf(activeTextColor)
                    b.tvGoalLoseTitle.setTextColor(activeTextColor)
                    b.tvGoalLoseDesc.setTextColor(activeTextColor)
                }

                "Maintain" -> {
                    setCardSelectedState(b.cardGoalMaintain, activeColor, true)
                    b.ivGoalMaintain.imageTintList = ColorStateList.valueOf(activeTextColor)
                    b.tvGoalMaintainTitle.setTextColor(activeTextColor)
                    b.tvGoalMaintainDesc.setTextColor(activeTextColor)
                }

                "Gain" -> {
                    setCardSelectedState(b.cardGoalGain, activeColor, true)
                    b.ivGoalGain.imageTintList = ColorStateList.valueOf(activeTextColor)
                    b.tvGoalGainTitle.setTextColor(activeTextColor)
                    b.tvGoalGainDesc.setTextColor(activeTextColor)
                }
            }
        }
    }

    private fun setCardSelectedState(card: MaterialCardView, strokeColor: Int, isSelected: Boolean) {
        card.strokeColor = strokeColor
        card.strokeWidth = if (isSelected) {
            resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._2sdp)
        } else {
            resources.getDimensionPixelSize(com.intuit.sdp.R.dimen._1sdp)
        }
    }

    private fun validateAndSave() {
        val heightStr = binding?.edtHeight?.text?.toString()?.trim()
        val weightStr = binding?.edtWeight?.text?.toString()?.trim()
        val ageStr = binding?.edtAge?.text?.toString()?.trim()
        val selectedActivityLevel = binding?.spinnerActivityLevel?.text?.toString()?.trim()

        if (selectedGender == null) {
            Toast.makeText(this, "Vui lòng chọn giới tính", Toast.LENGTH_SHORT).show()
            return
        }
        if (heightStr.isNullOrEmpty()) {
            binding?.edtHeight?.error = "Vui lòng nhập chiều cao"
            return
        }
        if (weightStr.isNullOrEmpty()) {
            binding?.edtWeight?.error = "Vui lòng nhập cân nặng"
            return
        }
        if (ageStr.isNullOrEmpty()) {
            binding?.edtAge?.error = "Vui lòng nhập tuổi"
            return
        }
        if (selectedActivityLevel.isNullOrEmpty() || selectedActivityLevel == "Chọn cường độ") {
            Toast.makeText(this, "Vui lòng chọn cường độ tập thể dục", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedGoal == null) {
            Toast.makeText(this, "Vui lòng chọn mục tiêu vóc dáng", Toast.LENGTH_SHORT).show()
            return
        }

        val height = heightStr.toDoubleOrNull() ?: 0.0
        val weight = weightStr.toDoubleOrNull() ?: 0.0
        val age = ageStr.toIntOrNull() ?: 0

        if (height !in 50.0..250.0) {
            binding?.edtHeight?.error = "Chiều cao không hợp lệ"
            return
        }
        if (weight !in 20.0..300.0) {
            binding?.edtWeight?.error = "Cân nặng không hợp lệ"
            return
        }
        if (age !in 10..120) {
            binding?.edtAge?.error = "Độ tuổi không hợp lệ"
            return
        }

        viewModel.calculateAndSaveConfig(
            gender = selectedGender!!,
            weight = weight,
            height = height,
            age = age,
            goal = selectedGoal!!,
            activityLevel = selectedActivityLevel
        )
    }

    override fun collectLaunchWhenStarted() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    binding?.layoutLoading?.isVisible = isLoading
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.calculationSuccess.collect { result ->
                    showResultView(result)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.configLoaded.collect { config ->
                    fillConfigData(config)
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { errorMsg ->
                    Toast.makeText(this@UserConfigActivity, "Lỗi: $errorMsg", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fillConfigData(config: com.ai.food.recognition.data.remote.dto.ConfigData) {
        binding?.let { b ->
            b.edtAge.setText(config.age?.toString() ?: "")
            b.edtHeight.setText(formatDoubleValue(config.height))
            b.edtWeight.setText(formatDoubleValue(config.weight))

            config.gender?.let { selectGender(it) }
            config.goal?.let { selectGoal(it) }

            config.activityLevel?.let {
                b.spinnerActivityLevel.setText(it, false)
            }
        }
    }

    private fun formatDoubleValue(value: Double?): String {
        if (value == null) return ""
        return if (value % 1.0 == 0.0) value.toInt().toString() else value.toString()
    }

    private fun showResultView(result: ConfigResult) {
        binding?.let { b ->
            // Hide the input forms
            b.layoutSubmit.isVisible = false
            b.layoutGrid2.parent.let { parentView ->
                (parentView as? android.view.View)?.parent?.let { scrollView ->
                    (scrollView as? android.view.View)?.isVisible = false
                }
            }

            // Also hide tvToolbarTitle temporarily or change it
            b.tvToolbarTitle.text = "Kết quả"

            // Show result layout
            b.layoutResult.isVisible = true

            // Set data
            b.tvTotalCalories.text = result.calories.toString()
            b.tvCarbsValue.text = "${result.carbs}g"
            b.tvProteinValue.text = "${result.protein}g"
            b.tvFatValue.text = "${result.fats}g"

            // Convert macros to calories for pie chart proportion
            val carbsCal = result.carbs * 4f
            val proteinCal = result.protein * 4f
            val fatCal = result.fats * 9f
            b.pieChart.setData(carbsCal, proteinCal, fatCal)

            // Setup Done button
            b.btnDone.setOnClickListener {
                Toast.makeText(this, "Thiết lập mục tiêu thành công!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
