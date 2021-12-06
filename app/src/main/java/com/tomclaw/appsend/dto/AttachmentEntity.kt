package com.tomclaw.appsend.dto

import com.google.gson.annotations.SerializedName

data class AttachmentEntity(
    @SerializedName("preview_url")
    val previewUrl: String,
    @SerializedName("original_url")
    val originalUrl: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int
)
