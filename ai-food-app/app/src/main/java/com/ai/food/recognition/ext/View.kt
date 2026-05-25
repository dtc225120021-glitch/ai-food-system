package com.ai.food.recognition.ext

import android.view.View
import android.widget.ImageView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.viewbinding.ViewBinding
import coil.ImageLoader
import coil.imageLoader
import coil.load
import com.ai.food.recognition.R

inline fun <T : ViewBinding> withBinding(
    binding: T?,
    block: T.() -> Unit
) {
    binding?.let { block(it) }
}

fun View.applySystemBarsPadding(
    left: Boolean = true,
    top: Boolean = true,
    right: Boolean = true,
    bottom: Boolean = true,
) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { v, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

        val topPadding = if (top) bars.top else 0
        val bottomPadding = if (bottom) bars.bottom else 0
        val leftPadding = if (left) bars.left else 0
        val rightPadding = if (right) bars.right else 0

        v.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
        insets
    }
}

fun ImageView.loadImage(
    url: String?,
    placeholder: Int = R.drawable.ic_launcher_foreground,
    imageLoader: ImageLoader = context.imageLoader,
    error: Int = R.drawable.ic_launcher_foreground
) {
    this.load(data = url, imageLoader = imageLoader) {
        crossfade(true)
        placeholder(placeholder)
        error(error)
    }
}

fun View.visible() {
    this.isVisible = true
}

fun View.gone() {
    this.isVisible = false
}
