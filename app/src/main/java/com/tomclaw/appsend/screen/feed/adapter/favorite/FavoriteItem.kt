package com.tomclaw.appsend.screen.feed.adapter.favorite

import com.tomclaw.appsend.categories.Category
import com.tomclaw.appsend.dto.Screenshot
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.api.Reaction
import com.tomclaw.appsend.user.api.UserBrief
import kotlinx.parcelize.Parcelize

@Parcelize
class FavoriteItem(
    override val id: Long,
    val time: Long,
    val appId: String,
    val packageName: String,
    val icon: String?,
    val title: String,
    val verName: String,
    val verCode: Int,
    val size: Long,
    val rating: Float,
    val downloads: Int,
    val status: Int,
    val category: Category?,
    val exclusive: Boolean,
    val openSource: Boolean,
    val description: String?,
    val screenshots: List<Screenshot>,
    override val user: UserBrief,
    override val actions: List<String>?,
    val reacts: List<Reaction>?,
    override var hasMore: Boolean = false,
    override var hasProgress: Boolean = false,
) : FeedItem
