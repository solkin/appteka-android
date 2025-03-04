package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.adapter.text.TextItem
import com.tomclaw.appsend.screen.feed.adapter.upload.UploadItem
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.screen.feed.api.TextPayload
import com.tomclaw.appsend.screen.feed.api.UploadPayload
import java.util.concurrent.TimeUnit

interface FeedConverter {

    fun convert(post: PostEntity): FeedItem

}

class FeedConverterImpl : FeedConverter {

    override fun convert(post: PostEntity): FeedItem {
        return when (post.payload) {
            is TextPayload -> TextItem(
                id = post.postId.toLong(),
                time = TimeUnit.SECONDS.toMillis(post.time),
                screenshots = post.payload.screenshots,
                text = post.payload.text,
                user = post.user
            )
            is UploadPayload -> UploadItem(
                id = post.postId.toLong(),
                time = TimeUnit.SECONDS.toMillis(post.time),
                screenshots = post.payload.screenshots.orEmpty(),
                text = post.payload.description.orEmpty(),
                user = post.user
            )

            else -> throw IllegalArgumentException("Post type is not supported: $post")
        }
    }

}
