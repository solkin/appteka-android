package com.tomclaw.appsend.screen.discuss.api

import com.google.gson.annotations.SerializedName

class TopicsResponse(
    @SerializedName("entries")
    val topics: List<TopicEntry>
)