package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName

data class FeedReactionResponse(
    @SerializedName("reactions")
    val reactions: Map<String, Reaction>
)

