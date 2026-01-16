package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Developer(
    @SerializedName("is_official")
    val isOfficial: Boolean?,
) : Parcelable
