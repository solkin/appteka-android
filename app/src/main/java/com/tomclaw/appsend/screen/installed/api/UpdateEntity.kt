package com.tomclaw.appsend.screen.installed.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
data class UpdateEntity(
    @SerializedName("app_id")
    val appId: String,
    @SerializedName("size")
    val size: Long,
    @SerializedName("time")
    val time: Long,
    @SerializedName("label")
    val title: String,
    @SerializedName("package")
    val packageName: String,
    @SerializedName("ver_name")
    val verName: String,
    @SerializedName("ver_code")
    val verCode: Int,
    @SerializedName("downloads")
    val downloads: Int,
    @SerializedName("user_id")
    val userId: String?,
    @SerializedName("icon")
    val icon: String?,
) : Parcelable
