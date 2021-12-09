package com.tomclaw.appsend.screen.chat

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import com.tomclaw.appsend.screen.chat.adapter.msg.IncomingMsgItem
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItem
import java.text.DateFormat

interface MessageConverter {

    fun convert(message: MessageEntity, prevMessage: MessageEntity?): Item

}

class MessageConverterImpl(
    private val timeFormatter: DateFormat,
    private val dateFormatter: DateFormat,
    private val resourceProvider: ChatResourceProvider
) : MessageConverter {

    override fun convert(message: MessageEntity, prevMessage: MessageEntity?): Item {
        val time = timeFormatter.format(message.time)
        val date = dateFormatter.format(message.time).takeIf {
            val prevTime = prevMessage?.time ?: 0
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
                if (message.incoming) {
                    IncomingMsgItem(
                        id = message.msgId.toLong(),
                        topicId = message.topicId,
                        msgId = message.msgId,
                        prevMsgId = message.prevMsgId,
                        type = message.type,
                        userId = message.userId,
                        userIcon = message.userIcon,
                        text = message.text,
                        time = time,
                        date = date,
                        attachment = attachment
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
                        text = message.text,
                        time = time,
                        date = date,
                        attachment = attachment,
                        cookie = message.cookie,
                        sent = true
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
                    attachment = null
                )
            }
        }
    }

}
