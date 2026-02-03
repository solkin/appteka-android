package com.tomclaw.appsend.screen.moderation.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.util.GsonModel

@GsonModel
class ModerationResponse(
    @SerializedName("entries")
    val files: List<AppEntity>
)