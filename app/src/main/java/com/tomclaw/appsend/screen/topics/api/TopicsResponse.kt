package com.tomclaw.appsend.screen.topics.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.TopicEntity

class TopicsResponse(
    @SerializedName("has_more")
    val hasMore: Boolean,
    @SerializedName("entries")
    val topics: List<TopicEntity>
)