package com.ai.food.recognition.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "script")
data class Script(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val createAt: Long = System.currentTimeMillis(),
    val updateAt: Long = System.currentTimeMillis(),
)
