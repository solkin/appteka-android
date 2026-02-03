package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.screen.feed.FeedDirection
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class FeedResponse(
    @SerializedName("posts")
    val posts: List<PostEntity>,
    @SerializedName("offset_id")
    val offsetId: Int,
    @SerializedName("direction")
    val direction: FeedDirection,
)
