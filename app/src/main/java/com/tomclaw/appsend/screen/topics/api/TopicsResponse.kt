package com.tomclaw.appsend.screen.topics.api

import com.google.gson.annotations.SerializedName

class TopicsResponse(
    @SerializedName("entries")
    val topics: List<TopicEntry>
)