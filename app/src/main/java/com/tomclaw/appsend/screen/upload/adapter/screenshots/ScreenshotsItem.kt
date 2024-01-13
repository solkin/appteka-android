package com.tomclaw.appsend.screen.upload.adapter.screenshots

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

data class ScreenshotsItem(
    override val id: Long,
    val items: List<Item>,
) : Item
