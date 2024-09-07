package com.tomclaw.appsend.screen.installed.api

import com.google.gson.annotations.SerializedName

data class CheckUpdatesResponse(
    @SerializedName(value = "entries")
    val entries: List<UpdateEntity>?,
)
