package com.tomclaw.appsend.screen.details.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class AppInfo(
    @SerializedName("app_id")
    val appId: String,
    @SerializedName("label")
    val label: String?,
    @SerializedName("labels")
    val labels: Map<String, String>?,
    @SerializedName("icon")
    val icon: String?,
    @SerializedName("file_status")
    val fileStatus: Int,
    @SerializedName("package")
    val packageName: String,
    @SerializedName("ver_name")
    val version: String,
    @SerializedName("ver_code")
    val versionCode: Int,
    @SerializedName("sdk_version")
    val sdkVersion: Int,
    @SerializedName("android")
    val androidVersion: String,
    @SerializedName("permissions")
    val permissions: List<String>?,
    @SerializedName("size")
    val size: Long,
    @SerializedName("downloads")
    val downloads: Int?,
    @SerializedName("download_time")
    val downloadTime: Long?,
    @SerializedName("favorites")
    val favorites: Int?,
    @SerializedName("time")
    val time: Long,
    @SerializedName("sha1")
    val sha1: String,
    @SerializedName("user_id")
    val userId: Int?,
    @SerializedName("user_name")
    val userName: String? = null,
    @SerializedName("user_icon")
    val userIcon: UserIcon?,
    @SerializedName("abi")
    val abi: List<String>?,
) : Parcelable

const val STATUS_NORMAL = 0
const val STATUS_UNLINKED = -1
const val STATUS_PRIVATE = -2
const val STATUS_MODERATION = -3
