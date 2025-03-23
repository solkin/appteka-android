package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.user.api.UserBrief

data class SubscribePayload(
    @SerializedName("publisher")
    val publisher: UserBrief,
) : PostPayload
