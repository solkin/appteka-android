package com.tomclaw.appsend.screen.chat.adapter.incoming

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import kotlinx.parcelize.Parcelize

@Parcelize
data class IncomingMsgItem(
    override val id: Long,
    val topicId: Int,
    val msgId: Int,
    val prevMsgId: Int,
    val type: Int,
    val userId: Int,
    val userIcon: UserIcon,
    val text: String,
    val time: String,
    val date: String?,
    val attachment: MsgAttachment?
) : Item, Parcelable
