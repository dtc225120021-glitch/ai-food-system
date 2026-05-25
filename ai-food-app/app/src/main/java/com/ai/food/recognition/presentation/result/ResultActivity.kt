package com.ai.food.recognition.presentation.result

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import coil.ImageLoader
import com.ai.food.recognition.base.BaseActivity
import com.ai.food.recognition.data.SessionManager
import com.ai.food.recognition.data.remote.AuthApi
import com.ai.food.recognition.data.remote.dto.ConfirmFoodRequest
import com.ai.food.recognition.data.remote.dto.FoodItem
import com.ai.food.recognition.databinding.ActivityResultBinding
import com.ai.food.recognition.databinding.ItemScannedFoodBinding
import com.ai.food.recognition.ext.loadImage
import com.ai.food.recognition.ext.withBinding
import com.ai.food.recognition.presentation.main.MainActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class ResultActivity : BaseActivity<ActivityResultBinding>() {
    override fun inflateBinding() = ActivityResultBinding.inflate(layoutInflater)

    private val imageLoader: ImageLoader by inject()
    private val authApi: AuthApi by inject()

    @SuppressLint("SetTextI18n")
    override fun initializeViews() {
        val imageUrl = intent.getStringExtra("imageUrl") ?: ""
        val foodItemsJson = intent.getStringExtra("foodItemsJson") ?: "[]"
        val recentId = intent.getStringExtra("recentId")

        val gson = Gson()
        val itemType = object : TypeToken<List<FoodItem>>() {}.type
        val foodItems: List<FoodItem> = gson.fromJson(foodItemsJson, itemType)

        val checkedMap = mutableMapOf<FoodItem, Boolean>()
        foodItems.forEach { checkedMap[it] = true }

        withBinding(binding) {
            val fullImageUrl = if (imageUrl.startsWith("http")) {
                imageUrl
            } else {
                "${SessionManager.BASE_URL.removeSuffix("/")}/$imageUrl".replace("([^:])//+".toRegex(), "$1/")
            }
            ivFoodImage.loadImage(url = fullImageUrl, imageLoader = imageLoader)

            // Setup Category exposed dropdown spinner
            val categories = arrayOf(
                "Bữa Sáng",
                "Bữa Phụ Sáng",
                "Bữa Trưa",
                "Bữa Xế Chiều",
                "Bữa Trước Tập",
                "Bữa Sau Tập",
                "Bữa Tối",
                "Bữa Đêm"
            )
            val categoryAdapter = ArrayAdapter(this@ResultActivity, android.R.layout.simple_dropdown_item_1line, categories)
            spinnerCategory.setAdapter(categoryAdapter)
            spinnerCategory.setText(categories[0], false)

            layoutFoodList.removeAllViews()

            foodItems.forEach { item ->
                val itemBinding =
                    ItemScannedFoodBinding.inflate(LayoutInflater.from(this@ResultActivity), layoutFoodList, false)

                itemBinding.tvFoodName.text = item.name ?: "Món ăn"
                itemBinding.tvCalories.text = "${item.calories ?: 0} kcal"
                itemBinding.tvDetails.text =
                    "Tinh bột: ${item.carbs ?: 0}g | Đạm: ${item.protein ?: 0}g | Béo: ${item.fat ?: 0}g"

                itemBinding.cbSelected.isChecked = checkedMap[item] == true

                val toggleChecked = {
                    val newState = !(checkedMap[item] ?: true)
                    checkedMap[item] = newState
                    itemBinding.cbSelected.isChecked = newState
                }

                itemBinding.root.setOnClickListener { toggleChecked() }
                itemBinding.cbSelected.setOnClickListener { toggleChecked() }

                layoutFoodList.addView(itemBinding.root)
            }

            ivClose.setOnClickListener {
                finish()
            }

            btnRegister.setOnClickListener {
                val selectedFoods = foodItems.filter { checkedMap[it] == true }
                if (selectedFoods.isEmpty()) {
                    Toast.makeText(this@ResultActivity, "Vui lòng chọn ít nhất 1 món ăn", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val selectedCategory = spinnerCategory.text.toString().trim().ifEmpty { "Bữa Sáng" }

                layoutLoading.isVisible = true

                lifecycleScope.launch {
                    try {
                        val request = ConfirmFoodRequest(
                            recentId = recentId,
                            image = imageUrl,
                            category = selectedCategory,
                            foods = selectedFoods
                        )
                        val response = authApi.confirmFood(request)
                        layoutLoading.isVisible = false

                        if (response.isSuccessful && response.body()?.success == true) {
                            var totalCarbs = 0
                            var totalProtein = 0
                            var totalFat = 0
                            var totalCalories = 0

                            selectedFoods.forEach { item ->
                                totalCarbs += item.carbs ?: 0
                                totalProtein += item.protein ?: 0
                                totalFat += item.fat ?: 0
                                totalCalories += item.calories ?: 0
                            }

                            // Sync details locally to SessionManager for instantaneous Home rendering
                            SessionManager.consumedCalories += totalCalories
                            SessionManager.consumedCarbs += totalCarbs
                            SessionManager.consumedProteins += totalProtein
                            SessionManager.consumedFats += totalFat

                            Toast.makeText(this@ResultActivity, "Xác nhận đăng ký món ăn thành công!", Toast.LENGTH_SHORT).show()

                            startActivity(
                                Intent(this@ResultActivity, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            )
                            finish()
                        } else {
                            val errMsg = response.body()?.message ?: "Xác nhận đăng ký món ăn thất bại"
                            Toast.makeText(this@ResultActivity, errMsg, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        layoutLoading.isVisible = false
                        Toast.makeText(this@ResultActivity, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}


