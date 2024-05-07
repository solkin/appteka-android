package com.tomclaw.appsend.screen.ratings.adapter.rating

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingItem(
    override val id: Long,
    val rateId: Int,
    val score: Int,
    val text: String?,
    val time: Long,
    val userId: Int,
    val userName: String,
    val userIcon: UserIcon,
    var hasMore: Boolean = false,
    var hasError: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable
