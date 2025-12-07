package com.tomclaw.appsend.screen.feed.adapter.text

import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
class TextItem(
    override val id: Long,
    val time: Long,
    val screenshots: List<Screenshot>,
    val text: String,
    override val user: UserBrief,
    override val actions: List<String>?,
    val reacts: List<com.tomclaw.appsend.screen.feed.api.Reaction>?,
    override var hasMore: Boolean = false,
    override var hasProgress: Boolean = false,
) : FeedItem
