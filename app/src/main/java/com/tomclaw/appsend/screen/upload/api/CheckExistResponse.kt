package com.tomclaw.appsend.screen.upload.api

import android.os.Parcelable
import com.tomclaw.appsend.dto.AppEntity
import kotlinx.parcelize.Parcelize

@Parcelize
data class CheckExistResponse(
    val reassign: Boolean,
    val info: String?,
    val warning: String?,
    val error: String?,
    val file: AppEntity?,
) : Parcelable
