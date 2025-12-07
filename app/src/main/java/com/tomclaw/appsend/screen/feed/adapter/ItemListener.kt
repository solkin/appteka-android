package com.tomclaw.appsend.screen.feed.adapter

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.screen.feed.api.Reaction
import com.tomclaw.appsend.user.api.UserBrief

interface ItemListener {

    fun onItemClick(item: Item)

    fun onLoadMore(item: Item)

    fun onImageClick(items: List<Screenshot>, clicked: Int)

    fun onAppClick(appId: String, title: String?)

    fun onUserClick(user: UserBrief)

    fun onMenuClick(item: FeedItem)

    fun onLoginClick()

    fun onReactionClick(item: FeedItem, reaction: Reaction)

}
