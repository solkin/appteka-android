package com.tomclaw.appsend.screen.feed.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostEntity(
    @SerializedName("id")
    val postId: Int,
    @SerializedName("time")
    val time: Long,
    @SerializedName("type")
    val type: Int,
    @SerializedName("payload")
    val payload: Unit,
    @SerializedName("user")
    val user: UserBrief,
) : Parcelable

const val TYPE_TEXT = 1
