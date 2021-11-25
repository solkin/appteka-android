package com.tomclaw.appsend.screen.discuss

import com.tomclaw.appsend.screen.discuss.adapter.topic.TopicItem
import com.tomclaw.appsend.screen.discuss.api.TopicEntry

interface TopicConverter {

    fun convert(entry: TopicEntry): TopicItem

}

class TopicConverterImpl(
    private val resourceProvider: DiscussResourceProvider
    ) : TopicConverter {

    override fun convert(entry: TopicEntry): TopicItem {
        val icon = when (entry.topicId) {
            1 -> "file:///android_asset/topic_common_qna.png"
            else -> entry.icon
        }
        val title = when (entry.topicId) {
            1 -> resourceProvider.commonQuestionsTopicTitle()
            else -> entry.title
        }
        return TopicItem(
            id = entry.topicId.toLong(),
            icon = icon,
            title = title,
            description = entry.description,
            packageName = entry.packageName,
            hasUnread = entry.readMsgId != entry.lastMsg.msgId,
            lastMsgText = entry.lastMsg.text,
            lastMsgUserIcon = entry.lastMsg.userIcon,
        )
    }

}