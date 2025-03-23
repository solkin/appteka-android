package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.adapter.favorite.FavoriteItem
import com.tomclaw.appsend.screen.feed.adapter.subscribe.SubscribeItem
import com.tomclaw.appsend.screen.feed.adapter.text.TextItem
import com.tomclaw.appsend.screen.feed.adapter.upload.UploadItem
import com.tomclaw.appsend.screen.feed.api.FavoritePayload
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.screen.feed.api.SubscribePayload
import com.tomclaw.appsend.screen.feed.api.TextPayload
import com.tomclaw.appsend.screen.feed.api.UploadPayload
import java.util.concurrent.TimeUnit

interface FeedConverter {

    fun convert(post: PostEntity): FeedItem?

}

class FeedConverterImpl : FeedConverter {

    override fun convert(post: PostEntity): FeedItem? {
        return when (post.payload) {
            is TextPayload -> TextItem(
                id = post.postId.toLong(),
                time = TimeUnit.SECONDS.toMillis(post.time),
                screenshots = post.payload.screenshots,
                text = post.payload.text,
                user = post.user
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
                user = post.user
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
                user = post.user
            )
            is SubscribePayload -> SubscribeItem(
                id = post.postId.toLong(),
                time = TimeUnit.SECONDS.toMillis(post.time),
                publisher = post.payload.publisher,
                user = post.user
            )

            else -> null
        }
    }

}
