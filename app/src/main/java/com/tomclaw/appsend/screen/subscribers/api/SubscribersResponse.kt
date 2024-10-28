package com.tomclaw.appsend.screen.subscribers.api

import com.google.gson.annotations.SerializedName

class SubscribersResponse(
    @SerializedName("entries")
    val entries: List<SubscriberEntity>
)
