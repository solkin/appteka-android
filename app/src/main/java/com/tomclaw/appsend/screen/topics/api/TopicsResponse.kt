package com.tomclaw.appsend.screen.topics.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.TopicEntity

class TopicsResponse(
    @SerializedName("entries")
    val topics: List<TopicEntity>
)