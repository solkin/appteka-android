package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.screen.feed.FeedDirection

class FeedResponse(
    @SerializedName("posts")
    val posts: List<PostEntity>,
    val offsetId: Int,
    val direction: FeedDirection,
)
