package com.tomclaw.appsend.screen.feed.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Reaction(
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("count")
    val count: Int?,
    @SerializedName("active")
    val active: Boolean?,
) : Parcelable
