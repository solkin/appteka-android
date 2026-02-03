package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class TextPayload(
    @SerializedName("screenshots")
    val screenshots: List<Screenshot>,
    @SerializedName("text")
    val text: String,
): PostPayload
