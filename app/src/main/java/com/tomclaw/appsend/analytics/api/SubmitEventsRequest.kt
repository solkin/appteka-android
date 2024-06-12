package com.tomclaw.appsend.analytics.api

import com.google.gson.annotations.SerializedName

data class SubmitEventsRequest(
    @SerializedName("environment") val environment: Environment,
    @SerializedName("events") val events: List<AnalyticsEvent>,
)
