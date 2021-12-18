package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MessageEntity(
    @SerializedName("topic_id")
    val topicId: Int,
    @SerializedName("msg_id")
    val msgId: Int,
    @SerializedName("prev_msg_id")
    val prevMsgId: Int,
    @SerializedName("user_id")
    val userId: Int,
    @SerializedName("user_icon")
    val userIcon: UserIcon,
    @SerializedName("text")
    val text: String,
    @SerializedName("time")
    val time: Long,
    @SerializedName("cookie")
    val cookie: String?,
    @SerializedName("type")
    val type: Int,
    @SerializedName("attachment")
    val attachment: AttachmentEntity?,
    @SerializedName("incoming")
    val incoming: Boolean
) : Parcelable
