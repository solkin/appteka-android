package com.tomclaw.appsend.screen.topics

import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.topics.adapter.topic.TopicItem

interface TopicConverter {

    fun convert(entity: TopicEntity): TopicItem

}

class TopicConverterImpl(
    private val resourceProvider: TopicsResourceProvider
) : TopicConverter {

    override fun convert(entity: TopicEntity): TopicItem {
        val icon = when (entity.topicId) {
            1 -> COMMON_QNA_TOPIC_ICON
            else -> entity.icon.orEmpty()
        }
        val title = when (entity.topicId) {
            1 -> resourceProvider.commonQuestionsTopicTitle()
            else -> entity.title
        }
        return TopicItem(
            id = entity.topicId.toLong(),
            icon = icon,
            title = title,
            description = entity.description,
            packageName = entity.packageName,
            isPinned = entity.isPinned,
            hasUnread = (entity.readMsgId ?: 0) < entity.lastMsg.msgId,
            lastMsgId = entity.lastMsg.msgId,
            lastMsgText = entity.lastMsg.text,
            lastMsgUserIcon = entity.lastMsg.userIcon,
        )
    }

}

const val COMMON_QNA_TOPIC_ICON = "file:///android_asset/topic_common_qna.png"
