package com.tomclaw.bananalytics.api

import com.google.gson.annotations.SerializedName

data class AnalyticsEvent(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("name") val name: String,
    @SerializedName("tags") val tags: Map<String, String>,
    @SerializedName("fields") val fields: Map<String, Double>,
    @SerializedName("time") val time: Long
)
