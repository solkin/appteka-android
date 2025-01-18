package com.tomclaw.appsend.screen.reviews.adapter.review

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import kotlinx.parcelize.Parcelize

@Parcelize
class ReviewItem(
    override val id: Long,
    val appId: String,
    val rateId: Int,
    val icon: String?,
    val title: String,
    val version: String,
    val rating: Float,
    val text: String?,
    val time: Long,
    val showRatingMenu: Boolean,
    var hasMore: Boolean = false,
    var hasError: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable
