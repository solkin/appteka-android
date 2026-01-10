package com.tomclaw.bananalytics.api

import com.google.gson.annotations.SerializedName

data class SubmitEventsRequest(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("environment") val environment: Environment,
    @SerializedName("events") val events: List<AnalyticsEvent>,
)
