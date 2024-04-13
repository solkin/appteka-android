package com.tomclaw.appsend.screen.home.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ModerationData(
    @SerializedName("moderator")
    val moderator: Boolean,
    @SerializedName("count")
    val count: Int,
) : Parcelable
