package com.tomclaw.appsend.screen.topics

import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.MessageType
import com.tomclaw.appsend.dto.TopicEntity
import com.tomclaw.appsend.screen.topics.adapter.topic.TopicItem
import com.tomclaw.appsend.util.stripLeadingQuote

interface TopicConverter {

    fun convert(entity: TopicEntity, translated: Boolean): TopicItem

}

class TopicConverterImpl(
    private val resourceProvider: TopicsResourceProvider
) : TopicConverter {

    override fun convert(entity: TopicEntity, translated: Boolean): TopicItem {
        val lastMsg = entity.lastMsg
            ?: throw IllegalStateException("lastMsg must be specified")
        val translation = lastMsg.translation?.takeIf { it.isNotBlank() }
        val showTranslation = translated && translation != null
        return TopicItem(
            id = entity.topicId.toLong(),
            icon = entity.iconOrDefault(),
            title = entity.titleOrDefault(),
            description = entity.descriptionOrDefault(),
            packageName = entity.packageName,
            isPinned = entity.isPinned,
            hasUnread = entity.readMsgId?.let { it < lastMsg.msgId } == true,
            lastMsgId = lastMsg.msgId,
            lastMsgText = lastMessageText(lastMsg, translation, showTranslation),
            lastMsgUserIcon = lastMsg.author.icon,
            lastMsgUserBadge = lastMsg.author.primaryBadge,
            hasTranslation = translation != null,
            translated = showTranslation,
        )
    }

    private fun TopicEntity.iconOrDefault(): String = when (topicId) {
        COMMON_QNA_TOPIC_ID -> COMMON_QNA_TOPIC_ICON
        else -> icon.orEmpty()
    }

    private fun TopicEntity.titleOrDefault(): String = when (topicId) {
        COMMON_QNA_TOPIC_ID -> resourceProvider.commonQuestionsTopicTitle()
        else -> title
    }

    private fun TopicEntity.descriptionOrDefault(): String? = when (topicId) {
        COMMON_QNA_TOPIC_ID -> resourceProvider.commonQuestionsTopicDescription()
        else -> description
    }

    private fun lastMessageText(
        lastMsg: MessageEntity,
        translation: String?,
        showTranslation: Boolean,
    ): String {
        val rawText = if (showTranslation) translation!! else lastMsg.text
        val strippedText = rawText.stripLeadingQuote()
        val attachmentsCount = lastMsg.attachments?.size ?: 0
        return when {
            lastMsg.type == MessageType.TOPIC_CREATED -> resourceProvider.chatCreatedMessage()
            strippedText.isNotBlank() -> strippedText
            attachmentsCount > 0 -> resourceProvider.attachmentsPlaceholder(attachmentsCount)
            else -> strippedText
        }
    }

}

private const val COMMON_QNA_TOPIC_ID = 1

const val COMMON_QNA_TOPIC_ICON = "file:///android_asset/topic_common_qna.png"
