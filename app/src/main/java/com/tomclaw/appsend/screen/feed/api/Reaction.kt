package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName

data class Reaction(
    @SerializedName("id")
    val id: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("count")
    val count: Int?,
    @SerializedName("active")
    val active: Boolean?,
)
