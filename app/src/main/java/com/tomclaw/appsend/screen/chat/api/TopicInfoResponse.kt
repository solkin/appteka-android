package com.tomclaw.appsend.screen.chat.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class TopicInfoResponse(
    @SerializedName("topic")
    val topic: TopicEntity
)