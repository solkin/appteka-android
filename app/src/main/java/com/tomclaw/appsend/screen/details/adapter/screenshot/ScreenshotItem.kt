package com.tomclaw.appsend.screen.details.adapter.screenshot

import android.net.Uri
import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenshotItem(
    override val id: Long,
    val original: Uri,
    val preview: Uri,
    val width: Int,
    val height: Int,
) : Item, Parcelable
