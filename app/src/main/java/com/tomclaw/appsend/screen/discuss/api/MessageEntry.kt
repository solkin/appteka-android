package com.tomclaw.appsend.screen.discuss.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.UserIcon

data class MessageEntry(
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_icon")
    val userIcon: UserIcon,
    @SerializedName("topic_id")
    val topicId: Int,
    @SerializedName("msg_id")
    val msgId: Int,
    @SerializedName("prev_msg_id")
    val prevMsgId: Int,
    @SerializedName("time")
    val time: Long,
    @SerializedName("type")
    val type: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("attachment")
    val attachment: String?,
    @SerializedName("incoming")
    val incoming: Boolean,
)
