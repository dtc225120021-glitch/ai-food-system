package com.ai.food.recognition.util

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.graphics.toColorInt
import com.ai.food.recognition.R
import com.ai.food.recognition.databinding.LayoutTopNotificationBinding
import java.lang.ref.WeakReference

object Notify {

    enum class Type {
        SUCCESS, WARNING, ERROR
    }

    private var activeNotificationViewRef: WeakReference<View>? = null

    @SuppressLint("DiscouragedApi", "InternalInsetResource")
    fun show(activity: Activity, message: String, type: Type) {
        if (activity.isFinishing || activity.isDestroyed) return

        dismissActiveNotification()

        val inflater = LayoutInflater.from(activity)

        val binding = LayoutTopNotificationBinding.inflate(inflater)
        val (colorHex, iconRes) = when (type) {
            Type.SUCCESS -> Pair("#4BBE4F", R.drawable.outline_check_24)
            Type.WARNING -> Pair("#F59E0B", R.drawable.ic_warning_24)
            Type.ERROR -> Pair("#EF4444", R.drawable.baseline_close_24)
        }

        binding.layoutNotificationContainer.setBackgroundColor(colorHex.toColorInt())
        binding.tvNotificationMessage.text = message
        binding.ivNotificationIcon.setImageResource(iconRes)

        val resourceId = activity.resources.getIdentifier("status_bar_height", "dimen", "android")
        val statusBarHeight = if (resourceId > 0) {
            activity.resources.getDimensionPixelSize(resourceId)
        } else {
            0
        }

        val dp6 = (2 * activity.resources.displayMetrics.density).toInt()
        val paddingTop = statusBarHeight + dp6
        binding.layoutNotificationContainer.setPadding(
            binding.layoutNotificationContainer.paddingStart,
            paddingTop,
            binding.layoutNotificationContainer.paddingEnd,
            binding.layoutNotificationContainer.paddingBottom
        )

        val decorView = activity.window.decorView as ViewGroup
        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.root.layoutParams = params
        decorView.addView(binding.root)

        activeNotificationViewRef = WeakReference(binding.root)

        binding.root.visibility = View.INVISIBLE
        binding.root.post {
            val height = binding.root.height.toFloat()
            binding.root.translationY = -height
            binding.root.visibility = View.VISIBLE

            binding.root.animate()
                .translationY(0f)
                .setDuration(400)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        val dismissRunnable = Runnable {
            if (binding.root.parent != null) {
                val height = binding.root.height.toFloat()
                binding.root.animate()
                    .translationY(-height)
                    .setDuration(400)
                    .setInterpolator(AccelerateInterpolator())
                    .withEndAction {
                        (binding.root.parent as? ViewGroup)?.removeView(binding.root)
                        if (activeNotificationViewRef?.get() == binding.root) {
                            activeNotificationViewRef = null
                        }
                    }
                    .start()
            }
        }
        binding.root.postDelayed(dismissRunnable, 3000)

        binding.layoutNotificationContainer.setOnClickListener {
            binding.layoutNotificationContainer.removeCallbacks(dismissRunnable)
            val height = binding.root.height.toFloat()
            binding.root.animate()
                .translationY(-height)
                .setDuration(300)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction {
                    (binding.root.parent as? ViewGroup)?.removeView(binding.root)
                    if (activeNotificationViewRef?.get() == binding.root) {
                        activeNotificationViewRef = null
                    }
                }
                .start()
        }
    }

    fun dismissActiveNotification() {
        val activeView = activeNotificationViewRef?.get()
        if (activeView != null) {
            activeView.animate().cancel()
            (activeView.parent as? ViewGroup)?.removeView(activeView)
            activeNotificationViewRef = null
        }
    }
}
