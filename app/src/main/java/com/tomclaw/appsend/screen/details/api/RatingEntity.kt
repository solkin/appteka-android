package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.core.permissions.Capability
import com.tomclaw.appsend.dto.UserMark
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class RatingEntity(
    @SerializedName("rate_id")
    val rateId: Int,
    @SerializedName("score")
    val score: Int,
    @SerializedName("text")
    val text: String?,
    @SerializedName("time")
    val time: Long,
    @SerializedName("user")
    val user: UserMark,
    @SerializedName("capabilities")
    val capabilities: Map<String, Capability>? = null,
) : Parcelable
