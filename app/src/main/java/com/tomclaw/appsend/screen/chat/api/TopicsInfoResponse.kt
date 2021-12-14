package com.tomclaw.appsend.screen.chat.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.TopicEntry

data class TopicsInfoResponse(
    @SerializedName("info")
    val info: TopicEntry
)