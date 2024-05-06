package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

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
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_icon")
    val userIcon: UserIcon,
    @SerializedName("user_name")
    val userName: String?,
) : Parcelable
