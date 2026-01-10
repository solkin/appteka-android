package com.tomclaw.bananalytics.api

import com.google.gson.annotations.SerializedName

data class Environment(
    @SerializedName("package_name") val packageName: String,
    @SerializedName("app_version") val appVersion: Long,
    @SerializedName("app_version_name") val appVersionName: String,
    @SerializedName("device_id") val deviceId: String,
    @SerializedName("os_version") val osVersion: Int,
    @SerializedName("manufacturer") val manufacturer: String,
    @SerializedName("model") val model: String,
    @SerializedName("country") val country: String,
    @SerializedName("language") val language: String,
)
