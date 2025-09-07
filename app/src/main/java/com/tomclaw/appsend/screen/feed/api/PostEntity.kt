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
    @SerializedName("reacts")
    val reacts: List<Reaction>?,
    @SerializedName("user")
    val user: UserBrief,
    @SerializedName("actions")
    val actions: List<String>?,
)

const val TYPE_TEXT = 1
const val TYPE_FAVORITE = 2
const val TYPE_UPLOAD = 3
const val TYPE_SUBSCRIBE = 4

const val ACTION_DELETE = "delete"
