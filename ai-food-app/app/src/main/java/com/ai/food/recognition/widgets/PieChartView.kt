package com.ai.food.recognition.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class PieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var carbsPercent: Float = 0f
    private var proteinPercent: Float = 0f
    private var fatPercent: Float = 0f

    private val colorCarbs = Color.parseColor("#4BBE4F")
    private val colorProtein = Color.parseColor("#F59E0B")
    private val colorFat = Color.parseColor("#EF4444")

    private val rectF = RectF()
    private var strokeWidthPx = 40f

    fun setData(carbs: Float, protein: Float, fat: Float) {
        val total = carbs + protein + fat
        if (total > 0) {
            carbsPercent = (carbs / total) * 360f
            proteinPercent = (protein / total) * 360f
            fatPercent = (fat / total) * 360f
        } else {
            carbsPercent = 0f
            proteinPercent = 0f
            fatPercent = 0f
        }
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        strokeWidthPx = w * 0.12f // Tùy chỉnh độ dày viền
        paint.strokeWidth = strokeWidthPx

        // Padding cho stroke
        val padding = strokeWidthPx / 2f
        rectF.set(padding, padding, w - padding, h - padding)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (carbsPercent == 0f && proteinPercent == 0f && fatPercent == 0f) {
            // Draw an empty circle if no data
            paint.color = Color.parseColor("#E2E8F0")
            canvas.drawArc(rectF, 0f, 360f, false, paint)
            return
        }

        var startAngle = -90f // Bắt đầu từ đỉnh

        // Vẽ Protein (Vàng)
        if (proteinPercent > 0) {
            paint.color = colorProtein
            canvas.drawArc(rectF, startAngle, proteinPercent, false, paint)
            startAngle += proteinPercent
        }

        // Vẽ Fat (Đỏ)
        if (fatPercent > 0) {
            paint.color = colorFat
            canvas.drawArc(rectF, startAngle, fatPercent, false, paint)
            startAngle += fatPercent
        }

        // Vẽ Carbs (Xanh)
        if (carbsPercent > 0) {
            paint.color = colorCarbs
            canvas.drawArc(rectF, startAngle, carbsPercent, false, paint)
        }
    }
}
