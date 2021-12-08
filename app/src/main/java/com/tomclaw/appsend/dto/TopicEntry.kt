package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class TopicEntry(
    @SerializedName("topic_id")
    val topicId: Int,
    @SerializedName("type")
    val type: Int,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String?,
    @SerializedName("package")
    val packageName: String?,
    @SerializedName("pinned")
    val isPinned: Boolean,
    @SerializedName("read_msg_id")
    val readMsgId: Int?,
    @SerializedName("last_msg")
    val lastMsg: MessageEntity,
) : Parcelable
