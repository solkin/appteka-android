package com.tomclaw.appsend.screen.chat

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import com.tomclaw.appsend.screen.chat.adapter.incoming.IncomingMsgItem
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItem
import com.tomclaw.appsend.screen.chat.api.TranslationEntity
import java.text.DateFormat

interface MessageConverter {

    fun convert(
        message: MessageEntity,
        prevMessage: MessageEntity?,
        translation: TranslationEntity?
    ): Item

}

class MessageConverterImpl(
    private val timeFormatter: DateFormat,
    private val dateFormatter: DateFormat,
    private val resourceProvider: ChatResourceProvider
) : MessageConverter {

    override fun convert(
        message: MessageEntity,
        prevMessage: MessageEntity?,
        translation: TranslationEntity?
    ): Item {
        val time = timeFormatter.format(message.time * 1000)
        val date = dateFormatter.format(message.time * 1000).takeIf {
            val prevTime = (prevMessage?.time ?: 0) * 1000
            val dateChanged = (prevTime > 0 && it != dateFormatter.format(prevTime))
            val sendingMessage =
                prevMessage?.time == Long.MAX_VALUE || message.time == Long.MAX_VALUE
            val firstMessage = message.prevMsgId == 0
            (firstMessage || dateChanged) && !sendingMessage
        }
        return when (message.type) {
            0 -> {
                var attachment: MsgAttachment? = null
                if (message.attachment != null) {
                    attachment = MsgAttachment(
                        message.attachment.previewUrl,
                        message.attachment.originalUrl,
                        message.attachment.size,
                        message.attachment.width,
                        message.attachment.height
                    )
                }
                var translated = false
                val text = if (attachment != null) {
                    resourceProvider.unsupportedMessageText()
                } else {
                    if (translation == null || !translation.translated) {
                        message.text
                    } else {
                        translated = true
                        translation.translation
                    }
                }
                if (message.incoming) {
                    IncomingMsgItem(
                        id = message.msgId.toLong(),
                        topicId = message.topicId,
                        msgId = message.msgId,
                        prevMsgId = message.prevMsgId,
                        type = message.type,
                        userId = message.userId,
                        userIcon = message.userIcon,
                        text = text,
                        time = time,
                        date = date,
                        attachment = attachment,
                        translated = translated,
                    )
                } else {
                    OutgoingMsgItem(
                        id = message.msgId.toLong(),
                        topicId = message.topicId,
                        msgId = message.msgId,
                        prevMsgId = message.prevMsgId,
                        type = message.type,
                        userId = message.userId,
                        userIcon = message.userIcon,
                        text = text,
                        time = time,
                        date = date,
                        attachment = attachment,
                        cookie = message.cookie,
                        sent = true,
                        translated = translated,
                    )
                }
            }

            else -> {
                IncomingMsgItem(
                    id = message.msgId.toLong(),
                    topicId = message.topicId,
                    msgId = message.msgId,
                    prevMsgId = message.prevMsgId,
                    type = message.type,
                    userId = message.userId,
                    userIcon = message.userIcon,
                    text = resourceProvider.unsupportedMessageText(),
                    time = time,
                    date = date,
                    attachment = null,
                    translated = false,
                )
            }
        }
    }

}
