package com.tomclaw.appsend.screen.discuss

import com.tomclaw.appsend.screen.discuss.adapter.topic.TopicItem
import com.tomclaw.appsend.screen.discuss.api.TopicEntry

interface TopicConverter {

    fun convert(entry: TopicEntry): TopicItem

}

class TopicConverterImpl() : TopicConverter {

    override fun convert(entry: TopicEntry): TopicItem {
        return TopicItem(
            id = entry.topicId.toLong(),
            icon = entry.icon,
            title = entry.title,
            description = entry.description,
            packageName = entry.packageName,
            hasUnread = entry.readMsgId != entry.lastMsg.msgId,
            lastMsgText = entry.lastMsg.text,
            lastMsgUserIcon = entry.lastMsg.userIcon
        )
    }

}