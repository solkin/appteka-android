package com.tomclaw.appsend.screen.moderation.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity

class ModerationResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)