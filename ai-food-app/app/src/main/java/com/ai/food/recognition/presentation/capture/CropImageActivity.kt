package com.ai.food.recognition.presentation.capture

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.ai.food.recognition.base.BaseActivity
import com.ai.food.recognition.databinding.ActivityCropImageBinding
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.presentation.result.ResultActivity
import com.ai.food.recognition.util.Notify
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CropImageActivity : BaseActivity<ActivityCropImageBinding>() {
    override fun inflateBinding() = ActivityCropImageBinding.inflate(layoutInflater)

    private val viewModel: CropImageViewModel by viewModel()

    override fun initializeViews() {
        super.initializeViews()

        val imageUri: Uri? = intent.data ?: (
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra("imageUri", Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra("imageUri")
            }
        )

        imageUri?.let {
            binding?.cropImageView?.setImageUriAsync(it)
        }

        withBinding(binding) {
            icDone.setOnClickListener {
                val croppedBitmap = cropImageView.getCroppedImage()
                if (croppedBitmap != null) {
                    viewModel.onIntent(
                        CropImageDRE.Intent.UploadImage(
                            bitmap = croppedBitmap,
                            cacheDir = cacheDir
                        )
                    )
                } else {
                    Notify.show(this@CropImageActivity, "Không thể cắt ảnh", Notify.Type.ERROR)
                }
            }
        }
    }

    override fun collectLaunchWhenStarted() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding?.layoutLoading?.isVisible = state.isLoading
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiEffect.collect { effect ->
                    when (effect) {
                        is CropImageDRE.Effect.NavigateToResult -> {
                            startActivity(
                                Intent(this@CropImageActivity, ResultActivity::class.java).apply {
                                    putExtra("imageUrl", effect.imagePath)
                                    putExtra("foodItemsJson", effect.foodItemsJson)
                                    putExtra("recentId", effect.recentId)
                                }
                            )
                            finish()
                        }
                        is CropImageDRE.Effect.ShowError -> {
                            Toast.makeText(this@CropImageActivity, effect.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}