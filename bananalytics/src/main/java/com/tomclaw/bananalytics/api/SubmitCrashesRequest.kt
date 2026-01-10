package com.tomclaw.bananalytics.api

import com.google.gson.annotations.SerializedName

data class SubmitCrashesRequest(
    @SerializedName("environment") val environment: Environment,
    @SerializedName("crashes") val crashes: List<CrashReport>,
)
