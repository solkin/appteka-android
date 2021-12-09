package com.tomclaw.appsend.screen.chat

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.MessageEntity
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import com.tomclaw.appsend.screen.chat.adapter.msg.IncomingMsgItem
import com.tomclaw.appsend.screen.chat.adapter.outgoing.OutgoingMsgItem

interface MessageConverter {

    fun convert(message: MessageEntity): Item

}

class MessageConverterImpl(
    private val resourceProvider: ChatResourceProvider
) : MessageConverter {

    override fun convert(message: MessageEntity): Item {

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
                        userId = message.userId,
                        userIcon = message.userIcon,
                        text = message.text,
                        time = message.time,
                        type = message.type,
                        attachment = attachment
                    )
                } else {
                    OutgoingMsgItem(
                        id = message.msgId.toLong(),
                        topicId = message.topicId,
                        msgId = message.msgId,
                        prevMsgId = message.prevMsgId,
                        userId = message.userId,
                        userIcon = message.userIcon,
                        text = message.text,
                        time = message.time,
                        cookie = message.cookie,
                        type = message.type,
                        attachment = attachment
                    )
                }
            }
            else -> {
                IncomingMsgItem(
                    id = message.msgId.toLong(),
                    topicId = message.topicId,
                    msgId = message.msgId,
                    prevMsgId = message.prevMsgId,
                    userId = message.userId,
                    userIcon = message.userIcon,
                    text = resourceProvider.unsupportedMessageText(),
                    time = message.time,
                    type = message.type,
                    attachment = null
                )
            }
        }
    }

}

const val COMMON_QNA_TOPIC_ICON = "file:///android_asset/topic_common_qna.png"
