package com.tomclaw.appsend.screen.details.adapter.user_review

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class UserReviewItem(
    override val id: Long,
    val score: Int,
    val text: String?,
    val time: Long,
    val userId: Int,
    val userIcon: UserIcon,
    val userName: String?,
) : Item, Parcelable
