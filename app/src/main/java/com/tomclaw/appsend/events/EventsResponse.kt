package com.tomclaw.appsend.events

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.MessageEntity

class EventsResponse(
    @SerializedName("time")
    val time: Long,
    @SerializedName("sent")
    val sent: List<MessageEntity>?,
    @SerializedName("incoming")
    val incoming: List<MessageEntity>?,
    @SerializedName("deleted")
    val deleted: List<MessageEntity>?,
)
