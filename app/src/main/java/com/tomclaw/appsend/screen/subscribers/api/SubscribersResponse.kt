package com.tomclaw.appsend.screen.subscribers.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity

class SubscribersResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)
