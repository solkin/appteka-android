package com.tomclaw.appsend.screen.topics.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.TopicEntry

class TopicsResponse(
    @SerializedName("entries")
    val topics: List<TopicEntry>
)