package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class RequestAIReviewResponse(
    @SerializedName("queued")
    val queued: Boolean,
    @SerializedName("message")
    val message: String?,
) : Parcelable
