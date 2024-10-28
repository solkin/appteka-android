package com.tomclaw.appsend.screen.subscribers.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubscriberEntity(
    @SerializedName("id")
    val rowId: Int,
    @SerializedName("time")
    val time: Long,
    @SerializedName("user")
    val user: UserBrief,
) : Parcelable
