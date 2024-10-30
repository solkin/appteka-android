package com.tomclaw.appsend.screen.users.adapter.publisher

import com.tomclaw.appsend.screen.users.UserItem
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
class PublisherItem(
    override val id: Long,
    val time: Long,
    override val user: UserBrief,
    override var hasMore: Boolean = false,
    override var hasProgress: Boolean = false,
) : UserItem
