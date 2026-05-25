package com.ai.food.recognition.presentation.history

import androidx.recyclerview.widget.LinearLayoutManager
import coil.ImageLoader
import com.ai.food.recognition.base.BaseActivity
import com.ai.food.recognition.databinding.ActivityHistoryDetailBinding
import com.ai.food.recognition.ext.applySystemBarsPadding
import com.ai.food.recognition.ext.loadImage
import com.ai.food.recognition.ext.withBinding
import org.koin.android.ext.android.inject

class HistoryDetailActivity : BaseActivity<ActivityHistoryDetailBinding>() {

    override fun inflateBinding() = ActivityHistoryDetailBinding.inflate(layoutInflater)

    private val imageLoader: ImageLoader by inject()
    private lateinit var adapter: FoodDetailAdapter

    override fun enableApplySystemBarsPadding() {
        withBinding(binding) {
            main.applySystemBarsPadding(top = false)
        }
    }

    override fun initializeViews() {
        adapter = FoodDetailAdapter()
        
        withBinding(binding) {
            cardBack.setOnClickListener { finish() }
            ivBack.setOnClickListener { finish() }

            rvFoods.layoutManager = LinearLayoutManager(this@HistoryDetailActivity)
            rvFoods.adapter = adapter
        }

        // Get Data from Intent
        val historyItem = intent.getParcelableExtra<HistoryItem>("HISTORY_ITEM")
        if (historyItem != null) {
            populateData(historyItem)
        } else {
            finish()
        }
    }

    private fun populateData(item: HistoryItem) {
        withBinding(binding) {
            tvDate.text = item.timestamp
            tvTotalCalories.text = "${item.totalCalories} kcal"
            
            ivMainImage.loadImage(
                url = item.imageUrl,
                imageLoader = imageLoader
            )

            adapter.submitList(item.foods)
        }
    }

    override fun collectLaunchWhenStarted() {
        // Not used as data is passed directly via Intent for now
    }
}
