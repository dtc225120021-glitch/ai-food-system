package com.ai.food.recognition.presentation.home

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.dto.FoodLogItem
import com.ai.food.recognition.databinding.ItemHomeFoodBinding
import com.ai.food.recognition.ext.loadImage
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HomeFoodAdapter(
    private val imageLoader: ImageLoader,
    private val onItemClick: (FoodLogItem) -> Unit
) : RecyclerView.Adapter<HomeFoodAdapter.FoodViewHolder>() {

    private val items = mutableListOf<FoodLogItem>()

    @SuppressLint("NotifyDataSetChanged")
    fun submitList(newItems: List<FoodLogItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        val binding = ItemHomeFoodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FoodViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    inner class FoodViewHolder(private val binding: ItemHomeFoodBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(item: FoodLogItem) {
            val totalCalo = item.foods?.sumOf { it.calories ?: 0 } ?: 0
            val foodNames = item.foods?.mapNotNull { it.name }?.joinToString(", ") ?: "Món ăn"

            binding.tvName.text = foodNames
            binding.tvCalo.text = "$totalCalo kcal"
            binding.tvCategory.text = item.category ?: "Bữa sáng"

            val dateStr = item.createdAt ?: ""
            binding.tvTime.text = formatTime(dateStr)

            val imageUrl = item.image ?: ""
            val fullImageUrl = if (imageUrl.startsWith("http")) {
                imageUrl
            } else {
                "${SessionManager.BASE_URL}$imageUrl"
            }
            binding.ivFood.loadImage(fullImageUrl, imageLoader)

            binding.root.setOnClickListener {
                onItemClick(item)
            }
        }

        private fun formatTime(isoString: String): String {
            if (isoString.isEmpty()) return "Hôm nay"
            return try {
                val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                parser.timeZone = TimeZone.getTimeZone("UTC")
                val date = parser.parse(isoString) ?: return "Hôm nay"

                val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                "Hôm nay, ${formatter.format(date)}"
            } catch (e: Exception) {
                try {
                    val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    parser.timeZone = TimeZone.getTimeZone("UTC")
                    val date = parser.parse(isoString) ?: return "Hôm nay"

                    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
                    "Hôm nay, ${formatter.format(date)}"
                } catch (e: Exception) {
                    "Hôm nay"
                }
            }
        }
    }
}
