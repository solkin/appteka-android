package com.tomclaw.appsend.screen.installed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class CheckUpdatesResponse(
    @SerializedName(value = "entries")
    val entries: List<UpdateEntity>?,
)
