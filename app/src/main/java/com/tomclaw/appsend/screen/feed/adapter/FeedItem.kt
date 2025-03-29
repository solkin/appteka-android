package com.tomclaw.appsend.screen.feed.adapter

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.user.api.UserBrief

interface FeedItem : Item, Parcelable {
    val user: UserBrief?
    var hasMore: Boolean
    var hasProgress: Boolean
}
