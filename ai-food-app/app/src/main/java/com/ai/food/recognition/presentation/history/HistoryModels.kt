package com.ai.food.recognition.presentation.history

import android.os.Parcel
import android.os.Parcelable

data class ScannedFood(
    val id: String,
    val name: String,
    val calories: Int,
    val isSelected: Boolean,
    val carbs: Int = 0,
    val protein: Int = 0,
    val fat: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readByte() != 0.toByte(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeInt(calories)
        parcel.writeByte(if (isSelected) 1 else 0)
        parcel.writeInt(carbs)
        parcel.writeInt(protein)
        parcel.writeInt(fat)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ScannedFood> {
        override fun createFromParcel(parcel: Parcel): ScannedFood {
            return ScannedFood(parcel)
        }

        override fun newArray(size: Int): Array<ScannedFood?> {
            return arrayOfNulls(size)
        }
    }
}


data class HistoryItem(
    val id: String,
    val timestamp: String,
    val imageUrl: String,
    val mainFoodName: String,
    val totalCalories: Int,
    val foods: List<ScannedFood>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.createTypedArrayList(ScannedFood.CREATOR) ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(timestamp)
        parcel.writeString(imageUrl)
        parcel.writeString(mainFoodName)
        parcel.writeInt(totalCalories)
        parcel.writeTypedList(foods)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<HistoryItem> {
        override fun createFromParcel(parcel: Parcel): HistoryItem {
            return HistoryItem(parcel)
        }

        override fun newArray(size: Int): Array<HistoryItem?> {
            return arrayOfNulls(size)
        }
    }
}
