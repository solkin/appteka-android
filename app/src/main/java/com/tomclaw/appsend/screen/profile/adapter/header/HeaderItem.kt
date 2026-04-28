package com.tomclaw.appsend.screen.profile.adapter.header

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.Badge
import com.tomclaw.appsend.dto.BadgeMark
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class HeaderItem(
    override val id: Long,
    val userName: String?,
    val userEmail: String?,
    val userBio: String?,
    val userIcon: UserIcon,
    val joinTime: Long,
    val lastSeen: Long,
    val role: Int,
    val isRegistered: Boolean,
    val isVerified: Boolean,
    val isSelf: Boolean,
    val isSubscribed: Boolean,
    val nameRegex: String?,
    val primaryBadge: BadgeMark? = null,
    val badges: List<Badge>? = null,
) : Item, Parcelable
