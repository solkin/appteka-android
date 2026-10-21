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
    @SerializedName("max_file_size")
    val maxFileSize: Long? = null,
    // The only origin this build can have — the package is bound to the
    // uploader. Absent when the choice is open.
    @SerializedName("origin_fixed")
    val originFixed: String? = null,
    // Why "own" is not open to this user, already localized.
    @SerializedName("own_denied")
    val ownDenied: String? = null,
) : Parcelable
