package com.tomclaw.appsend.screen.profile.adapter.ratings

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.screen.profile.adapter.rating.RatingItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingsItem(
    override val id: Long,
    val count: Int,
    val items: List<RatingItem>,
) : Item, Parcelable
