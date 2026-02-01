package com.tomclaw.appsend.screen.profile.adapter.feed

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class FeedItem(
    override val id: Long,
    val feedCount: Int,
    val subsCount: Int,
    val pubsCount: Int,
) : Item, Parcelable
