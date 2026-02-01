package com.tomclaw.appsend.screen.profile.adapter.reviews

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.screen.profile.adapter.review.ReviewItem
import kotlinx.parcelize.Parcelize

@Parcelize
data class ReviewsItem(
    override val id: Long,
    val count: Int,
    val items: List<ReviewItem>,
) : Item, Parcelable
