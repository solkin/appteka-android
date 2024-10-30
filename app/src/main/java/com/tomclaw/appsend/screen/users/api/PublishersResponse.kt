package com.tomclaw.appsend.screen.users.api

import com.google.gson.annotations.SerializedName

class PublishersResponse(
    @SerializedName("entries")
    val entries: List<PublisherEntity>
)
