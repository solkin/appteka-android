package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.user.api.UserBrief
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class SubscribePayload(
    @SerializedName("publisher")
    val publisher: UserBrief,
) : PostPayload
