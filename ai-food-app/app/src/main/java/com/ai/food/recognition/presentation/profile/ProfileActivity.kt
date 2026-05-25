package com.ai.food.recognition.presentation.profile

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import coil.ImageLoader
import com.ai.food.recognition.R
import com.ai.food.recognition.base.BaseActivity
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.databinding.ActivityProfileBinding
import com.ai.food.recognition.ext.applySystemBarsPadding
import com.ai.food.recognition.ext.loadImage
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.presentation.config.UserConfigActivity
import com.ai.food.recognition.util.Notify
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileActivity : BaseActivity<ActivityProfileBinding>() {

    override fun inflateBinding() = ActivityProfileBinding.inflate(layoutInflater)

    private val viewModel: ProfileViewModel by viewModel()
    private val imageLoader: ImageLoader by inject()

    private var selectedGender: String = "Male"
    private var selectedAvatarUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedAvatarUri = it
            binding?.ivAvatar?.setImageURI(it)
        }
    }

    override fun enableApplySystemBarsPadding() {
        withBinding(binding) {
            main.applySystemBarsPadding(top = false)
        }
    }

    override fun initializeViews() {
        withBinding(binding) {
            cardBack.setOnClickListener { finish() }
            ivBack.setOnClickListener { finish() }

            layoutAvatar.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }
            cardAvatar.setOnClickListener {
                pickImageLauncher.launch("image/*")
            }

            cardMale.setOnClickListener {
                selectGender("Male")
            }
            cardFemale.setOnClickListener {
                selectGender("Female")
            }

            btnResetGoal.setOnClickListener {
                startActivity(Intent(this@ProfileActivity, UserConfigActivity::class.java))
            }

            btnSaveChanges.setOnClickListener {
                val fullName = edtFullName.text.toString().trim()
                val heightStr = edtHeight.text.toString().trim()
                val weightStr = edtWeight.text.toString().trim()

                if (fullName.isEmpty()) {
                    Notify.show(this@ProfileActivity, "Vui lòng nhập họ tên", Notify.Type.ERROR)
                    return@setOnClickListener
                }
                
                val height = heightStr.toDoubleOrNull() ?: 0.0
                val weight = weightStr.toDoubleOrNull() ?: 0.0

                if (height <= 0 || weight <= 0) {
                    Notify.show(this@ProfileActivity, "Chiều cao và cân nặng không hợp lệ", Notify.Type.ERROR)
                    return@setOnClickListener
                }

                viewModel.onIntent(
                    ProfileDRE.Intent.SaveProfile(
                        fullName = fullName,
                        gender = selectedGender,
                        height = height,
                        weight = weight,
                        avatarUri = selectedAvatarUri,
                        cacheDir = cacheDir,
                        contentResolver = contentResolver
                    )
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.onIntent(ProfileDRE.Intent.LoadProfile)
    }

    override fun onResume() {
        super.onResume()
        // Reload in case returned from UserConfigActivity
        viewModel.onIntent(ProfileDRE.Intent.LoadProfile)
    }

    private fun selectGender(gender: String) {
        selectedGender = gender
        val activeColor = ContextCompat.getColor(this, R.color.color_4BBE4F)
        val inactiveColor = "#E2E8F0".toColorInt()
        val labelActiveColor = ContextCompat.getColor(this, R.color.color_4BBE4F)
        val labelInactiveColor = "#64748B".toColorInt()

        binding?.let { b ->
            if (gender == "Male") {
                setCardSelectedState(b.cardMale, activeColor, true)
                setCardSelectedState(b.cardFemale, inactiveColor, false)

                b.tvMale.setTextColor(labelActiveColor)
                b.tvFemale.setTextColor(labelInactiveColor)
            } else {
                setCardSelectedState(b.cardFemale, activeColor, true)
                setCardSelectedState(b.cardMale, inactiveColor, false)

                b.tvFemale.setTextColor(labelActiveColor)
                b.tvMale.setTextColor(labelInactiveColor)
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

    override fun collectLaunchWhenStarted() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding?.let { b ->
                        b.edtEmail.setText(state.email)
                        // Only update text if not currently focused to avoid cursor jumping
                        if (!b.edtFullName.isFocused) {
                            b.edtFullName.setText(state.fullName)
                        }
                        if (!b.edtHeight.isFocused) {
                            val h = state.height
                            b.edtHeight.setText(if (h % 1.0 == 0.0) h.toInt().toString() else h.toString())
                        }
                        if (!b.edtWeight.isFocused) {
                            val w = state.weight
                            b.edtWeight.setText(if (w % 1.0 == 0.0) w.toInt().toString() else w.toString())
                        }
                        
                        selectGender(state.gender)
                        
                        b.tvCurrentGoal.text = state.goal
                        b.tvGoalDetails.text = "${state.targetCalories} kcal / ngày"

                        if (selectedAvatarUri == null && state.avatar.isNotEmpty()) {
                            val fullImageUrl = when {
                                state.avatar.startsWith("http") -> state.avatar
                                state.avatar.startsWith("/") -> "${SessionManager.BASE_URL.removeSuffix("/")}${state.avatar}"
                                else -> "${SessionManager.BASE_URL}${state.avatar}"
                            }
                            b.ivAvatar.loadImage(url = fullImageUrl, imageLoader = imageLoader)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is ProfileDRE.Effect.ShowError -> {
                            Notify.show(this@ProfileActivity, effect.message, Notify.Type.ERROR)
                        }
                        is ProfileDRE.Effect.ShowSuccess -> {
                            Notify.show(this@ProfileActivity, effect.message, Notify.Type.SUCCESS)
                            finish()
                        }
                    }
                }
            }
        }
    }
}
