package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class FeedReactionResponse(
    @SerializedName("reactions")
    val reactions: Map<String, Reaction>
)

