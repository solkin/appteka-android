package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.adapter.text.TextItem
import com.tomclaw.appsend.screen.feed.api.PostEntity
import com.tomclaw.appsend.screen.feed.api.TYPE_TEXT

interface FeedConverter {

    fun convert(post: PostEntity): FeedItem

}

class FeedConverterImpl : FeedConverter {

    override fun convert(post: PostEntity): FeedItem {
        return when (post.type) {
            TYPE_TEXT -> TextItem(
                id = post.postId.toLong(),
                time = post.time,
                text = "Sample text",
                user = post.user
            )

            else -> throw IllegalArgumentException("Post type is not supported: $post")
        }
    }

}
