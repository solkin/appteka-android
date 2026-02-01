package com.tomclaw.appsend.screen.topics.adapter.topic

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.UserIcon
import kotlinx.parcelize.Parcelize

@Parcelize
data class TopicItem(
    override val id: Long,
    val icon: String,
    val title: String,
    val description: String?,
    val packageName: String?,
    val isPinned: Boolean,
    val hasUnread: Boolean,
    val lastMsgId: Int,
    val lastMsgText: String,
    val lastMsgUserIcon: UserIcon,
    var hasMore: Boolean = false,
    var hasProgress: Boolean = false,
) : Item, Parcelable
