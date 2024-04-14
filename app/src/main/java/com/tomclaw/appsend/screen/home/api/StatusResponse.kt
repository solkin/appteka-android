package com.tomclaw.appsend.screen.home.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatusResponse(
    @SerializedName("block")
    val block: Boolean?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("message")
    val message: String?,
) : Parcelable