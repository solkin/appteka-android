package com.tomclaw.appsend.screen.upload.api

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.tomclaw.appsend.dto.AppEntity
import com.tomclaw.appsend.screen.details.api.AppVersion
import com.tomclaw.appsend.screen.details.api.Meta
import com.tomclaw.appsend.util.GsonModel
import kotlinx.parcelize.Parcelize

@Parcelize
@GsonModel
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
    @SerializedName("meta")
    val meta: Meta?,
    @SerializedName("versions")
    val versions: List<AppVersion>?,
) : Parcelable
