package com.tomclaw.appsend.screen.upload.adapter.screenshots.adapter

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenshotImage(
    val uri: Uri,
    val width: Int,
    val height: Int,
) : Item, Parcelable {
    override fun getType(): Int = IMAGE
}

const val IMAGE: Int = 2
