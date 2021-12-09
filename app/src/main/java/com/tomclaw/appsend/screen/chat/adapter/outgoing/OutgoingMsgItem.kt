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
    val type: Int,
    val userId: Int,
    val userIcon: UserIcon,
    val text: String,
    val time: String,
    val date: String?,
    val attachment: MsgAttachment?,
    val cookie: String,
    val sent: Boolean
) : Item, Parcelable
