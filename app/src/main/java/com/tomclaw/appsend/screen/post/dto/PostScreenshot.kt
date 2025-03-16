package com.tomclaw.appsend.screen.post.dto

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PostScreenshot(
    val scrId: String?,
    val original: Uri,
    val preview: Uri,
    val width: Int,
    val height: Int,
) : Parcelable
