package com.tomclaw.appsend.categories

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
data class Category(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: Map<String, String>,
    @SerializedName("icon")
    val icon: String,
) : Parcelable
