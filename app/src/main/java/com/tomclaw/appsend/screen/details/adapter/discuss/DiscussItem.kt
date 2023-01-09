package com.tomclaw.appsend.screen.details.adapter.discuss

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class DiscussItem(
    override val id: Long,
    val msgCount: Int?,
) : Item, Parcelable
