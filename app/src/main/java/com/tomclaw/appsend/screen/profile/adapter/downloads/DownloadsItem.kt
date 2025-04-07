package com.tomclaw.appsend.screen.profile.adapter.downloads

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class DownloadsItem(
    override val id: Long,
    val count: Int,
) : Item, Parcelable
