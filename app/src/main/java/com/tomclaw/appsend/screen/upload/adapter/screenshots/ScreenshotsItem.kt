package com.tomclaw.appsend.screen.upload.adapter.screenshots

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.upload.adapter.screenshots.adapter.ScreenshotImage
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenshotsItem(
    override val id: Long,
    val items: List<ScreenshotImage>,
) : Item, Parcelable
