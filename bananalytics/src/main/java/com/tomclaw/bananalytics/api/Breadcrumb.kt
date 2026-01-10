package com.tomclaw.bananalytics.api

import com.google.gson.annotations.SerializedName

data class Breadcrumb(
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("message") val message: String,
    @SerializedName("category") val category: String
)

enum class BreadcrumbCategory {
    NAVIGATION,
    USER_ACTION,
    NETWORK,
    ERROR,
    CUSTOM;

    fun toApiValue(): String = name.lowercase()
}
