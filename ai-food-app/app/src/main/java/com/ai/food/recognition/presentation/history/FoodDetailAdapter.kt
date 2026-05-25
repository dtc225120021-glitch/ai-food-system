package com.ai.food.recognition.presentation.history

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.TooltipCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ai.food.recognition.databinding.ItemScannedFoodBinding

class FoodDetailAdapter : ListAdapter<ScannedFood, FoodDetailAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemScannedFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemScannedFoodBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScannedFood) {
            binding.apply {
                tvFoodName.text = item.name
                tvCalories.text = "${item.calories} kcal"
                tvDetails.text = "Tinh bột: ${item.carbs}g | Đạm: ${item.protein}g | Béo: ${item.fat}g"
                
                // Read-only history view
                cbSelected.isChecked = item.isSelected
                cbSelected.isEnabled = true // Keep it enabled so it displays the active green color
                
                val tooltipText = if (item.isSelected) "Món ăn được chọn" else "Món ăn không được chọn"
                TooltipCompat.setTooltipText(cbSelected, tooltipText)
                
                @SuppressLint("ClickableViewAccessibility")
                cbSelected.setOnTouchListener { v, event ->
                    if (event.action == MotionEvent.ACTION_UP) {
                        v.performLongClick()
                    }
                    true // Consume touch event to make it read-only and prevent state changes
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ScannedFood>() {
        override fun areItemsTheSame(oldItem: ScannedFood, newItem: ScannedFood): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ScannedFood, newItem: ScannedFood): Boolean {
            return oldItem == newItem
        }
    }
}
