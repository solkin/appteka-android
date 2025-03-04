package com.tomclaw.appsend.screen.feed.adapter.upload

import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
class UploadItem(
    override val id: Long,
    val time: Long,
    val screenshots: List<Screenshot>,
    val text: String,
    override val user: UserBrief,
    override var hasMore: Boolean = false,
    override var hasProgress: Boolean = false,
) : FeedItem
