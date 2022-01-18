package com.tomclaw.appsend.events

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.TopicEntity

class EventsResponse(
    @SerializedName("time")
    val time: Long,
    @SerializedName("messages")
    val messages: List<MessageEntity>?,
    @SerializedName("topics")
    val topics: List<TopicEntity>?,
    @SerializedName("deleted")
    val deleted: List<MessageEntity>?,
)
