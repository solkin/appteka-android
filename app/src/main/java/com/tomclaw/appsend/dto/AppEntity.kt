package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.categories.Category
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppEntity(
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
    @SerializedName("time")
    val time: Long,
    @SerializedName("size")
    val size: Long,
    @SerializedName("rating")
    val rating: Float,
    @SerializedName("downloads")
    val downloads: Int,
    @SerializedName("file_status")
    val status: Int,
    @SerializedName("category")
    val category: Category?,
    @SerializedName("exclusive")
    val exclusive: Boolean,
    @SerializedName("source_url")
    val sourceUrl: String?,
) : Parcelable
