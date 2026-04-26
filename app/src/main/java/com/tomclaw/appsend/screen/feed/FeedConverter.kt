package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.core.permissions.CapabilityAction
import com.tomclaw.appsend.core.permissions.CapabilityPolicy
import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.adapter.favorite.FavoriteItem
import com.tomclaw.appsend.screen.feed.adapter.subscribe.SubscribeItem
import com.tomclaw.appsend.screen.feed.adapter.text.TextItem
import com.tomclaw.appsend.screen.feed.adapter.unauthorized.UnauthorizedItem
import com.tomclaw.appsend.screen.feed.adapter.upload.UploadItem
import com.tomclaw.appsend.screen.feed.api.ACTION_DELETE
import com.tomclaw.appsend.screen.feed.api.FavoritePayload
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.screen.feed.api.SubscribePayload
import com.tomclaw.appsend.screen.feed.api.TextPayload
import com.tomclaw.appsend.screen.feed.api.UploadPayload
import java.util.concurrent.TimeUnit

interface FeedConverter {

    fun convert(post: PostEntity): FeedItem?

    fun unauthorized(): List<FeedItem>

}

class FeedConverterImpl : FeedConverter {

    override fun convert(post: PostEntity): FeedItem? {
        val actions = resolveActions(post)
        return when (post.payload) {
            is TextPayload -> TextItem(
                id = post.postId.toLong(),
                time = TimeUnit.SECONDS.toMillis(post.time),
                screenshots = post.payload.screenshots,
                text = post.payload.text,
                user = post.user,
                actions = actions,
                reacts = post.reacts,
            )

            is FavoritePayload -> FavoriteItem(
                id = post.postId.toLong(),
                time = TimeUnit.SECONDS.toMillis(post.time),
                appId = post.payload.appId,
                packageName = post.payload.packageName,
                icon = post.payload.icon,
                title = post.payload.title,
                verName = post.payload.verName,
                verCode = post.payload.verCode,
                size = post.payload.size,
                rating = post.payload.rating,
                downloads = post.payload.downloads,
                status = post.payload.status,
                category = post.payload.category,
                exclusive = post.payload.exclusive,
                openSource = post.payload.openSource,
                description = post.payload.description.orEmpty(),
                screenshots = post.payload.screenshots.orEmpty(),
                user = post.user,
                actions = actions,
                reacts = post.reacts,
            )

            is UploadPayload -> UploadItem(
                id = post.postId.toLong(),
                time = TimeUnit.SECONDS.toMillis(post.time),
                appId = post.payload.appId,
                packageName = post.payload.packageName,
                icon = post.payload.icon,
                title = post.payload.title,
                verName = post.payload.verName,
                verCode = post.payload.verCode,
                size = post.payload.size,
                status = post.payload.status,
                category = post.payload.category,
                exclusive = post.payload.exclusive,
                openSource = post.payload.openSource,
                description = post.payload.description.orEmpty(),
                screenshots = post.payload.screenshots.orEmpty(),
                user = post.user,
                actions = actions,
                reacts = post.reacts,
            )

            is SubscribePayload -> SubscribeItem(
                id = post.postId.toLong(),
                time = TimeUnit.SECONDS.toMillis(post.time),
                publisher = post.payload.publisher,
                user = post.user,
                actions = actions,
                reacts = post.reacts,
            )

            else -> null
        }
    }

    /**
     * Prefer server-resolved capabilities as the source of truth; fall
     * back to the legacy `actions[]` array for servers that don't ship
     * capability data yet. Once every deployed server includes
     * capabilities, the `actions` branch can be removed.
     */
    private fun resolveActions(post: PostEntity): List<String>? {
        val caps = post.capabilities ?: return post.actions
        val list = mutableListOf<String>()
        if (CapabilityPolicy.isAllowed(
                action = CapabilityAction.FEED_POST_DELETE,
                capabilities = caps,
                allowOnUnknown = false,
            )
        ) {
            list += ACTION_DELETE
        }
        return list.takeIf { it.isNotEmpty() }
    }

    override fun unauthorized(): List<FeedItem> {
        return listOf(
            UnauthorizedItem(id = 1)
        )
    }

}
