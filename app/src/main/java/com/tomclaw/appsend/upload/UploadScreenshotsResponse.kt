package com.tomclaw.appsend.upload

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadScreenshotsResponse(
    @SerializedName("scr_ids")
    val scrIds: List<String>,
) : Parcelable
