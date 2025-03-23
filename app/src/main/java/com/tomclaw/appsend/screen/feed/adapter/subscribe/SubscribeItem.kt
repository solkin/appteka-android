package com.tomclaw.appsend.screen.feed.adapter.subscribe

import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
class SubscribeItem(
    override val id: Long,
    val time: Long,
    val publisher: UserBrief,
    override val user: UserBrief,
    override var hasMore: Boolean = false,
    override var hasProgress: Boolean = false,
) : FeedItem
