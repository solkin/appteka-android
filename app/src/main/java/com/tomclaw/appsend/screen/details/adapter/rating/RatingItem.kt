package com.tomclaw.appsend.screen.details.adapter.rating

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.UserMark
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingItem(
    override val id: Long,
    val score: Int,
    val text: String?,
    val time: Long,
    val user: UserMark,
) : Item, Parcelable
