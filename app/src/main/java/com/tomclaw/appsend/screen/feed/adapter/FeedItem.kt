package com.tomclaw.appsend.screen.feed.adapter

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.screen.feed.api.Reaction
import com.tomclaw.appsend.user.api.UserBrief

interface FeedItem : Item, Parcelable {
    val user: UserBrief?
    val actions: List<String>?
    var hasMore: Boolean
    var hasProgress: Boolean
    
    fun getReactions(): List<Reaction>?
    
    fun withReactions(reactions: List<Reaction>): FeedItem
}
