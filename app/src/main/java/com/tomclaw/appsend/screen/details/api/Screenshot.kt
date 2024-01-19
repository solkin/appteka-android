package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Screenshot(
    @SerializedName("scr_id")
    val scrId: String,
    @SerializedName("original")
    val original: String,
    @SerializedName("preview")
    val preview: String,
    @SerializedName("width")
    val width: Int,
    @SerializedName("height")
    val height: Int,
) : Parcelable
