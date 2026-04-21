package com.tomclaw.appsend.screen.chat.adapter.system

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class SystemMsgItem(
    override val id: Long,
    val text: String,
    val date: String?,
) : Item, Parcelable
