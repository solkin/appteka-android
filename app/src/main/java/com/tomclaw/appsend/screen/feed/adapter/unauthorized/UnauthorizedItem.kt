package com.tomclaw.appsend.screen.feed.adapter.unauthorized

import android.os.Parcelable
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
data class UnauthorizedItem(
    override val id: Long,
) : FeedItem, Parcelable {
    override val user: UserBrief?
        get() = null
    override var hasMore: Boolean
        get() = false
        set(value) {}
    override var hasProgress: Boolean
        get() = false
        set(value) {}
}
