package com.tomclaw.appsend.screen.users.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

@GsonModel
class PublishersResponse(
    @SerializedName("entries")
    val entries: List<PublisherEntity>
)
