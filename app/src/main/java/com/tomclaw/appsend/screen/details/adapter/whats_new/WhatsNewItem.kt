package com.tomclaw.appsend.screen.details.adapter.whats_new

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class WhatsNewItem(
    override val id: Long,
    val text: String,
) : Item, Parcelable
