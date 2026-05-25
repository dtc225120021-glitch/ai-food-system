package com.ai.food.recognition.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.ai.food.recognition.R
import com.ai.food.recognition.databinding.ViewAiFoodNavigationBinding
import androidx.core.content.withStyledAttributes

class AIFoodMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    internal val colorSelected = ContextCompat.getColor(context, R.color.color_4BBE4F)
    internal val colorDefault = ContextCompat.getColor(context, R.color.color_9CA3AF)

    private val binding = ViewAiFoodNavigationBinding.inflate(LayoutInflater.from(context), this)

    init {
        attrs?.let {
            context.withStyledAttributes(it, R.styleable.AIFoodMenuView) {
                val icon = getResourceId(R.styleable.AIFoodMenuView_iconMenu, 0)
                val title = getString(R.styleable.AIFoodMenuView_titleMenu)
                if (icon != 0) {
                    binding.ivIcon.setImageResource(icon)
                }
                binding.tvTitle.text = title
                val padding = getDimensionPixelSize(R.styleable.AIFoodMenuView_iconPadding, 0)
                binding.ivIcon.setPadding(padding, padding, padding, padding)
            }
        }
    }

    fun selected() {
        binding.apply {
            tvTitle.setTextColor(colorSelected)
            ivIcon.setColorFilter(colorSelected)
        }
    }

    fun unselect() {
        binding.apply {
            tvTitle.setTextColor(colorDefault)
            ivIcon.setColorFilter(colorDefault)
        }
    }
}