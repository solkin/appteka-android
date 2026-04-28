package com.tomclaw.appsend.screen.ratings.adapter.rating

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.UserMark
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingItem(
    override val id: Long,
    val rateId: Int,
    val score: Int,
    val text: String?,
    val time: Long,
    val user: UserMark,
    val showRatingMenu: Boolean,
    var hasMore: Boolean = false,
    var hasError: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable
