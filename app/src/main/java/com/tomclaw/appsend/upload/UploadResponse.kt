package com.tomclaw.appsend.upload

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadResponse(
    @SerializedName("app_id")
    val appId: String,
    @SerializedName("file_status")
    val fileStatus: Int,
): Parcelable
