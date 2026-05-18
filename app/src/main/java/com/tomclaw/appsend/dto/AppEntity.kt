package com.tomclaw.appsend.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
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
    @SerializedName("abi")
    val abi: List<String>?,
    // Server-flagged when file_status=Private was caused by a moderator
    // rejection rather than the owner privatising it themselves. Only
    // populated on lists viewed by the file owner (e.g. /user/uploads);
    // defaults to false for older servers / foreign lists.
    @SerializedName("declined")
    val declined: Boolean = false,
) : Parcelable
