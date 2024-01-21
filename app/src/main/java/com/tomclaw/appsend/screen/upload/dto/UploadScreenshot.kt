package com.tomclaw.appsend.screen.upload.dto

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UploadScreenshot(
    val scrId: String?,
    val original: Uri,
    val preview: Uri,
    val width: Int,
    val height: Int,
) : Parcelable
