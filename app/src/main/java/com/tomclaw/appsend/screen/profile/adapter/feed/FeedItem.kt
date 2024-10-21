package com.tomclaw.appsend.screen.profile.adapter.feed

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedItem(
    override val id: Long,
    val feedCount: Int,
    val subsCount: Int,
    val pubsCount: Int,
) : Item, Parcelable
