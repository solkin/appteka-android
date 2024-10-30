package com.tomclaw.appsend.screen.users.api

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
) : UserEntity, Parcelable