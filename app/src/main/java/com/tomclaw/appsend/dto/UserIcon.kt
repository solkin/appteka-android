package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserIcon(
    @SerializedName("icon")
    val icon: String,
    @SerializedName("label")
    val label: Map<String, String>,
    @SerializedName("color")
    val color: String,
) : Parcelable
