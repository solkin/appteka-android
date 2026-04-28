package com.tomclaw.appsend.screen.chat.adapter.outgoing

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import com.tomclaw.appsend.dto.BadgeMark
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import kotlinx.parcelize.Parcelize

@Parcelize
data class OutgoingMsgItem(
    override val id: Long,
    val topicId: Int,
    val msgId: Int,
    val prevMsgId: Int,
    val type: Int,
    val userId: Int,
    val userIcon: UserIcon,
    val userBadge: BadgeMark?,
    val text: String,
    val time: String,
    val date: String?,
    val attachments: List<MsgAttachment>?,
    val cookie: String?,
    val sent: Boolean,
    val translated: Boolean,
) : Item, Parcelable
