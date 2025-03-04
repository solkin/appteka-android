package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.user.api.UserBrief

data class PostEntity(
    @SerializedName("id")
    val postId: Int,
    @SerializedName("time")
    val time: Long,
    @SerializedName("type")
    val type: Int,
    @SerializedName("payload")
    val payload: PostPayload,
    @SerializedName("user")
    val user: UserBrief,
)

const val TYPE_TEXT = 1
const val TYPE_FAVORITE = 2
const val TYPE_UPLOAD = 3
