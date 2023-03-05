package com.tomclaw.appsend.screen.upload.adapter.open_source

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class OpenSourceItem(
    override val id: Long,
    val value: Boolean,
    val url: String,
) : Item, Parcelable
