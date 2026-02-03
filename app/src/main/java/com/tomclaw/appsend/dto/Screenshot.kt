package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
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
