package com.tomclaw.appsend.upload

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("app_id")
    val app_id: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("file_status")
    val fileStatus: Int,
)
