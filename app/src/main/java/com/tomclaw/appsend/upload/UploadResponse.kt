package com.tomclaw.appsend.upload

import com.google.gson.annotations.SerializedName

data class UploadResponse(
    @SerializedName("app_id")
    val appId: String,
    @SerializedName("file_status")
    val fileStatus: Int,
)
