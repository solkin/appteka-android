package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName

class FeedResponse(
    @SerializedName("posts")
    val posts: List<PostEntity>
)
