package com.tomclaw.appsend.events

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.MessageEntity

class EventsResponse(
    @SerializedName("time")
    val time: Long,
    @SerializedName("messages")
    val messages: List<MessageEntity>?,
    @SerializedName("deleted")
    val deleted: List<MessageEntity>?,
)
