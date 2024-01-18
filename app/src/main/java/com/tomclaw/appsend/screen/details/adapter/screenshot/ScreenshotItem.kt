package com.tomclaw.appsend.screen.details.adapter.screenshot

import android.net.Uri
import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenshotItem(
    override val id: Long,
    val uri: Uri,
    val width: Int,
    val height: Int,
) : Item, Parcelable
