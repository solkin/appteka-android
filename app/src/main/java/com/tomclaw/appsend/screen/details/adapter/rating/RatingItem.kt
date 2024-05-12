package com.tomclaw.appsend.screen.details.adapter.rating

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class RatingItem(
    override val id: Long,
    val score: Int,
    val text: String?,
    val time: Long,
    val userId: Int,
    val userName: String,
    val userIcon: UserIcon,
) : Item, Parcelable
