package com.ai.food.recognition.presentation.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.ai.food.recognition.databinding.ItemHistoryBinding
import com.ai.food.recognition.ext.loadImage

class HistoryAdapter(
    private val imageLoader: ImageLoader,
    private val onItemClick: (HistoryItem) -> Unit
) : ListAdapter<HistoryItem, HistoryAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryItem) {
            binding.apply {
                tvMainFood.text = item.mainFoodName
                tvDate.text = item.timestamp
                tvCalories.text = "${item.totalCalories} kcal"
                tvItemsCount.text = "Bao gồm ${item.foods.size} món"

                ivThumbnail.loadImage(
                    url = item.imageUrl,
                    imageLoader = imageLoader
                )

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<HistoryItem>() {
        override fun areItemsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: HistoryItem, newItem: HistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}
