package com.tomclaw.appsend.screen.chat

import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.dto.MessageType
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import com.tomclaw.appsend.screen.chat.adapter.incoming.IncomingMsgItem
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItem
import com.tomclaw.appsend.screen.chat.adapter.system.SystemMsgItem
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
            MessageType.PLAIN -> {
                val attachments = message.attachments
                    ?.takeIf { it.isNotEmpty() }
                    ?.map { att ->
                        MsgAttachment(
                            att.previewUrl,
                            att.originalUrl,
                            att.size,
                            att.width,
                            att.height
                        )
                    }
                var translated = false
                val text = if (translation == null || !translation.translated) {
                    message.text
                } else {
                    translated = true
                    translation.translation
                }
                if (message.incoming) {
                    IncomingMsgItem(
                        id = message.msgId.toLong(),
                        topicId = message.topicId,
                        msgId = message.msgId,
                        prevMsgId = message.prevMsgId,
                        type = message.type,
                        author = message.author,
                        text = text,
                        time = time,
                        date = date,
                        attachments = attachments,
                        translated = translated,
                    )
                } else {
                    OutgoingMsgItem(
                        id = message.msgId.toLong(),
                        topicId = message.topicId,
                        msgId = message.msgId,
                        prevMsgId = message.prevMsgId,
                        type = message.type,
                        author = message.author,
                        text = text,
                        time = time,
                        date = date,
                        attachments = attachments,
                        cookie = message.cookie,
                        sent = true,
                        translated = translated,
                    )
                }
            }

            MessageType.TOPIC_CREATED -> {
                SystemMsgItem(
                    id = message.msgId.toLong(),
                    text = resourceProvider.chatCreatedMessage(),
                    date = date,
                )
            }

            else -> {
                IncomingMsgItem(
                    id = message.msgId.toLong(),
                    topicId = message.topicId,
                    msgId = message.msgId,
                    prevMsgId = message.prevMsgId,
                    type = message.type,
                    author = message.author,
                    text = resourceProvider.unsupportedMessageText(),
                    time = time,
                    date = date,
                    attachments = null,
                    translated = false,
                )
            }
        }
    }

}
