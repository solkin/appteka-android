package com.tomclaw.appsend.screen.chat.adapter

import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon

abstract class MsgItem(
    override val id: Long,
    val topicId: Int,
    val msgId: Int,
    val prevMsgId: Int,
    val userId: Int,
    val userIcon: UserIcon,
    val text: String,
    val time: Long,
    val type: Int,
    val attachment: MsgAttachment
) : Item
