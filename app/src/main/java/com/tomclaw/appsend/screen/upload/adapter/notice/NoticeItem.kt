package com.tomclaw.appsend.screen.upload.adapter.notice

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class NoticeItem(
    override val id: Long,
    val type: NoticeType,
    val text: String,
    val clickable: Boolean,
) : Item, Parcelable

enum class NoticeType {
    INFO,
    WARNING,
    ERROR
}
