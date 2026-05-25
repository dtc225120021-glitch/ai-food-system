package com.ai.food.recognition.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("file") val file: UploadedFile?
)

data class UploadedFile(
    @SerializedName("filename") val filename: String?,
    @SerializedName("path") val path: String?,
    @SerializedName("uploadedBy") val uploadedBy: String?,
    @SerializedName("_id") val id: String?,
    @SerializedName("createdAt") val createdAt: String?
)
