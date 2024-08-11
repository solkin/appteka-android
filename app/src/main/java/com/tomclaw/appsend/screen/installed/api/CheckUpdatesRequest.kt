package com.tomclaw.appsend.screen.installed.api

import com.google.gson.annotations.SerializedName

data class CheckUpdatesRequest(
    @SerializedName(value = "locale")
    private val locale: String,
    @SerializedName(value = "apps")
    private val apps: Map<String, Long>,
)