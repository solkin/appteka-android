package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class RequestScanResponse(
    @SerializedName("scan_id")
    val scanId: Int,
    @SerializedName("file_id")
    val fileId: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String?,
) : Parcelable

