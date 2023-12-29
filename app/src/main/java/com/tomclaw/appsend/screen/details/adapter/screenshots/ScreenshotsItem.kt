package com.tomclaw.appsend.screen.details.adapter.screenshots

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class ScreenshotsItem(
    override val id: Long,
    val items: List<Screenshot>,
) : Item, Parcelable
