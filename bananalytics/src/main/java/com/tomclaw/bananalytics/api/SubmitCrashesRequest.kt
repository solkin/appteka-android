package com.tomclaw.bananalytics.api

import com.google.gson.annotations.SerializedName

data class SubmitCrashesRequest(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("environment") val environment: Environment,
    @SerializedName("crashes") val crashes: List<CrashReport>,
)
