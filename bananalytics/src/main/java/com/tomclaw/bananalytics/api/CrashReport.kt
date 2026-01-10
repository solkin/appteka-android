package com.tomclaw.bananalytics.api

import com.google.gson.annotations.SerializedName

data class CrashReport(
    @SerializedName("session_id") val sessionId: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("thread") val threadName: String,
    @SerializedName("stacktrace") val stacktrace: String,
    @SerializedName("is_fatal") val isFatal: Boolean,
    @SerializedName("context") val context: Map<String, String> = emptyMap(),
    @SerializedName("breadcrumbs") val breadcrumbs: List<Breadcrumb> = emptyList()
)
