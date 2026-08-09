package com.tomclaw.appsend.core.content

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
data class ContentFlagsResponse(
    @SerializedName("flags")
    val flags: List<ContentFlag>,
) : Parcelable
