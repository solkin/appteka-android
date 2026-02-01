package com.tomclaw.appsend.screen.details.adapter.status

import android.os.Parcelable
import com.tomclaw.appsend.util.adapter.Item
import kotlinx.parcelize.Parcelize

@Parcelize
data class StatusItem(
    override val id: Long,
    val type: StatusType,
    val text: String,
    val actionType: StatusAction,
    val actionLabel: String?,
) : Item, Parcelable

enum class StatusType {
    INFO,
    WARNING,
    ERROR
}

enum class StatusAction {
    NONE,
    EDIT_META,
    UNPUBLISH
}
