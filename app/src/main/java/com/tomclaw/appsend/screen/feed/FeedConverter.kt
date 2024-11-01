package com.tomclaw.appsend.screen.feed

import com.tomclaw.appsend.screen.feed.adapter.FeedItem
import com.tomclaw.appsend.screen.feed.api.FeedEntity

interface FeedConverter {

    fun convert(entity: FeedEntity): FeedItem

}

class FeedConverterImpl : FeedConverter {

    override fun convert(entity: FeedEntity): FeedItem {
        return when(entity) {
            else -> throw IllegalArgumentException("Entity type is not supported: $entity")
        }
    }

}
