package com.tomclaw.appsend.screen.upload.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.details.api.AppVersion
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckExistResponse(
    @SerializedName("reassign")
    val reassign: Boolean,
    @SerializedName("info")
    val info: String?,
    @SerializedName("warning")
    val warning: String?,
    @SerializedName("error")
    val error: String?,
    @SerializedName("file")
    val file: AppEntity?,
    @SerializedName("versions")
    val versions: List<AppVersion>?,
) : Parcelable
