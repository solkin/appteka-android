package com.tomclaw.appsend.screen.topics

import com.tomclaw.appsend.dto.TopicEntry
import com.tomclaw.appsend.screen.topics.adapter.topic.TopicItem

interface TopicConverter {

    fun convert(entry: TopicEntry): TopicItem

}

class TopicConverterImpl(
    private val resourceProvider: TopicsResourceProvider
) : TopicConverter {

    override fun convert(entry: TopicEntry): TopicItem {
        val icon = when (entry.topicId) {
            1 -> COMMON_QNA_TOPIC_ICON
            else -> entry.icon.orEmpty()
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
            isPinned = entry.isPinned,
            hasUnread = entry.readMsgId != entry.lastMsg.msgId,
            lastMsgText = entry.lastMsg.text,
            lastMsgUserIcon = entry.lastMsg.userIcon,
        )
    }

}

const val COMMON_QNA_TOPIC_ICON = "file:///android_asset/topic_common_qna.png"
