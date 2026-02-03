package com.tomclaw.appsend.screen.feed.api

import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.util.GsonModel

@GsonModel
data class UploadPayload(
    @SerializedName("app_id")
    val appId: String,
    @SerializedName("package")
    val packageName: String,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("label")
    val title: String,
    @SerializedName("ver_name")
    val verName: String,
    @SerializedName("ver_code")
    val verCode: Int,
    @SerializedName("size")
    val size: Long,
    @SerializedName("file_status")
    val status: Int,
    @SerializedName("category")
    val category: Category?,
    @SerializedName("exclusive")
    val exclusive: Boolean,
    @SerializedName("open_source")
    val openSource: Boolean,
    @SerializedName("description")
    val description: String?,
    @SerializedName("screenshots")
    val screenshots: List<Screenshot>?,
): PostPayload
