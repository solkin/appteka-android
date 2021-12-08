package com.tomclaw.appsend.screen.chat.adapter.outgoing

import android.os.Parcelable
import com.avito.konveyor.blueprint.Item
import com.tomclaw.appsend.dto.UserIcon
import com.tomclaw.appsend.screen.chat.adapter.MsgAttachment
import kotlinx.android.parcel.Parcelize

@Parcelize
data class OutgoingMsgItem(
    override val id: Long,
    val topicId: Int,
    val msgId: Int,
    val prevMsgId: Int,
    val userId: Int,
    val userIcon: UserIcon,
    val text: String,
    val time: Long,
    val cookie: String,
    val type: Int,
    val attachment: MsgAttachment
) : Item, Parcelable
