package com.ai.food.recognition.presentation.capture

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream

class CaptureViewModel : ViewModel() {



    fun bitmapToFile(context: Context, bitmap: Bitmap): File {

        val file = File(context.cacheDir, "image_${System.currentTimeMillis()}.jpg")

        val outputStream = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)

        outputStream.flush()
        outputStream.close()

        return file
    }
}