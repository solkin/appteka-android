package com.tomclaw.appsend.screen.users.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@GsonModel
@Parcelize
data class PublisherEntity(
    @SerializedName("id")
    val rowId: Int,
    @SerializedName("time")
    val time: Long,
    @SerializedName("user")
    val user: UserBrief,
) : UserEntity, Parcelable