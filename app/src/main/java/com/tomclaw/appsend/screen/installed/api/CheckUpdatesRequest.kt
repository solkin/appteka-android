package com.tomclaw.appsend.screen.installed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class CheckUpdatesRequest(
    @SerializedName(value = "locale")
    private val locale: String,
    @SerializedName(value = "apps")
    private val apps: Map<String, Long>,
)